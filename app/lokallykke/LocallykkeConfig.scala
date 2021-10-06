package lokallykke

import com.typesafe.config.ConfigFactory
import scala.jdk.CollectionConverters._

object LocallykkeConfig {
  lazy val config = ConfigFactory.load("production.conf").withFallback(
    ConfigFactory.load("application.conf")
  )

  lazy val secretKey = config.getString("play.http.secret.key")

  lazy val adminUsers = config.getStringList("admin-users").asScala.toList

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

  object OpenID {
    lazy val openIDConfg = config.getConfig("open-id")
    lazy val clientId = openIDConfg.getString("client-id")
    lazy val secret = openIDConfg.getString("client-secret")
  }


}
