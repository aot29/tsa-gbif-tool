package services

import models.Species
import java.sql.Connection

/**
 * Connect to the tsa_data database.
 * Connection settings are in .env,
 * and are passed to the container by docker-compose on service start.
 */
trait DbService {

  /**
   * Lists all the species actually used in the Main table.
   *
   * @return List of Species objects or empty list
   */
  def listSpecies: List[Species]

  /**
   * Cleanups the used_in_main column of the system table.
   *
   * @return Count of rows updated
   */
  def markAllUnused: Int

  /**
   * Marks all species actually present in the Main table
   * as used in the System table (System.used_in_main = true)
   *
   * @param speciesList a list of Species objects
   * @return Count of rows updated
   */
  def markAllUsed(speciesList: List[Species]): Int

  /**
   * Resets all GBIF-related columns in the System table
   * (GBIF_check, GBIF_response, GBIF_usage_key).
   *
   * @return Count of rows updated
   */
  def deleteAllGBIFData(): Int

  /**
   * Save the species data parsed from the GBIF response. Update the family and order, remove DE and EN family and order names.
   *
   * @param species a Species object containing the data parsed from GBIF as returned by the service.GbifParser
   * @return number of rows changed, should be 1, or None if there's an error.
   * */
  def updateGbifData(species: Species): Int

  /**
   * Reads the species lineage as stored in the system table.
   *
   * @param species a Species object containing the latinName of the species to be read from the DB
   * @return a Species object with the lineage of the species filled-in, or None if no corresponding species found in the DB.
   * */
  def setLineage(species: Species): Species

  /**
   * Close any open connections
   */
  def close(): Unit
}
