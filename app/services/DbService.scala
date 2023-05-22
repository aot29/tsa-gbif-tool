package services

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, Statement, Types}
import com.typesafe.config.ConfigFactory
import models.{Species, TaxonomicStatus}
import models.TaxonomicStatus._
import play.mvc.Result

/**
 * Connect to the tsa_data database.
 * Connection settings are in .env,
 * and are passed to the container by docker-compose on service start.
 */
object DbService {
  /**
   * Opens a connection to the database.
   *
   * @return SQL connection
   */
  private def getConnection:Connection = {
    val config = ConfigFactory.load()
    val driver = config.getString("db.driver")
    val url = config.getString("db.url")
    val username = config.getString("db.user")
    val password = config.getString("db.password")

    Class.forName(driver)
    DriverManager.getConnection(url, username, password)
  }

  /**
   * Lists all the species actually used in the Main table.
   *
   * @return List of Species objects or None
   */
  def listSpecies:Option[List[Species]] = {
    val query: String = "SELECT UNIQUE species FROM main"
    var result: List[Species] = List()
    var connection: Connection = null
    try {
      connection = getConnection
      val statement: Statement = connection.createStatement
      val rs = statement.executeQuery(query)
      while (rs.next) {
        result = result :+ new Species(rs.getString("species"))
      }
      Some(result)
    } catch {
      case e: Exception => e.printStackTrace(); None
    } finally {
      connection.close()
    }
  }

  /**
   * Cleanups the used_in_main column of the system table.
   *
   * @return Count of rows updated
   */
  def markAllUnused:Option[Int] = {
    val query:String = "UPDATE system SET used_in_main=false;"
    var connection: Connection = null
    try {
      connection = getConnection
      val statement: Statement = connection.createStatement
      Some(statement.executeUpdate(query))
    } catch {
      case e: Exception => e.printStackTrace(); None
    } finally {
      connection.close()
    }
  }

  /**
   * Marks all species actually present in the Main table
   * as used in the System table (System.used_in_main = true)
   *
   * @param speciesList a list of Species objects
   * @return True if succeeded, false otherwise
   */
  def markAllUsed(speciesList:List[Species]):Boolean = {
    val query:String = "UPDATE system SET used_in_main=true WHERE Artname = ?;"
    var connection: Connection = null
    try {
      connection = getConnection
      val statement: PreparedStatement = connection.prepareStatement(query)
      for (species <- speciesList) {
        statement.setString(1, species.latinName)
        Some(statement.executeUpdate())
      }
      true
    } catch {
      case e: Exception => e.printStackTrace(); false
    } finally {
      connection.close()
    }
  }

  /**
   * Resets all GBIF-related columns in the System table
   * (GBIF_check, GBIF_response, GBIF_usage_key).
   *
   * @return Count of rows updated
   */
  def deleteAllGBIFData():Option[Int] = {
    val query:String =
      """
        |UPDATE system SET GBIF_check=NULL, GBIF_response=NULL, GBIF_usage_key=NULL
        |WHERE (GBIF_check IS NULL OR GBIF_check!='IGNORE');""".stripMargin
    var connection: Connection = null
    try {
      connection = getConnection
      val statement: Statement = connection.createStatement
      Some(statement.executeUpdate(query))
    } catch {
      case e: Exception => e.printStackTrace(); None
    } finally {
      connection.close()
    }
  }

  /**
   * Save the species data parsed from the GBIF response. Update the family and order, remove DE and EN family and order names.
   *
   * @param species a Species object containing the data parsed from GBIF as returned by the service.GbifParser
   * @return number of rows changed, should be 1, or None if there's an error.
   * */
  def updateGbifData(species:Species): Option[Int] = {
    val query:String =
      """
        |UPDATE system SET GBIF_check=?, GBIF_response=?, GBIF_usage_key=?,
        |Familia=?, Familie_dt_="", Familia_en="",
        |Ordo=?, Ordnung_dt_=""
        |WHERE Artname=? AND (GBIF_check IS NULL OR GBIF_check!='IGNORE');""".stripMargin
    var connection: Connection = null
    try {
      connection = getConnection
      val statement: PreparedStatement = connection.prepareStatement(query)
      statement.setString(1, species.status.toString)
      statement.setString(2, species.GbifResponse)
      statement.setInt(3, species.GbifUsageKey)
      statement.setString(4, species.familia)
      statement.setString(5, species.ordo)
      statement.setString(6, species.latinName)
      Some(statement.executeUpdate())
    } catch {
      case e: Exception => e.printStackTrace(); None
    } finally {
      connection.close()
    }
  }

  /**
   * Reads the species lineage as stored in the system table.
   *
   * @param species a Species object containing the latinName of the species to be read from the DB
   * @return a Species object with the lineage of the species filled-in, or None if no corresponding species found in the DB.
   * */
  def setLineage(species:Species):Option[Species] = {
    val query:String = "SELECT Artname,Familia,Ordo,Klasse FROM system WHERE Artname=?"
    var connection: Connection = null
    try {
      connection = getConnection
      val statement: PreparedStatement = connection.prepareStatement(query)
      statement.setString(1, species.latinName)
      val rs = statement.executeQuery()
      if (rs.next) {
        species.familia = rs.getString("Familia")
        species.ordo = rs.getString("Ordo")
        species.klasse = rs.getString("Klasse")
      }
      Some(species)
    } catch {
      case e: Exception => e.printStackTrace ();
      None
    } finally {
      connection.close ()
    }
  }
}

