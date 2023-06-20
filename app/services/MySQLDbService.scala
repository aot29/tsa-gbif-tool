package services

import models.Species
import com.typesafe.config.ConfigFactory

import java.sql.{Connection, DriverManager, PreparedStatement, Statement}
import scala.util.Using.Releasable
import scala.util.{Try, Using}

object MySQLDbService extends DbService {

  /**
   * Opens a connection to the database.
   *
   * @return SQL connection
   */
  private def connection: Connection = {
    val config = ConfigFactory.load()
    val driver = config.getString("db.driver")
    val url = config.getString("db.url")
    val username = config.getString("db.user")
    val password = config.getString("db.password")
    Class.forName(driver)
    DriverManager.getConnection(url, username, password)
  }

  override def listSpecies: Try[List[Species]] = {
    Using(connection) { connection => {
        val query: String = "SELECT UNIQUE species FROM main"
        var result: List[Species] = List()
        val statement: Statement = connection.createStatement
        val rs = statement.executeQuery(query)
        while (rs.next) {
          result = result :+ new Species(rs.getString("species"))
        }
        result
      }
    }
  }

  override def markAllUnused: Try[Int] = {
    Using(connection) { connection => {
      val query: String = "UPDATE system SET used_in_main=false;"
      val statement: Statement = connection.createStatement
      statement.executeUpdate(query)
    }}
  }

  override def markAllUsed(speciesList:List[Species]): Try[Int] = {
    Using(connection) { connection => {
      val query: String = "UPDATE system SET used_in_main=true WHERE Artname = ?;"
      val statement: PreparedStatement = connection.prepareStatement(query)
      var resp = 0
      for (species <- speciesList) {
        statement.setString(1, species.latinName)
        resp += statement.executeUpdate()
      }
      resp
    }}
  }

  override def deleteAllGBIFData(): Try[Int] = {
    Using(connection) { connection => {
      val query: String =
        """
          |UPDATE system SET GBIF_check=NULL, GBIF_response=NULL, GBIF_usage_key=NULL
          |WHERE (GBIF_check IS NULL OR GBIF_check!='IGNORE');""".stripMargin
      val statement: Statement = connection.createStatement
      statement.executeUpdate(query)
    }}
  }

  override def updateGbifData(species:Species): Try[Int] = {
    Using(connection) { connection => {
      val query: String =
        """
          |UPDATE system SET GBIF_check=?, GBIF_response=?, GBIF_usage_key=?,
          |Familia=?, Familie_dt_="", Familia_en="",
          |Ordo=?, Ordnung_dt_=""
          |WHERE Artname=? AND (GBIF_check IS NULL OR GBIF_check!='IGNORE');""".stripMargin
      val statement: PreparedStatement = connection.prepareStatement(query)
      statement.setString(1, species.status.toString)
      statement.setString(2, species.GbifResponse)
      statement.setInt(3, species.GbifUsageKey)
      statement.setString(4, species.familia)
      statement.setString(5, species.ordo)
      statement.setString(6, species.latinName)
      statement.executeUpdate()
    }}
  }

  override def setLineage(species:Species): Try[Species] = {
    Using(connection) { connection => {
      val query: String = "SELECT Artname,Familia,Ordo,Klasse FROM system WHERE Artname=?"
      val statement: PreparedStatement = connection.prepareStatement(query)
      statement.setString(1, species.latinName)
      val rs = statement.executeQuery()
      if (rs.next) {
        species.familia = rs.getString("Familia")
        species.ordo = rs.getString("Ordo")
        species.klasse = rs.getString("Klasse")
      }
      species
    }}
  }
}

