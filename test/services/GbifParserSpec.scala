package services

import models.{Species, TaxonomicStatus}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._

class GbifParserSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  private val db = new MySQLDbService
  private val parser: GbifParser = new GbifParser(db)

  "GbifParser" should {
    "parse json response for exact match" in {
      val species:Species = new Species("Puma concolor")
      val jsonString:String = """{"usageKey":2435099,"scientificName":"Puma concolor (Linnaeus, 1771)","canonicalName":"Puma concolor","rank":"SPECIES","status":"ACCEPTED","confidence":100,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=1; singleMatch=5","matchType":"EXACT","kingdom":"Animalia","phylum":"Chordata","order":"Carnivora","family":"Felidae","genus":"Puma","species":"Puma concolor","kingdomKey":1,"phylumKey":44,"classKey":359,"orderKey":732,"familyKey":9703,"genusKey":2435098,"speciesKey":2435099,"synonym":false,"class":"Mammalia"}"""
      var responseSpecies: Species = parser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Puma concolor"
      responseSpecies.GbifUsageKey mustBe 2435099
      responseSpecies.status mustBe TaxonomicStatus.ACCEPTED
    }

    "parse json response for synonym match" in {
      val species:Species = new Species("Bufo americanus")
      val jsonString: String = """{"usageKey":5217021,"acceptedUsageKey":2422872,"scientificName":"Bufo americanus Holbrook, 1836","canonicalName":"Bufo americanus","rank":"SPECIES","status":"SYNONYM","confidence":100,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=0; nextMatch=5","matchType":"EXACT","alternatives":[{"usageKey":5217030,"acceptedUsageKey":2422888,"scientificName":"Bufo mexicanus Brocchi, 1879","canonicalName":"Bufo mexicanus","rank":"SPECIES","status":"SYNONYM","confidence":15,"note":"Similarity: name=5; authorship=0; classification=4; rank=6; status=0","matchType":"FUZZY","kingdom":"Animalia","phylum":"Chordata","order":"Anura","family":"Bufonidae","genus":"Anaxyrus","species":"Anaxyrus mexicanus","kingdomKey":1,"phylumKey":44,"classKey":131,"orderKey":952,"familyKey":6727,"genusKey":2422857,"speciesKey":2422888,"synonym":true,"class":"Amphibia"}],"kingdom":"Animalia","phylum":"Chordata","order":"Anura","family":"Bufonidae","genus":"Anaxyrus","species":"Anaxyrus americanus","kingdomKey":1,"phylumKey":44,"classKey":131,"orderKey":952,"familyKey":6727,"genusKey":2422857,"speciesKey":2422872,"synonym":true,"class":"Amphibia"}"""
      var responseSpecies: Species = parser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Bufo americanus"
      responseSpecies.GbifUsageKey mustBe 2422872 // store the accepted usage key for synonyms, as otherwise the species key and usakey key would be different
      responseSpecies.status mustBe TaxonomicStatus.SYNONYM
      // if this is a synonym, put the gbif response for the synonym here
      responseSpecies.GbifResponse must not be None
      responseSpecies.GbifResponse mustBe jsonString
    }
    "parse json response for no match" in {
      val species: Species = new Species("div.")
      val jsonString: String = """{"usageKey":1,"scientificName":"Animalia","canonicalName":"Animalia","rank":"KINGDOM","status":"ACCEPTED","confidence":99,"note":"Similarity: name=100; classification=4; rank=12; status=1; nextMatch=5","matchType":"HIGHERRANK","kingdom":"Animalia","kingdomKey":1,"synonym":false}"""
      var responseSpecies: Species = parser.parse(species, jsonString)
      responseSpecies.latinName mustBe "div."
      responseSpecies.GbifUsageKey mustBe 0
      responseSpecies.status mustBe TaxonomicStatus.UNKNOWN
      responseSpecies.GbifResponse mustBe jsonString
    }
    "parse json response for reptile without an ordo" in {
      val species: Species = new Species("Aldabrachelys gigantea")
      val jsonString: String = """{"usageKey":7696021,"scientificName":"Aldabrachelys gigantea (Schweigger, 1812)","canonicalName":"Aldabrachelys gigantea","rank":"SPECIES","status":"ACCEPTED","confidence":100,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=1; singleMatch=5","matchType":"EXACT","kingdom":"Animalia","phylum":"Chordata","family":"Testudinidae","genus":"Aldabrachelys","species":"Aldabrachelys gigantea","kingdomKey":1,"phylumKey":44,"classKey":11418114,"familyKey":9618,"genusKey":2441821,"speciesKey":7696021,"synonym":false,"class":"Testudines"}"""
      var responseSpecies: Species = parser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Aldabrachelys gigantea"
      responseSpecies.GbifUsageKey mustBe 7696021
      responseSpecies.familia mustBe "Testudinidae"
      responseSpecies.ordo mustBe "Testudines" // is empty in GBIF
      responseSpecies.status mustBe TaxonomicStatus.CONFLICTING
      responseSpecies.GbifResponse mustBe jsonString
    }
    "parse json response for conflict resolved automatically" in {
      val species: Species = new Species("Anoplotrupes stercorosus")
      val jsonString: String = """{"usageKey":1071240,"scientificName":"Anoplotrupes stercorosus (Hartmann, 1791)","canonicalName":"Anoplotrupes stercorosus","rank":"SPECIES","status":"ACCEPTED","confidence":99,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=1; nextMatch=3","matchType":"EXACT","alternatives":[{"usageKey":12038775,"scientificName":"Anoplotrupes stercorosus (Scriba, 1791)","canonicalName":"Anoplotrupes stercorosus","rank":"SPECIES","status":"DOUBTFUL","confidence":97,"note":"Similarity: name=110; authorship=0; classification=4; rank=6; status=-5","matchType":"EXACT","kingdom":"Animalia","phylum":"Arthropoda","order":"Coleoptera","family":"Geotrupidae","genus":"Anoplotrupes","species":"Anoplotrupes stercorosus","kingdomKey":1,"phylumKey":54,"classKey":216,"orderKey":1470,"familyKey":8495,"genusKey":1071236,"speciesKey":12038775,"synonym":false,"class":"Insecta"}],"kingdom":"Animalia","phylum":"Arthropoda","order":"Coleoptera","family":"Geotrupidae","genus":"Anoplotrupes","species":"Anoplotrupes stercorosus","kingdomKey":1,"phylumKey":54,"classKey":216,"orderKey":1470,"familyKey":8495,"genusKey":1071236,"speciesKey":1071240,"synonym":false,"class":"Insecta"}""".stripMargin
      var responseSpecies: Species = parser.parse(species, jsonString)
      responseSpecies.latinName mustBe "Anoplotrupes stercorosus"
      responseSpecies.GbifUsageKey mustBe 1071240
      responseSpecies.familia mustBe "Geotrupidae" // was: Scarabaeidae, automatically updated
      responseSpecies.ordo mustBe "Coleoptera"
      responseSpecies.status mustBe TaxonomicStatus.ACCEPTED
      responseSpecies.GbifResponse mustBe jsonString
    }
  }
}
