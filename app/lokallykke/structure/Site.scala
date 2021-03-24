package lokallykke.structure

import lokallykke.db.{CustomerPageHandler, ItemHandler, PageHandler}
import play.api.inject.{Module => GuiceModule}

trait Site extends GuiceModule {

  def itemHandler : ItemHandler
  def pageHandler : PageHandler
  def customerPageHandler : CustomerPageHandler

  def init() : Unit = {}

  protected def register() : Unit = {
    Site.currentSite = Some(this)
  }
  init()
  register()

}

object Site {
  protected[structure] var currentSite : Option[Site] = None



}