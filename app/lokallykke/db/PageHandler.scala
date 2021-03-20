package lokallykke.db

import lokallykke.db.PageHandler.PageResults
import lokallykke.model.pages.{Image, Page, PageContent, PageContentItem, PageTag}
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
  lazy val contentItems = tables.Pages.contentItems

  lazy val insertImage = images.returning(images.map(_.id)).into((img, pid) => img.copy(id = pid))
  lazy val insertPage = pages.returning(pages.map(_.id)).into((pag, pid) => pag.copy(id = pid))
  lazy val insertContent = pageContent.returning(pageContent.map(_.id)).into((pc, pid) => pc.copy(id = pid))

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def livePages = pages.filter(_.isdeleted === 0)

  def saveImage(bytes : Array[Byte], contentType : String) : Long = {
    val insres = Await.result(db.run(insertImage += Image(-1L, bytes, contentType)), dt)
    insres.id
  }

  def loadImage(imageid : Long) : Image = {
    Await.result(db.run(images.filter(_.id === imageid).result), dt).head
  }

  def loadPageIdAndNames = {
    Await.result(db.run(livePages.map(en => (en.id, en.name)).result), dt)
  }

  def loadPage(pageId : Long) : Option[PageResults.LoadResult] = {
    Await.result(db.run(pages.filter(_.id === pageId).result), dt).headOption.map {
      case pag => {
        val tags = Await.result(db.run(pageTags.filter(_.pageid === pageId).result), dt)
        val content = Await.result(db.run(pageContent.filter(_.pageid === pageId).result), dt)
        val contentItems = Await.result(db.run(contentItems.filter(ci => ci.contentid.inSetBind(content.map(_.id).distinct)).result), dt)
        PageResults.LoadResult(pag, tags, content, contentItems)
      }
    }
  }

  def savePage(page : Page, tags : Seq[PageTag], content : Seq[(PageContent, Seq[String])]) : Unit = {
    val refPageId = if(page.id < 0) {
      val retPage = Await.result(db.run(insertPage += page), dt)
      retPage.id
    }
    else {
      Await.result(db.run(pageTags.filter(_.pageid === page.id).delete), dt)
      Await.result(db.run(pageContent.filter(_.pageid === page.id).delete), dt)
      Await.result(db.run(pages.filter(_.id === page.id).map(p => (p.name, p.description)).update((page.name, page.description))), dt)
      page.id
    }
    Await.result(db.run(pageTags ++= tags.map(t => t.copy(pageid = refPageId))), dt)
    val insertedContent = Await.result(db.run(insertContent ++= content.map(c => c._1.copy(pageid = refPageId))) , dt)
    val contentItemsToInsert = content.zip(insertedContent).flatMap {
      case ((_,items),updCont) => items.zipWithIndex.map(it => PageContentItem(updCont.id, it._2, it._1))
    }
    Await.result(db.run(contentItems ++= contentItemsToInsert), dt)
    logger.info(s"Inserted page: ${page.name} with ID: ${page.id}, ${tags.size} and ${content.size} content")
  }

  def deletePage(pageId : Long) : Unit = {
    Await.result(db.run(pages.filter(_.id === pageId).map(p => p.isdeleted).update(1)), dt)

  }
}

object PageHandler {
  object PageResults {
    case class LoadResult(page : Page, tags : Seq[PageTag], content : Seq[PageContent], contentItems : Seq[PageContentItem])
  }

}

