package lokallykke.db

import lokallykke.model.items.{Item, ItemTag}
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import lokallykke.helpers.Extensions._

import java.sql.{Time, Timestamp}

trait ItemHandler {
  val profile : JdbcProfile
  import profile.api._
  val db : Database

  private val logger = LoggerFactory.getLogger(this.getClass)

  val tables : Tables
  lazy val items = tables.Items.items
  lazy val tags = tables.Items.tags
  lazy val liveItems = items.filter(_.deletedAt.isEmpty)
  private implicit val dt = 30.seconds

  def existingInstagramIds(seq : Seq[Item]) : Set[String]

  private lazy val insertAndReturnItem = items.returning(items.map(_.id)).into((it, id) => it.copy(id = id))


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

  def createItem(instagramId : Option[String], image : Array[Byte], name : Option[String], caption : Option[String], costVal : Option[Double], askPrice : Option[Double]) : Item = {
    val insertee = Item(-1L, instagramId, name, image, None, None, caption, new Timestamp(System.currentTimeMillis),costVal, None, None, None, askPrice)
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

  def loadItems(itemIds : Seq[Long]) : Seq[Item] = {
    val query = (itemIds.grouped(500).map {
      case (grp) => liveItems.filter(en => en.id.inSetBind(grp))
    }).reduce(_ union _)
    Await.result(db.run(query.result), dt)
  }

  def lookupByInstaId(instaId : String) : Option[Item] = {
    Await.result(db.run(items.filter(_.instagramId === instaId).result), dt).headOption
  }

  def loadItemImage(itemId : Long) : Option[Array[Byte]] = {
    Await.result(db.run(items.filter(_.id === itemId).map(_.bytes).result), dt).headOption
  }

  def deleteItem(itemId : Long) : Unit = {
    Await.result(db.run(items.filter(en => en.soldvalue.isEmpty && en.id === itemId).map(_.deletedAt).update(Some(new Timestamp(System.currentTimeMillis())))), dt)
  }

  def distinctInstagramIds : Set[String] = {
    Await.result(db.run(liveItems.filter(_.instagramId.isDefined).map(_.instagramId.get).result), dt).toSet
  }

  def loadTagsFor(itemids : Seq[Long]) : Seq[ItemTag] = {
    val query = itemids.grouped(500).map(grp => tags.filter(t => t.itemid.inSetBind(grp))).reduce(_ union _)
    Await.result(db.run(query.result), dt)
  }

  def loadDistinctTags : Seq[String] = {
    Await.result(db.run(tags.map(_.tagname).distinct.sortBy(x => x).result), dt)
  }

  def updateTagsFor(itemId : Long, tagnames : Seq[String]) = {
    Await.result(db.run(tags.filter(_.itemid === itemId).delete),dt)
    val insertees = tagnames.map(t => ItemTag(itemId, t))
    Await.result(db.run(tags ++= insertees), dt)
  }

  def loadItemsMatchingTags(filterTags : Seq[String]) = {
    val query = if(filterTags.isEmpty) {
      liveItems
    }
    else {
      val itemRels = tags.filter(tag => tag.tagname.inSetBind(filterTags)).map(_.itemid).distinct
      (liveItems join itemRels on {_.id === _}).map(_._1)
    }
    Await.result(db.run(query.result), dt)
  }



}
