package lokallykke.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import lokallykke.LocallykkeConfig
import lokallykke.model.items.Item
import scala.concurrent.duration._


import scala.concurrent.Await

object Connection {

  lazy val ds = {
    val conf = LocallykkeConfig.Db
    new HikariDataSource(new HikariConfig {
      setJdbcUrl(conf.url)
      setUsername(conf.user)
      setPassword(conf.password)
      setMaximumPoolSize(5)
    })
  }

  lazy val h2handler = new H2Handler
  lazy val postgresHandler = new PostgresHandler



  class H2Handler extends Tables with ItemHandler with PageHandler with CustomerPageHandler {
    import slick.jdbc.H2Profile.api._
    private implicit val dt = 30.seconds

    override val tables: Tables = this
    override val db = Database.forDataSource(ds, Some(5)).asInstanceOf[slick.jdbc.JdbcBackend.Database]
    override val profile = slick.jdbc.H2Profile

    override def existingInstagramIds(seq: Seq[Item]): Set[String] = {
      val instaIds = seq.map(_.instagramId).collect({case Some(id) => id})
      (instaIds.grouped(900).map {
        case grp => Await.result(db.run(items.filter(_.instagramId.inSetBind(grp)).map(_.instagramId).result), dt)
      }).flatten.map(_.get).toSet
    }
  }

  class PostgresHandler extends Tables with ItemHandler with PageHandler with CustomerPageHandler {
    import slick.jdbc.PostgresProfile.api._
    private implicit val dt = 30.seconds

    override val tables: Tables = this
    override val db = Database.forDataSource(ds, Some(5)).asInstanceOf[slick.jdbc.JdbcBackend.Database]
    override val profile = slick.jdbc.PostgresProfile

    override def existingInstagramIds(seq: Seq[Item]): Set[String] = {
      val instaIds = seq.map(_.instagramId).collect({case Some(id) => id})
      (instaIds.grouped(900).map {
        case grp => Await.result(db.run(items.filter(_.instagramId.inSetBind(grp)).map(_.instagramId).result), dt)
      }).flatten.map(_.get).toSet
    }


  }


}
