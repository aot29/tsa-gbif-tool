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

    "parse json response for synonym match" in {
      val species:Species = new Species("Bufo americanus")
      val jsonString: String = """{"usageKey":5217021,"acceptedUsageKey":2422872,"scientificName":"Bufo americanus Holbrook, 1836","canonicalName":"Bufo americanus","rank":"SPECIES","status":"SYNONYM","confidence":100,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=0; nextMatch=5","matchType":"EXACT","alternatives":[{"usageKey":5217030,"acceptedUsageKey":2422888,"scientificName":"Bufo mexicanus Brocchi, 1879","canonicalName":"Bufo mexicanus","rank":"SPECIES","status":"SYNONYM","confidence":15,"note":"Similarity: name=5; authorship=0; classification=4; rank=6; status=0","matchType":"FUZZY","kingdom":"Animalia","phylum":"Chordata","order":"Anura","family":"Bufonidae","genus":"Anaxyrus","species":"Anaxyrus mexicanus","kingdomKey":1,"phylumKey":44,"classKey":131,"orderKey":952,"familyKey":6727,"genusKey":2422857,"speciesKey":2422888,"synonym":true,"class":"Amphibia"}],"kingdom":"Animalia","phylum":"Chordata","order":"Anura","family":"Bufonidae","genus":"Anaxyrus","species":"Anaxyrus americanus","kingdomKey":1,"phylumKey":44,"classKey":131,"orderKey":952,"familyKey":6727,"genusKey":2422857,"speciesKey":2422872,"synonym":true,"class":"Amphibia"}"""
      var responseSpecies: Species = GbifParser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Bufo americanus"
      responseSpecies.GbifUsageKey mustBe 5217021
      responseSpecies.status mustBe TaxonomicStatus.SYNONYM
      // if this is a synonym, put the gbif response for the synonym here
      responseSpecies.GbifResponse must not be None
      responseSpecies.GbifResponse mustBe jsonString
    }
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
