package lokallykke.structure

import lokallykke.db.ItemHandler
import play.api.inject.{Module => GuiceModule}

trait Site extends GuiceModule {

  def itemHandler : ItemHandler

}
