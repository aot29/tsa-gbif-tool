package controllers

import models.Species
import play.api.libs.json
import play.api.libs.json._
import play.api.mvc._
import services.{GbifParser, GbifService, MySQLDbService => db}

import javax.inject.{Inject, Singleton}

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
    try {
      val species  = db.listSpecies
      Ok(json.Json.toJson(species))
    } catch {
      case e: Exception => e.printStackTrace(); InternalServerError
    } finally {
      db.close()
    }
  }

  /**
   * Deletes all data from GBIF check columns in System table
   *
   * e.g. curl -v --request DELETE localhost:9000/cleanup
   */
  def cleanup: Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    try {
      db.markAllUnused
      db.deleteAllGBIFData()
      NoContent
    } catch {
      case e: Exception => e.printStackTrace(); InternalServerError
    } finally {
      db.close()
    }
  }

  /**
   * Gets all species actually used in Main,
   * marks these as used in the System table.
   *
   * e.g. curl -v --request PUT localhost:9000/markAllUsed
   */
  def markAllUsed(): Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    try {
      val speciesList  = db.listSpecies
      db.markAllUsed(speciesList)
      Accepted
    } catch {
      case e: Exception => e.printStackTrace(); InternalServerError
    } finally {
      db.close()
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
    try {
      val parser: GbifParser = new GbifParser(db)
      val gbifData = GbifService.matchName(name)
        gbifData match {
          case Some(gbifData) =>
            val species = new Species(name.replace('_', ' '))
            val parsedSpecies: Species = parser.parse(species, gbifData)
            val rowCount = db.updateGbifData(parsedSpecies)
            rowCount match {
              case 0 =>
                Conflict("Change rejected. Content probably set to IGNORE by admin\n")
              case _ =>
                Accepted(json.Json.parse(gbifData))
            }
          case None => NoContent
      }
    } catch {
      case e: Exception => e.printStackTrace(); InternalServerError
    } finally {
      db.close()
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
    try {
      val parser: GbifParser = new GbifParser(db)
      val speciesList  = db.listSpecies
      for(species <- speciesList) {
        val gbifData = GbifService.matchName(species.latinName)
        gbifData match {
          case Some(gbifData) =>
            val parsedSpecies:Species = parser.parse(species, gbifData)
            // println(parsedSpecies)
            db.updateGbifData(parsedSpecies)
          case None =>
            println(species.latinName + " not found")
        }
      }
      Accepted
    } catch {
      case e: Exception => e.printStackTrace(); InternalServerError
    } finally {
      db.close()
    }
  }
}

