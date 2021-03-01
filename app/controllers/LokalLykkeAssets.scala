package controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import play.api.mvc._

class LokalLykkeAssets @Inject() (val assets : controllers.Assets, cc : ControllerComponents) {

  def at(file : String) : Action[AnyContent] = assets.at(file)

  def accountingJS : Action[AnyContent] = {
    /*val clientFile = this.getClass.getClassLoader.getResourceAsStream("lokallykke-client-fastopt.js")
    val bytes = IOUtils.toByteArray(clientFile)*/
    val ret = assets.at("c:/git/lokallykke-site/target/scala-2.13/classes/lokallykke-client-fastopt.js")
    assets.at("c:/git/lokallykke-site/target/scala-2.13/classes/lokallykke-client-fastopt.js")
  }

}
