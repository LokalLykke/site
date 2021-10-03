package lokallykke.structure

import lokallykke.db.{CustomerPageHandler, ItemHandler, PageHandler, SessionHandler}
import play.api.inject.{Module => GuiceModule}

trait Site extends GuiceModule {

  def itemHandler : ItemHandler
  def pageHandler : PageHandler
  def customerPageHandler : CustomerPageHandler
  def sessionHandler : SessionHandler

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