package lokallykke.structure
import lokallykke.db.{Connection, CustomerPageHandler, ItemHandler, PageHandler}
import play.api.inject.Binding
import play.api.{Configuration, Environment}

import javax.inject.Singleton

@Singleton
class ProductionSite extends Site {
  override def itemHandler: ItemHandler = Connection.h2handler

  override def pageHandler: PageHandler = Connection.h2handler

  override def customerPageHandler: CustomerPageHandler = Connection.h2handler

  override def bindings(environment: Environment, configuration: Configuration): collection.Seq[Binding[_]] = Seq(
    bind[Site].toInstance(this)
  )


}
