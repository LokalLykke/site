package lokallykke.db

import lokallykke.model.items.Item
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

trait ItemHandler {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val tables : Tables
  lazy val items = tables.Items.items
  val db : Database
  val profile : JdbcProfile
  import profile.api._
  private implicit val dt = 30.seconds

  def existingInstagramIds(seq : Seq[Item]) : Set[Long]

  private lazy val insertAndReturnItem = items.returning(items.map(_.id)).into((it, id) => it.copy(id = id))

  lazy val liveItems = items.filter(_.deletedAt.isEmpty)

  def insertItems(items : Seq[Item]) : Seq[Item] = {
    val existInstaIds = existingInstagramIds(items)
    val insertees = items.filter(it => it.id < 0L && it.instagramId.map(insId =>  !existInstaIds(insId)).getOrElse(true))
    val ret = Await.result(db.run(insertAndReturnItem ++= insertees), dt)
    logger.info(s"Inserted ${insertees.size} items")
    ret
  }

  def changeImage(itemId : Long, bytes : Array[Byte], width : Option[Int], height : Option[Int]): Unit = {
    Await.result(db.run(items.filter(_.id === itemId).map(en => (en.bytes, en.width, en.height)).update(bytes, width, height)), dt)
    logger.info(s"Updated image for item with ID: ${itemId}")
  }

  def changeCaption(itemId : Long, caption : Option[String]) = {
    Await.result(db.run(items.filter(_.id ===itemId).map(_.caption).update(caption)), dt)
    logger.info(s"Changed caption for item with ID: ${itemId} to: ${caption.getOrElse("")}")
  }

  def loadItems(includeSold : Boolean = false) : Seq[Item] = {
    Await.result(db.run(liveItems.filter(en => en.soldat.isEmpty || includeSold).result), dt)
  }

  def lookupByInstaId(instaId : Long) : Option[Item] = {
    Await.result(db.run(items.filter(_.instagramId === instaId).result), dt).headOption
  }

  def loadItemImage(itemId : Long) : Option[Array[Byte]] = {
    Await.result(db.run(items.filter(_.id === itemId).map(_.bytes).result), dt).headOption
  }



}
