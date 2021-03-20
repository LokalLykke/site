package lokallykke.db

import lokallykke.model.items.{Item, ItemTag}
import lokallykke.model.pages.{Image, Page, PageContent, PageContentItem, PageTag}
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.Duration


trait Tables {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val profile : JdbcProfile
  import profile.api._

  def createItemTables(db : Database)(implicit duration: Duration) = {
    val schema = Items.schema
    logger.info(s"Will create schema based on following DML")
    schema.createIfNotExistsStatements.foreach(stat => logger.info(stat))
    Await.result(db.run(schema.createIfNotExists),duration)
  }
  def createPageTables(db : Database)(implicit duration: Duration) = {
    val schema = Pages.schema
    logger.info(s"Will create schema based on following DML")
    schema.createIfNotExistsStatements.foreach(stat => logger.info(stat))
    Await.result(db.run(schema.createIfNotExists),duration)
  }


  object Items {

    lazy val items = TableQuery[ItemTable]
    lazy val tags = TableQuery[ItemTagTable]

    lazy val schema = items.schema ++ tags.schema

    class ItemTable(tag : Tag) extends Table[Item](tag, "ITEMS") {
      def id = column[Long]("ITEMID", O.PrimaryKey, O.AutoInc)
      def instagramId = column[Option[String]]("INSTAID")
      def name = column[Option[String]]("ITEMNAME")
      def bytes = column[Array[Byte]]("FILEBYTES")
      def width = column[Option[Int]]("WIDTH")
      def height = column[Option[Int]]("HEIGHT")
      def caption = column[Option[String]]("CAPTION")
      def registered = column[Timestamp]("REGISTERED")
      def costvalue = column[Option[Double]]("COSTVALUE")
      def soldat = column[Option[Timestamp]]("SOLDAT")
      def soldvalue = column[Option[Double]]("SOLDVALUE")
      def deletedAt = column[Option[Timestamp]]("DELETEDAT")
      def askprice = column[Option[Double]]("ASKPRICE")
      def * = (id, instagramId, name, bytes, width, height, caption, registered, costvalue, soldat, soldvalue, deletedAt, askprice).<> (Item.tupled, Item.unapply)

      def indxInsta = index("IDX_ITEM_INSID", (instagramId), false)
    }

    class ItemTagTable(tag : Tag) extends Table[ItemTag](tag, "ITEMTAGS"){
      def itemid = column[Long]("ITEMID")
      def tagname = column[String]("TAG")
      def * =(itemid, tagname) <> (ItemTag.tupled, ItemTag.unapply)

      def pk = primaryKey("PK_ITEMTAGS", (itemid, tagname))
      def fkItem = foreignKey("FK_ITEMTAGS_ITEMID", (itemid), items)(_.id, onDelete = ForeignKeyAction.Cascade)
    }

  }

  object Pages {

    val pages = TableQuery[PageTable]
    val pageTags = TableQuery[PageTagTable]
    val images = TableQuery[ImageTable]
    val pageContent = TableQuery[PageContentTable]
    val contentItems = TableQuery[PageContentItemsTable]


    val schema = pages.schema ++ pageTags.schema ++ images.schema ++ pageContent.schema ++ contentItems.schema

    class PageTable(tag : Tag) extends Table[Page](tag, "PAGES"){
      def id = column[Long]("PAGEID", O.AutoInc)
      def name = column[String]("PAGENAME")
      def description = column[Option[String]]("DESCRIPTION")
      def isdeleted = column[Int]("ISDELETED")
      def * = (id, name, description, isdeleted) <> (Page.tupled, Page.unapply)

      def pk = primaryKey("PK_PAGES", (id))
    }

    class PageTagTable(tag : Tag) extends Table[PageTag](tag, "PAGETAGS") {
      def pageid = column[Long]("PAGEID")
      def tagname = column[String]("TAGNAME")
      def * = (pageid, tagname) <> (PageTag.tupled, PageTag.unapply)

      def fkPage = foreignKey("FK_PAGETAGS_PAGE", (pageid), pages)(_.id, onDelete = ForeignKeyAction.Cascade)
      def indxPage = index("IDX_PAGETABS_PAGE", (pageid), unique = false)
    }

    class ImageTable(tag : Tag) extends Table[Image](tag, "PAGEIMAGES") {
      def id = column[Long]("IMAGEID", O.AutoInc)
      def bytes = column[Array[Byte]]("IMAGEBYTES")
      def contenttype = column[String]("CONTENTTYPE")
      def * = (id, bytes, contenttype) <> (Image.tupled, Image.unapply)

      def pk = primaryKey("PK_PAGEIMAGES", (id))
    }

    class PageContentTable(tag : Tag) extends Table[PageContent](tag, "PAGECONTENT") {
      def id = column[Long]("CONTENTID", O.AutoInc)
      def pageid = column[Long]("PAGEID")
      def indx = column[Int]("INDX")
      def contenttype = column[String]("CONTENTTYPE")
      def parentid = column[Option[Long]]("PARENTID")
      def text = column[Option[String]]("TEXT")
      def style = column[Option[String]]("STYLE")
      def level = column[Option[Int]]("LEVEL")
      def imageid = column[Option[Long]]("IMAGEID")
      def caption = column[Option[String]]("CAPTION")
      def withborder = column[Option[Int]]("BORDER")
      def stretched = column[Option[Int]]("STRETCHED")
      def withbackground = column[Option[Int]]("BACKGROUND")
      def * = (id, pageid, indx, contenttype, parentid, text, style, level, imageid, caption, withborder, stretched, withbackground) <> (PageContent.tupled, PageContent.unapply)

      def pk = primaryKey("PK_PAGECONTENT", (id))
      def fkPage = foreignKey("FK_PAGECONTENT_PAGE", (pageid), pages)(_.id, onDelete = ForeignKeyAction.Cascade)
    }

    class PageContentItemsTable(tag : Tag) extends Table[PageContentItem](tag, "PAGECONTIT") {
      def contentid = column[Long]("CONTENTID")
      def indx = column[Int]("INDX")
      def text = column[String]("TEXT")
      def * = (contentid, indx, text) <> (PageContentItem.tupled, PageContentItem.unapply)

      def pk = primaryKey("PK_PAGECONTIT", (contentid, indx))
      def fkCont = foreignKey("FK_PAGECONTIT", (contentid), pageContent)(_.id, onDelete = ForeignKeyAction.Cascade)
    }

  }


}
