package lokallykke.structure
import lokallykke.db.{Connection, ItemHandler}
import play.api.inject.Binding
import play.api.{Configuration, Environment}

import javax.inject.Singleton

@Singleton
class ProductionSite extends Site {
  override def itemHandler: ItemHandler = Connection.h2handler

  override def bindings(environment: Environment, configuration: Configuration): collection.Seq[Binding[_]] = Seq(
    bind[Site].toInstance(this)
  )
}
