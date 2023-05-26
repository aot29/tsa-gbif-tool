package services

import com.typesafe.config.ConfigFactory

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, Statement, Types}

trait ConnectionFactory {
  def getConnection: Connection
}

class PersistentConnectionFactory extends ConnectionFactory {
  /**
   * Opens a connection to the database.
   *
   * @return SQL connection
   */
  def getConnection: Connection = {
    val config = ConfigFactory.load()
    val driver = config.getString("db.driver")
    val url = config.getString("db.url")
    val username = config.getString("db.user")
    val password = config.getString("db.password")

    Class.forName(driver)
    DriverManager.getConnection(url, username, password)
  }
}