package lokallykke.structure

import lokallykke.db.{ItemHandler, PageHandler}
import play.api.inject.{Module => GuiceModule}

trait Site extends GuiceModule {

  def itemHandler : ItemHandler
  def pageHandler : PageHandler

}
