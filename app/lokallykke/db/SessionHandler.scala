package lokallykke.db


import scala.concurrent.Await
import scala.concurrent.duration._
import lokallykke.model.session.UserSession
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

trait SessionHandler {
  val profile : JdbcProfile
  import profile.api._
  val db : Database
  implicit val dt = 10.seconds

  private val logger = LoggerFactory.getLogger(this.getClass)

  val tables : Tables

  def loadSession(sessionId : Long) : Option[UserSession] = {
    Await.result(db.run(tables.Session.sessions.filter(_.sessionid === sessionId).result), dt).headOption
  }

}
