package controllers

import models.Species
import play.api.libs.json
import services.{DbService, GbifParser, GbifService}
import play.api.mvc._
import javax.inject.{Inject, Singleton}
import play.api.libs.json._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class GbifToolController @Inject()(var controllerComponents: ControllerComponents) extends BaseController {

  implicit val speciesJson: Writes[Species] = new Writes[Species] {
    def writes(species: Species): JsObject = Json.obj(
      "latinName" -> species.latinName
    )
  }

  /**
   * Makes a list of species used in the Main table
   *
   * e.g. curl -v localhost:9000/list
   */
  def listSpecies(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val species  = DbService.listSpecies
    Ok(json.Json.toJson(species))
  }

  /**
   * Deletes all data from GBIF check columns in System table
   *
   * e.g. curl -v --request DELETE localhost:9000/cleanup
   */
  def cleanup: Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
   DbService.markAllUnused
   DbService.deleteAllGBIFData()
   NoContent
  }

  /**
   * Gets all species actually used in Main,
   * marks these as used in the System table.
   *
   * e.g. curl -v --request PUT localhost:9000/markAllUsed
   */
  def markAllUsed(): Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    val speciesList  = DbService.listSpecies
    speciesList match {
      case Some(speciesList) =>
        DbService.markAllUsed(speciesList)
        Accepted
      case None => NoContent
    }
  }

  /**
   * Matches a species name with the GBIF taxonomic backbone
   * REPLACE SPACES BY UNDERSCORES, like so: Puma_concolor
   * Updates the columns GBIF_check, GBIF_response, GBIF_usage_key in the system table.
   *
   * e.g. curl -v --request PUT localhost:9000/match/Puma_concolor
   * GBIF equivalent https://api.gbif.org/v1/species/match?verbose=true&kingdom=Animalia&name=Puma_concolor
   * */
  def matchName(name: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val gbifData = GbifService.matchName(name)
    val species = new Species(name.replace('_', ' '))
    gbifData match {
      case Some(gbifData) =>
        val parsedSpecies: Species = GbifParser.parse(species, gbifData)
        DbService.updateGbifData(parsedSpecies)
        Accepted(json.Json.parse(gbifData))
      case None => NoContent
    }
  }

  /**
   * Gets a list of species used in the Main table
   * For each species, calls the GBIF API, get its status, usage key and evaluate the response
   * Updates the columns GBIF_check, GBIF_response, GBIF_usage_key in the system table.
   *
   * e.g. curl -v --request PUT localhost:9000/matchAll
   */
  def matchAll(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val speciesList  = DbService.listSpecies
    speciesList match {
      case Some(speciesList) =>
        for(species <- speciesList) {
          val gbifData = GbifService.matchName(species.latinName)
          gbifData match {
            case Some(gbifData) =>
              val parsedSpecies:Species = GbifParser.parse(species, gbifData)
              // println(parsedSpecies)
              DbService.updateGbifData(parsedSpecies)
            case None =>
              println(species.latinName + " not found")
          }
        }
        Accepted
      case None =>
        NoContent
    }
  }
}

