package lokallykke.db

import dk.lokallykke.client.viewmodel.customer.{CustomerItem, CustomerPage, CustomerPageContent}
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import lokallykke.helpers.Extensions._


trait CustomerPageHandler {

  val profile : JdbcProfile
  import profile.api._
  val db : Database

  private val logger = LoggerFactory.getLogger(this.getClass)
  private implicit val dt = 30.seconds

  val tables : Tables
  private lazy val items = tables.Items.items
  private lazy val itemTags = tables.Items.tags
  private lazy val pages = tables.Pages.pages
  private lazy val pageTags = tables.Pages.pageTags
  private lazy val pageContent = tables.Pages.pageContent
  private lazy val pageContentItems = tables.Pages.contentItems
  private lazy val pageImages = tables.Pages.images

  private lazy val livePages = pages.filter(_.isdeleted === 0)
  private lazy val liveItems = items.filter(_.deletedAt.isEmpty)

  def loadCustomerPages = {
    val retPages = Await.result(db.run(livePages.result), dt)

    val qPagTags = (livePages join pageTags on {_.id === _.pageid})
    val qItTag = liveItems join itemTags on  {_.id === _.itemid}
    val taggedItemsQuery = for {
      ((pag, pagTag), (it, itTag)) <- qPagTags join qItTag on {_._2.tagname === _._2.tagname}
    } yield (pag.id, it.id, itTag.tagname)
    val taggedItems = Await.result(db.run(taggedItemsQuery.result), dt)

    val contentImageQuery = for {
      (pag, cont) <- livePages join pageContent on {_.id === _.pageid}
      (im) <- pageImages.filter(_.id === cont.imageid)
    } yield (pag.id, cont.indx, im.id)

    val contentImages = Await.result(db.run(contentImageQuery.result), dt)
    val imageByContent = contentImages.groupBy(_._1).map(p => p._1 -> p._2.minBy(_._2)._3)

    val onlyInOne = taggedItems.groupBy(_._2).filter(_._2.size == 1).map(_._1).toSet
    val pageItemMap = taggedItems.groupBy(_._1)
    val imagedPages = scala.collection.mutable.HashMap.empty[Long, Long]

    retPages.filter(pag => !imagedPages.contains(pag.id)).foreach {
      case pag => {
        var foundItem : Option[Long] = None
        pageItemMap.getOrElse(pag.id, Nil).foreach {
          case (_, itemId, label) => {
            if(onlyInOne(itemId))
              foundItem = Some(itemId)
          }
        }
        foundItem.foreach(it => imagedPages(pag.id) = it)
      }
    }
    retPages.filter(pag => !imagedPages.contains(pag.id)).foreach {
      case pag => {
        pageItemMap.getOrElse(pag.id, Nil).headOption.foreach(h => imagedPages(pag.id) = h._2)
      }
    }

    retPages.sortBy(_.name).map {
      case pag => {
        val imageUrl = (imageByContent.get(pag.id), imagedPages.get(pag.id)) match {
          case (Some(imgId), _) => controllers.routes.PagesController.loadImage(imgId).url
          case (_, Some(imgId)) => controllers.routes.LokalLykkeAssets.croppedItemImage(imgId).url
          case _ => controllers.routes.LokalLykkeAssets.at("images/no-image.jpg").url
        }
        CustomerPage(pag.id, pag.name, controllers.routes.CustomerPageController.page(pag.id).url, pag.description.getOrElse(pag.name), imageUrl)
      }
    }

  }

  def loadCustomerPageAndContent(pageId : Long) = {
    loadCustomerPages.find(_.id == pageId).map {
      case pag => {
        val contents = Await.result(db.run(pageContent.filter(_.pageid === pageId).result), dt)
        val contentListQuery = (pageContentItems join pageContent.filter(_.pageid === pageId) on  {_.contentid === _.id}).map(_._1)
        val contentListItems = Await.result(db.run(contentListQuery.result), dt).groupBy(_.pageContentId).map {
          case (cid, ents) => cid -> ents.sortBy(_.indx).map(_.text)
        }
        val customerCont = contents.sortBy(_.indx).map {
          case cont => CustomerPageContent(
            cont.level.map(lev => (cont.text.getOrElse(""), lev)),
            cont.imageid.map(imid => controllers.routes.PagesController.loadImage(imid).url),
            cont.text.filter(_ => cont.contenttype == "paragraph"),
            contentListItems.get(cont.id).filter(_ => cont.style == "ordered"),
            contentListItems.get(cont.id).filter(_ => cont.style != "ordered")
          )
        }
        val items = loadItemsForPage(pag.id)

        (pag, customerCont, items)
      }
    }
  }

  def loadItemsForPage(id : Long) = {
    val itIdQuery = (pageTags.filter(_.pageid === id) join itemTags on {_.tagname === _.tagname}).map(_._2.itemid).distinct
    val query = (liveItems join itIdQuery on {_.id === _}).map(_._1)
    Await.result(db.run(query.result), dt).map {
      case it => CustomerItem(it.id, it.name.getOrElse(""), it.caption, controllers.routes.LokalLykkeAssets.cardItemImage(it.id).url)
    }
  }

}



