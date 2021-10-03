package lokallykke.db


import scala.concurrent.Await
import scala.concurrent.duration._
import lokallykke.model.session.{AuthenticationProcess, UserSession}
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import java.sql.Timestamp

trait SessionHandler {
  val profile : JdbcProfile
  import profile.api._
  val db : Database
  implicit val dt = 10.seconds

  private val logger = LoggerFactory.getLogger(this.getClass)

  val tables : Tables

  protected val insertSession = tables.Session.sessions returning tables.Session.sessions.map(_.sessionid) into ((sess, id) => sess.copy(sessionId = id))

  def createSession(ip : String): Long = {
    val started = new Timestamp(System.currentTimeMillis)
    val inserted = Await.result(db.run(insertSession += UserSession(-1L, ip, started)), 10.seconds)
    inserted.sessionId
  }

  def isAuthorized(sessionId : Long, ip : String) : Boolean = {
    val now = new Timestamp(System.currentTimeMillis())
    val query = (tables.Session.sessions.filter(s => s.sessionid === sessionId && s.ip === ip) join tables.Session.processes.filter(p => p.validUntil.isDefined && p.validUntil > now) on {_.sessionid === _.sessionId}).map {
      case (sess, procs) => sess.sessionid
    }
    Await.result(db.run(query.result), 10.seconds).headOption match {
      case Some(_) => true
      case _ => false
    }
  }

  def nonceExists(sessionId : Long, nonce : String) : Boolean = {
    Await.result(db.run(tables.Session.processes.filter(p => p.sessionId === sessionId && p.nonce === nonce).map(_.sessionId).result), 5.seconds).headOption match {
      case Some(_) => true
      case _ => false
    }

  }

  def initiateAuthenticationProcess(sessionId : Long, nonce : String, state : String, forwardUrl : String) : Unit = {
    val insertee = AuthenticationProcess(sessionId, nonce, state, forwardUrl, None, None)
    Await.result(db.run(tables.Session.processes += insertee), 10.seconds)
  }

  def validateNonceAndState(sessionId : Long, nonce : String, state : String) : Boolean = {
    val query = tables.Session.processes.filter(p => p.sessionId === sessionId && p.nonce === nonce && p.state === state)
    Await.result(db.run(query.result), 10.seconds).headOption match {
      case Some(_) => true
      case _ => false
    }
  }

  def finalizeProcess(sessionId : Long, nonce : String, email : String, validUntil : Timestamp) : Unit = {
    val updateStat = tables.Session.processes.filter(p => p.sessionId === sessionId && p.nonce === nonce).map(p => (p.email, p.validUntil)).update((Some(email), Some(validUntil)))
    Await.result(db.run(updateStat), 10.seconds)
  }



}
