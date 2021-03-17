package lokallykke.db

import lokallykke.model.pages.Image
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

trait PageHandler {
  val profile : JdbcProfile
  import profile.api._
  val db : Database
  private implicit val dt = 30.seconds
  val tables : Tables
  lazy val images = tables.Pages.images
  lazy val pages = tables.Pages.pages
  lazy val pageTags = tables.Pages.pageTags
  lazy val pageContent = tables.Pages.pageContent

  lazy val insertImage = images.returning(images.map(_.id)).into((pag, pid) => pag.copy(id = pid))

  private val logger = LoggerFactory.getLogger(this.getClass)

  def saveImage(bytes : Array[Byte], contentType : String) : Long = {
    val insres = Await.result(db.run(insertImage += Image(-1L, bytes, contentType)), dt)
    insres.id
  }

  def loadImage(imageid : Long) : Image = {
    Await.result(db.run(images.filter(_.id === imageid).result), dt).head
  }

}
