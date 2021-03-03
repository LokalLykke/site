package controllers

import java.io.File

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import javax.inject.Inject
import org.apache.commons.io.{FileUtils, IOUtils}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class LokalLykkeAssets @Inject() (val assets : controllers.Assets, cc : ControllerComponents) extends AbstractController(cc) {

  def at(file : String) : Action[AnyContent] = assets.at(file)

  def jsLibrary = Action {
    implicit request : Request[AnyContent] => {
      val bytes = FileUtils.readFileToByteArray(new File("./client/target/scala-2.13", "client-fastopt.js"))
      Ok(bytes)
      //Ok(new String(bytes, "UTF-8")).withHeaders(("Content-Type", "text/javascript"))
    }

  }

  //assets.at("./client/target/scala-2.13", "client-fastopt.js")

  def accountingJS : Action[AnyContent] = Action {
    implicit request : Request[AnyContent] => {
      implicit val executionContext = ExecutionContext.global
      //val clientFile = this.getClass.getClassLoader.getResourceAsStream("lokallykke-dk.lokallykke.client-fastopt.js")
      //val bytes = IOUtils.toByteArray(clientFile)
      Ok.sendResource("application.conf")
    }
  }

/*Action {
    implicit request : Request[AnyContent] => {
      Ok("Hello")
    }*/

}
