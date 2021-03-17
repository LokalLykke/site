import lokallykke.db.Connection

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.global

object ExplorePageHandling {
  private implicit val dt = 30.seconds

  def main(args: Array[String]): Unit = {
    val handler = Connection.h2handler
    handler.createPageTables(handler.db)

  }


}
