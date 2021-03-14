package dk.lokallykke.client.util.tables

import dk.lokallykke.client.util.CommonUtil
import org.querki.jquery.{EventHandler, JQueryEventObject}
import dk.lokallykke.client.util.JsExtensions._

import java.time.LocalDateTime
import java.util.Base64
import scala.scalajs.js.{Any, Date}


abstract class Column[A, B] {
  val id : String
  val name : String
  val isButton : Boolean = false
  def value(a : A) : Option[B]
  def onClick(a : A) : Option[EventHandler]
  def imageUrl(a : A) : Option[String] = None
  def format(b : B) : Option[String]
  def stringValue(a : A) : String = (for(b <- value(a); str <- format(b)) yield str).getOrElse("")
  def doCreate(a : A) : Boolean = true
  def classes(a : A) : Option[String] = None
  def onButtonPressed : Option[(JQueryEventObject, A) => Any] = None
  def dataString(a : A) : Option[String] = None
}


object Column {
  abstract class StringColumn[A] extends Column[A,String] {override def format(b : String) = Some(b)}
  abstract class DoubleColumn[A] extends Column[A,Double] {override def format(d : Double) = Some(d.toPrettyString)}
  abstract class DateColumn[A] extends Column[A,Date] {override def format(d : Date) = Some(d.toDateString)}
  abstract class DateTimeColumn[A] extends Column[A,LocalDateTime] {override def format(d : LocalDateTime) = Some(d.toDateTimeString)}
  abstract class ImageColumn[A] extends Column[A, String] {override def format(str : String) = None}
  abstract class ButtonColumn[A] extends Column[A, String] {override def format(str : String) = Some(str); override val isButton: Boolean = true}


  object StringColumn {
    def apply[A](inId : String, inName : String, inValue : A => Option[String], inOnClick : Option[EventHandler] = None) = {
      new StringColumn[A] {
        override val id = inId
        override val name = inName
        override def value(a: A): Option[String] = inValue(a)
        override def onClick(a : A) : Option[EventHandler] = inOnClick
      }
    }
  }

  object DoubleColumn {
    def apply[A](inId : String, inName : String, inValue : A => Option[Double], inOnClick : Option[EventHandler] = None) = {
      new DoubleColumn[A] {
        override val id = inId
        override val name = inName
        override def value(a: A): Option[Double] = inValue(a)
        override def onClick(a : A) : Option[EventHandler] = inOnClick
      }
    }
  }

  object DateColumn {
    def apply[A](inId : String, inName : String, inValue : A => Option[Date], inOnClick : Option[EventHandler] = None) = {
      new DateColumn[A] {
        override val id = inId
        override val name = inName
        override def value(a: A): Option[Date] = inValue(a)
        override def onClick(a : A) : Option[EventHandler] = inOnClick
      }
    }
  }

  object DateTimeColumn {
    def apply[A](inId : String, inName : String, inValue : A => Option[LocalDateTime], inOnClick : Option[EventHandler] = None) = {
      new DateTimeColumn[A] {
        override val id = inId
        override val name = inName
        override def value(a: A): Option[LocalDateTime] = inValue(a)
        override def onClick(a : A) : Option[EventHandler] = inOnClick
      }
    }
  }

  object ImageColumn {
    def apply[A](inId : String, inName : String, inOnClick : Option[EventHandler] = None, url : (A) => Option[String]) : ImageColumn[A] = {
      new ImageColumn[A] {
        override val id = inId
        override val name = inName
        override def onClick(a : A) : Option[EventHandler] = inOnClick
        override def value(a: A): Option[String] = None
        override def imageUrl(a: A): Option[String] = url(a)
      }
    }

    def apply[A](inId : String, inName : String, bytesFor : A => Array[Byte], fileTypeFor : A => String, inOnClick : Option[EventHandler]) : ImageColumn[A] = {
      new ImageColumn[A] {
        override val id = inId
        override val name = inName
        override def onClick(a : A) : Option[EventHandler] = inOnClick
        override def value(a: A): Option[String] = None
        override def dataString(a : A): Option[String] = Some(CommonUtil.toImageDataString(bytesFor(a), fileTypeFor(a)))
      }

    }
  }

  object ButtonColumn {
    def apply[A](inId : String, inName : String, buttonText : String, inButtonPressed : Option[(JQueryEventObject,A) => Any] = None, inClasses : Option[A => String], inDoCreate : Option[A => Boolean] = None) = {
      new ButtonColumn[A] {
        override val id = inId
        override val name = inName
        override val isButton: Boolean = true
        override def onClick(a : A) : Option[EventHandler] = None
        override def value(a: A): Option[String] = Some(buttonText)
        override def classes(a: A): Option[String] = inClasses.map(_(a))
        override def doCreate(a: A): Boolean = inDoCreate.map(_(a)).getOrElse(true)
        override def onButtonPressed: Option[(JQueryEventObject,A) => Any] = inButtonPressed
      }
    }
  }


}
