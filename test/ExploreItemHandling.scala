import lokallykke.LocallykkeConfig
import lokallykke.db.Connection
import lokallykke.instagram.{InstagramLoader, LoaderObserver}
import lokallykke.model.items.Item

import java.sql.Timestamp
import scala.concurrent.duration._

object ExploreItemHandling {

  private implicit val dt = 30.seconds

  def main(args: Array[String]): Unit = {
    InstagramLoader.downloadItems()
    /*val resp = InstagramLoader.parseResponse
    println(s"Parsed: ${resp.size} posts")
    resp.foreach(it => println(it))*/

  }

}
