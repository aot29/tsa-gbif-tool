package services

import requests.Response
import java.net.URLEncoder

/**
 * GBIF API client
 * @see https://www.gbif.org/developer/species
 */
object GbifService {
  private val GBIF_URL = "https://api.gbif.org/v1"
  private val SPECIES_MATCH_URL = GBIF_URL + "/species/match"

  /**
   * Search the GBIF backbone for a name. Animals only.
   * Uses the /species/match endpoint.
   *
   * @param name latin name of a species
   * @return JSON string
   */
  def matchName(name:String):Option[String] = {
    try {
      val response:Response = requests.get(
        SPECIES_MATCH_URL,
        params = Map(
          "verbose" -> "true",
          "kingdom" -> "Animalia",
          "name" -> URLEncoder.encode(name.replace(' ', '_'), "UTF-8")
        )
      )
      Some(response.text())
    } catch {
      case e: Exception => e.printStackTrace(); None
    }
  }

}
