package dk.lokallykke.client.util

import java.sql.{Date, Timestamp}
import java.text.{DecimalFormat, NumberFormat}
import java.time.format.DateTimeFormatter
import java.util.Locale

object JsExtensions {


  implicit class DoubleExtensions(d : Double) {
    import DoubleExtensions._
    def toPrettyString = formatPretty.format(d)
  }

  implicit class DateExtensions(d : Date) {
    import DateExtensions._
    def toDateString = DateExtensions.dkFormat.format(d.toLocalDate)
  }

  implicit class LongExtensions(l : Long) {
    def toDate = new Date(l)
    def toTimestamp = new Timestamp(l)
  }

  implicit class TimestampExtensions(ts : Timestamp) {
    import TimestampExtensions._
    def toTimestampString = dkFormat.format(ts.toLocalDateTime)
  }


  object DoubleExtensions {
    val numberLocale = Locale.forLanguageTag("da-DK")
    val formatPretty = NumberFormat.getInstance(numberLocale).asInstanceOf[DecimalFormat]
    formatPretty.applyPattern("###,##0.00")

  }

  object DateExtensions {
    val dkFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
  }

  object TimestampExtensions {
    val dkFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH24:MI:SS")
  }


}
