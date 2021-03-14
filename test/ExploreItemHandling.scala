import lokallykke.db.Connection
import lokallykke.instagram.{InstagramLoader, LoaderObserver}
import lokallykke.model.items.Item

import java.sql.Timestamp
import scala.concurrent.duration._

object ExploreItemHandling {

  private implicit val dt = 30.seconds

  def main(args: Array[String]): Unit = {
    val handler = Connection.h2handler
    val instaItems = InstagramLoader.parseResponse(LoaderObserver.Sink)
    instaItems.foreach(it => println(it))

    val converted = instaItems map {
      case it => Item(-1L, Some(it.id), it.bytes, Some(it.width), Some(it.height), it.caption, new Timestamp(System.currentTimeMillis()),
        None, None, None,None, None)
    }

    for (
      withCaption <- converted.filter(_.caption.isDefined);
      instaId <- withCaption.instagramId;
      inDb <- handler.lookupByInstaId(instaId)
    ) {
      handler.changeCaption(inDb.id, withCaption.caption)
    }
    handler.loadItems().foreach(println)

  }

}
