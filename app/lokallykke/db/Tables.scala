package lokallykke.db

import lokallykke.model.items.Item
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.Duration


trait Tables {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val profile : JdbcProfile
  import profile.api._

  def createTables(db : Database)(implicit duration: Duration) = {
    val schema = Items.schema
    logger.info(s"Will create schema based on following DML")
    schema.createIfNotExistsStatements.foreach(stat => logger.info(stat))
    Await.result(db.run(schema.createIfNotExists),duration)
  }

  object Items {

    lazy val items = TableQuery[ItemTable]

    lazy val schema = items.schema

    class ItemTable(tag : Tag) extends Table[Item](tag, "ITEMS") {
      def id = column[Long]("ITEMID", O.PrimaryKey, O.AutoInc)
      def instagramId = column[Option[Long]]("INSTAID")
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

  }


}
