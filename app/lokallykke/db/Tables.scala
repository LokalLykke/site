package lokallykke.db

import lokallykke.model.items.Item
import org.slf4j.LoggerFactory
import slick.lifted.Tag
import slick.model.Table
import slick.jdbc
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
      def bytes = column[Array[Byte]]("FILEBYTES")
      def width = column[Option[Int]]("WIDTH")
      def height = column[Option[Int]]("HEIGHT")
      def caption = column[Option[String]]("CAPTION")
      def timestamp = column[Timestamp]("TIMESTAMP")
      def * = (id, instagramId, bytes, width, height, caption, timestamp).<> (Item.tupled, Item.unapply)


    }

  }


}
