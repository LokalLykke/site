package controllers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import dk.lokallykke.client.util.Selector
import lokallykke.db.Connection

import javax.inject.{Inject, Singleton}
import org.apache.commons.io.{FileUtils, IOUtils}
import play.api.mvc._

import javax.imageio.ImageIO
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

  def croppedItemImage(itemId : Long, height : Int = 100, width : Int = 100) = Action {
    request : Request[AnyContent] => {
      handler.loadItemImage(itemId) match {
        case None => Status(404)
        case Some(bytz) => {
          val inStream = new ByteArrayInputStream(bytz)
          val image = ImageIO.read(inStream)
          inStream.close()
          (image.getHeight, image.getWidth) match {
            case (h,_) if h < height => Ok(bytz)
            case (_,w) if w < width => Ok(bytz)
            case (h, w) => {
              val cropped = image.getSubimage((w - width) / 2, (h - height)/ 2, width, height)
              val outStream = new ByteArrayOutputStream()
              ImageIO.write(cropped, "png", outStream)
              val ret = outStream.toByteArray
              outStream.close()
              Ok(ret)
            }
          }
        }
       }
    }
  }

  def cardItemImage(itemId : Long) = itemImage(itemId)

  def fontFile(fontFamily : String, fileName : String) : Action[AnyContent] = {
    assets.at(s"/fonts/$fontFamily/$fileName")
  }

  def fontLinuxLibertine = fontFile("LinuxLibertine", "LinLibertine_Re-4.1_.8_.woff")
  def fontLinuxLibertineIt = fontFile("LinuxLibertine", "LinLibertine_It-4.0_.3_.woff")
  def fontFarro = fontFile("Farro", "Farro-regular.ttf")
  def fontAmatic = fontFile("AmaticSC", "AmaticSC-Regular.ttf")
  def fontCoda = fontFile("Coda", "Coda-Regular.ttf")
  def fontTangerine = fontFile("Tangerine", "Tangerine-Bold.ttf")





/*Action {
    implicit request : Request[AnyContent] => {
      Ok("Hello")
    }*/

}
