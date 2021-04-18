package lokallykke

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

object LocallykkeConfig {
  lazy val config = ConfigFactory.load("application.conf") /*ConfigFactory.load("production.conf").withFallback(
    ConfigFactory.load("application.conf")
  )*/

  object Db {
    private lazy val dbConfig = config.getConfig("db")
    lazy val url = dbConfig.getString("url")
    lazy val user = dbConfig.getString("user")
    lazy val password = dbConfig.getString("password")

  }

  object Instagram {
    lazy val instaConfig = config.getConfig("instagram")
    lazy val user = instaConfig.getString("user")
    lazy val password = instaConfig.getString("password")
  }


}
