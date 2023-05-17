package services

import akka.pattern.StatusReply.Success
import models.{Species, TaxonomicStatus}
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class GbifParserSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "GbifParser" should {
    "parse json response for exact match" in {
      val species:Species = new Species("Puma concolor")
      val jsonString:String = """{"usageKey":2435099,"scientificName":"Puma concolor (Linnaeus, 1771)","canonicalName":"Puma concolor","rank":"SPECIES","status":"ACCEPTED","confidence":100,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=1; singleMatch=5","matchType":"EXACT","kingdom":"Animalia","phylum":"Chordata","order":"Carnivora","family":"Felidae","genus":"Puma","species":"Puma concolor","kingdomKey":1,"phylumKey":44,"classKey":359,"orderKey":732,"familyKey":9703,"genusKey":2435098,"speciesKey":2435099,"synonym":false,"class":"Mammalia"}"""
      var responseSpecies: Species = GbifParser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Puma concolor"
      responseSpecies.GbifUsageKey mustBe 2435099
      responseSpecies.status mustBe TaxonomicStatus.ACCEPTED
    }
    /* To do: find a good example for a synonym
    "parse json response for synonym match" in {
      val species:Species = new Species("Canis familiaris")
      val jsonString: String = """{"usageKey":5219200,"acceptedUsageKey":6164210,"scientificName":"Canis familiaris Linnaeus, 1758","canonicalName":"Canis familiaris","rank":"SPECIES","status":"SYNONYM","confidence":100,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=0; singleMatch=5","matchType":"EXACT","kingdom":"Animalia","phylum":"Chordata","order":"Carnivora","family":"Canidae","genus":"Canis","species":"Canis lupus","kingdomKey":1,"phylumKey":44,"classKey":359,"orderKey":732,"familyKey":9701,"genusKey":5219142,"speciesKey":5219173,"synonym":true,"class":"Mammalia"}"""
      var responseSpecies: Species = GbifParser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Canis familiaris"
      responseSpecies.GbifUsageKey mustBe 5219200
      responseSpecies.status mustBe TaxonomicStatus.SYNONYM
      // if this is a synonym, put the gbif response for the synonym here
      responseSpecies.GbifResponse must not be None
      responseSpecies.GbifResponse mustBe jsonString
    }
    */
    "parse json response for no match" in {
      val species: Species = new Species("div.")
      val jsonString: String = """{"usageKey":1,"scientificName":"Animalia","canonicalName":"Animalia","rank":"KINGDOM","status":"ACCEPTED","confidence":99,"note":"Similarity: name=100; classification=4; rank=12; status=1; nextMatch=5","matchType":"HIGHERRANK","kingdom":"Animalia","kingdomKey":1,"synonym":false}"""
      var responseSpecies: Species = GbifParser.parse(species, jsonString)
      responseSpecies.latinName mustBe "div."
      responseSpecies.GbifUsageKey mustBe 0
      responseSpecies.status mustBe TaxonomicStatus.UNKNOWN
      responseSpecies.GbifResponse mustBe jsonString
    }
  }
}
