package services

import models.{Species, TaxonomicStatus}
import models.TaxonomicStatus.TaxonomicStatus
import play.api.libs.json
import play.api.libs.json.JsValue
import services.DbService


object GbifParser {
  def parse(species:Species, text: String): Species = {
    DbService.setLineage(species)
    val jsonVal = json.Json.parse(text)
    var parsedName:String = parseName(jsonVal)
    var parsedFamily:String = parseFamily(jsonVal)
    val parsedOrder:String = parseOrder(jsonVal)
    val parsedStatus:TaxonomicStatus = parseStatus(species, parsedName, parsedFamily, parsedOrder, jsonVal)
    var parsedUsageKey:Int = jsonVal("usageKey").as[Int]
    val parsedGbifString:String = jsonVal.toString

    // if there's a conflict, reset the name to the originally requested name
    // (so as to update the correct row in the system table)
    // it's left up to the user to sort this out using the json response
    if (parsedStatus == TaxonomicStatus.UNKNOWN) {
      parsedName = species.latinName
      parsedUsageKey = 0
    }

    new Species(
      parsedName,
      parsedStatus,
      parsedUsageKey,
      parsedGbifString
    )
  }

  def parseName(jsonVal: JsValue): String = {
    jsonVal("canonicalName").as[String]
  }

  def parseFamily(jsonVal: JsValue): String = {
    try {
      jsonVal("family").as[String]
    } catch {
      case e: Exception => ""
    }
  }

  def parseOrder(jsonVal: JsValue): String = {
    try {
      jsonVal("order").as[String]
    } catch {
      case e:Exception => ""
    }
  }

  def parseStatus(
      species: Species, parsedName:String, parsedFamily:String, parsedOrder:String, jsonVal: JsValue
    ): TaxonomicStatus = {
    if (species.latinName == parsedName && species.familia == parsedFamily && species.ordo == parsedOrder) {
      if (jsonVal("status").as[String] == "SYNONYM") {
        TaxonomicStatus.SYNONYM
      } else {
        // covers ACCEPTED and DOUBTFUL
        TaxonomicStatus.ACCEPTED
      }
    } else {
      if (parsedName  == "Animalia") {
        // no corresponding species was found
        TaxonomicStatus.UNKNOWN
      } else {
        val latinName = species.latinName
        // a species was found, but accepted name is different
        println(f"CONFLICT $latinName%s $parsedName%s")
        TaxonomicStatus.CONFLICTING
      }
    }
  }

}
