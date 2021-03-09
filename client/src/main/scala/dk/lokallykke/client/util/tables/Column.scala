package dk.lokallykke.client.util.tables

import org.querki.jquery.EventHandler
import dk.lokallykke.client.util.JsExtensions._

import java.time.LocalDateTime
import scala.scalajs.js.Date


abstract class Column[A, B] {
  val id : String
  val name : String
  def value(a : A) : Option[B]
  def onClick(a : A) : Option[EventHandler]
  def imageUrl(a : A) : Option[String] = None
  def format(b : B) : Option[String]
  def stringValue(a : A) : String = (for(b <- value(a); str <- format(b)) yield str).getOrElse("")

}


object Column {
  abstract class StringColumn[A] extends Column[A,String] {override def format(b : String) = Some(b)}
  abstract class DoubleColumn[A] extends Column[A,Double] {override def format(d : Double) = Some(d.toPrettyString)}
  abstract class DateColumn[A] extends Column[A,Date] {override def format(d : Date) = Some(d.toDateString)}
  abstract class DateTimeColumn[A] extends Column[A,LocalDateTime] {override def format(d : LocalDateTime) = Some(d.toDateTimeString)}
  abstract class ImageColumn[A] extends Column[A, String] {override def format(str : String) = None}

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
    def apply[A](inId : String, inName : String, inOnClick : Option[EventHandler] = None, url : (A) => Option[String]) = {
      new ImageColumn[A] {
        override val id = inId
        override val name = inName
        override def onClick(a : A) : Option[EventHandler] = inOnClick
        override def value(a: A): Option[String] = None
        override def imageUrl(a: A): Option[String] = url(a)
      }
    }
  }


}
