import lokallykke.db.Connection
import scala.concurrent.duration._

object ExploreDB {
  implicit val dt = 3.minute
  implicit val executionContext = scala.concurrent.ExecutionContext.global

  def main(args: Array[String]): Unit = {
    implicit val handler = Connection.postgresHandler
    handler.createItemTables(handler.db)
    handler.createPageTables(handler.db)
  }

}
