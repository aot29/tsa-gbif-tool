package services

import com.typesafe.config.ConfigFactory
import requests.Response

import java.net.URLEncoder

/**
 * GBIF API client
 * @see https://www.gbif.org/developer/species
 */
object GbifService {
  private val GBIF_URL = "https://api.gbif.org/v1"
  private val SPECIES_MATCH_URL = GBIF_URL + "/species/match"
  private val config = ConfigFactory.load()

  /**
   * Search the GBIF backbone for a name. Animals only.
   * Uses the /species/match endpoint.
   *
   * @param name latin name of a species
   * @return JSON string
   */
  def matchName(name:String):Option[String] = {
    val params = Map(
      "verbose" -> "true",
      "kingdom" -> "Animalia",
      "name" -> URLEncoder.encode(name.replace(' ', '_'), "UTF-8")
    )
    val response:Response = get(params)
    Some(response.text())
  }

  /**
   * Executes a request, with or without proxy.
   * Proxy settings are passed from .env to conf/application.conf
   * Remember to do `export $(xargs <.env)` prior to `sbt run` when running from CLI.
   * When running with Docker, .env is read on `docker-compose up -d`.
   */
  private def get(params:Map[String, String]): Response = {
    if (config.hasPath("http.proxy") & config.getString("http.proxy").nonEmpty) {
      val proxy = config.getString("http.proxy").split(':')
      val proxy_url = proxy(1).split("//")(1)
      val proxy_port = proxy(2).toInt
      requests.get(SPECIES_MATCH_URL, params = params, proxy = (proxy_url, proxy_port))
    } else {
      requests.get(SPECIES_MATCH_URL, params = params)
    }
  }

}
