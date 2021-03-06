package lokallykke.db

import lokallykke.model.items.Item
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

trait ItemHandler {

  val table : Tables
  val db : Database
  val profile : JdbcProfile
  import profile.api._

  def insertItems(items : Seq[Item]) = {
  }


}
