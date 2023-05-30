package services

import models.TaxonomicStatus.TaxonomicStatus
import models.{Species, TaxonomicStatus}
import play.api.libs.json
import play.api.libs.json.{JsObject, JsValue}


class GbifParser(db: DbService) {
  def parse(species:Species, text: String): Species = {
    db.setLineage(species) // load lineage from db
    val jsonVal = json.Json.parse(text)
    var parsedName:String = parseName(jsonVal)
    var parsedFamily:String = parseFamily(jsonVal)
    val parsedOrder:String = parseOrder(jsonVal)
    val parsedStatus:TaxonomicStatus = parseStatus(species, parsedName, parsedFamily, parsedOrder, jsonVal)
    var parsedUsageKey:Int = parseUsageKey(jsonVal)
    val parsedGbifString:String = jsonVal.toString

    // if there's a conflict, reset the name to the originally requested name
    // (so as to update the correct row in the system table)
    // it's left up to the user to sort this out using the json response
    if (parsedStatus == TaxonomicStatus.UNKNOWN) {
      parsedName = species.latinName
      parsedUsageKey = 0
    }

    val newSpecies = new Species(
      parsedName,
      parsedStatus,
      parsedUsageKey,
      parsedGbifString
    )
    if (parsedStatus == TaxonomicStatus.ACCEPTED) {
      newSpecies.ordo = parsedOrder
      newSpecies.familia = parsedFamily
    } else {
      newSpecies.ordo = species.ordo
      newSpecies.familia = species.familia
    }
    newSpecies
  }

  /**
   * Store the accepted usage key for synonyms, as otherwise the species key and usakey key would be different
   *
   * @param jsonVal
   * @return
   */
  private def parseUsageKey(jsonVal: JsValue): Int = {
    if (jsonVal.as[JsObject].keys.contains("acceptedUsageKey"))
      jsonVal("acceptedUsageKey").as[Int]
    else
      jsonVal("usageKey").as[Int]
  }

  private def parseName(jsonVal: JsValue): String = {
    jsonVal("canonicalName").as[String]
  }

  private def parseFamily(jsonVal: JsValue): String = {
    try {
      jsonVal("family").as[String]
    } catch {
      case e: Exception => ""
    }
  }

  private def parseOrder(jsonVal: JsValue): String = {
    try {
      jsonVal("order").as[String]
    } catch {
      case e:Exception => ""
    }
  }

  private def parseStatus(species: Species,
                          parsedName:String,
                          parsedFamily:String,
                          parsedOrder:String,
                          jsonVal: JsValue): TaxonomicStatus = {
    if (species.latinName == parsedName && species.familia == parsedFamily && species.ordo == parsedOrder) {
      if (jsonVal("status").as[String] == "SYNONYM") {
        TaxonomicStatus.SYNONYM
      } else {
        // covers ACCEPTED and DOUBTFUL
        TaxonomicStatus.ACCEPTED
      }
    } else if (parsedName  == "Animalia") {
        // no corresponding species was found
        TaxonomicStatus.UNKNOWN
    } else if (species.latinName == parsedName) {
      // species name was found, but higher taxon ranks differ
      if (parsedFamily == "" || parsedOrder == "") {
        // family or order are missing, e.g. "Reptiles"
        TaxonomicStatus.CONFLICTING
      } else {
        // accept family and order from gbif
        TaxonomicStatus.ACCEPTED
      }
    } else {
        // a species was found, but accepted name is different
        TaxonomicStatus.CONFLICTING
    }
  }
}
