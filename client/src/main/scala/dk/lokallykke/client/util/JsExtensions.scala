package dk.lokallykke.client.util

import dk.lokallykke.client.util.JsExtensions.DateExtensions

import java.text.{DecimalFormat, NumberFormat}
import java.util.{Locale, TimeZone}
import java.time._
import java.time.format._
import scala.scalajs.js.Date

object JsExtensions {

  implicit class DoubleExtensions(d : Double) {
    import DoubleExtensions._
    def toPrettyString = formatPretty.format(d)
  }

  implicit class DateExtensions(d : Date) {
    import DateExtensions._
    def toDateString ={
      val localDateTime = Instant.ofEpochMilli(d.getTime.toLong)
      dateFormatter.format(LocalDateTime.ofInstant(localDateTime, ZoneOffset.ofHours(1)))
    }
  }

  implicit class DateTimeExtensions(d : LocalDateTime) {
    import DateTimeExtensions._

    def toDateTimeString = d.format(dateTimeFormatter)
  }


  implicit class LongExtensions(l : Long) {
    def toDate = new Date(l)
    def toDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneOffset.ofHours(1))
  }


  object DoubleExtensions {
    val numberLocale = Locale.forLanguageTag("da-DK")
    val formatPretty = NumberFormat.getInstance(numberLocale).asInstanceOf[DecimalFormat]
    formatPretty.applyPattern("###,##0.00")

  }

  object DateExtensions {
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
  }

  object DateTimeExtensions {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  }




}
