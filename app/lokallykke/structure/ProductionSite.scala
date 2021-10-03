package lokallykke.structure
import lokallykke.db.{Connection, CustomerPageHandler, ItemHandler, PageHandler, SessionHandler}
import play.api.inject.Binding
import play.api.{Configuration, Environment}

import javax.inject.Singleton

@Singleton
class ProductionSite extends Site {
  override def itemHandler: ItemHandler = Connection.postgresHandler
  override def pageHandler: PageHandler = Connection.postgresHandler
  override def customerPageHandler: CustomerPageHandler = Connection.postgresHandler
  override def sessionHandler : SessionHandler = Connection.postgresHandler

  override def bindings(environment: Environment, configuration: Configuration): collection.Seq[Binding[_]] = Seq(
    bind[Site].toInstance(this)
  )


}
