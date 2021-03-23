package controllers

import java.io.File
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import dk.lokallykke.client.util.Selector
import lokallykke.db.Connection

import javax.inject.{Inject, Singleton}
import org.apache.commons.io.{FileUtils, IOUtils}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class LokalLykkeAssets @Inject() (val assets : controllers.Assets, cc : ControllerComponents) extends AbstractController(cc) {

  lazy val handler = Connection.h2handler

  def at(file : String) : Action[AnyContent] = assets.at(file)

  def jsLibrary = assets.at("/","client-fastopt-bundle.js")
  def jsLibraryMap = assets.at("/","client-fastopt-bundle.js.map")

  def itemImage(itemId : Long) = Action {
    request : Request[AnyContent] => {
      handler.loadItemImage(itemId) match {
        case Some(bytz) => Ok(bytz)
        case None => Status(404)
      }
    }
  }

  def fontFile(fontFamily : String, fileName : String) : Action[AnyContent] = {
    assets.at(s"/fonts/$fontFamily/$fileName")
  }

  def fontLinuxLibertine = fontFile("LinuxLibertine", "LinLibertine_Re-4.1_.8_.woff")
  def fontLinuxLibertineIt = fontFile("LinuxLibertine", "LinLibertine_It-4.0_.3_.woff")





/*Action {
    implicit request : Request[AnyContent] => {
      Ok("Hello")
    }*/

}
