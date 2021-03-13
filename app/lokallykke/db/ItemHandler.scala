package lokallykke.db

import lokallykke.model.items.Item
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import lokallykke.helpers.Extensions._

import java.sql.Timestamp

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

  def createItemFromImage(image : Array[Byte]) : Item = {
    val insertee = Item(-1L, None, None, image, None, None, None, new Timestamp(System.currentTimeMillis),None, None, None, None, None )
    Await.result(db.run(insertAndReturnItem += insertee), dt)
  }

  def changeImage(itemId : Long, bytes : Array[Byte], width : Option[Int], height : Option[Int]): Unit = {
    Await.result(db.run(items.filter(_.id === itemId).map(en => (en.bytes, en.width, en.height)).update(bytes, width, height)), dt)
    logger.info(s"Updated image for item with ID: ${itemId}")
  }

  def updateItem(itemId : Long, name : Option[String], caption : Option[String], costValue : Option[Double], askPrice : Option[Double]) = {
    Await.result(db.run(items.filter(_.id === itemId).map(en => (en.name, en.caption, en.costvalue, en.askprice)).update((name, caption, costValue, askPrice))), dt)
    logger.info(s"Updated item with ID: ${itemId} name: ${name.getOrElse("")}  caption: ${caption.getOrElse("")} costvalue : ${costValue.map(_.toPrettyString).getOrElse("")}")
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
