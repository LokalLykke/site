package dk.lokallykke.client.util

import dk.lokallykke.client.util.JsExtensions.DateExtensions

import java.text.{DecimalFormat, NumberFormat}
import java.util.{Locale, TimeZone}
import java.time._
import java.time.format._
import java.time.temporal.{ChronoField, TemporalField}
import scala.scalajs.js.Date
import scala.util.Try

object JsExtensions {

  implicit class DoubleExtensions(d : Double) {
    import DoubleExtensions._
    def isZero = {
      val rem = d % 1.0
      rem < 0.01 && rem > -0.01
    }
    def toPrettyString = {
      if(isZero) {
        formatPrettyInt.format(d) + ",00"
      }
      else formatPretty.format(d)
    }
    def toInputString = {
      if(isZero) {
        inputFormatInt.format(d) + ".00"
      }
      else inputFormat.format(d)
    }
  }

  implicit class DateExtensions(d : Date) {
    import DateExtensions._
    def toDateString ={
      val localDateTime = Instant.ofEpochMilli(d.getTime.toLong)
      dateFormatter.format(LocalDateTime.ofInstant(localDateTime, ZoneOffset.ofHours(1)))
    }
    def toInputDateString = {
      val localDateTime = Instant.ofEpochMilli(d.getTime.toLong)
      inputDateFormatter.format(LocalDateTime.ofInstant(localDateTime, ZoneOffset.ofHours(1)))
    }

  }

  implicit class DateTimeExtensions(d : LocalDateTime) {
    import DateTimeExtensions._

    def toDateTimeString = d.format(dateTimeFormatter)
    def toInputDateString = d.format(inputDateTimeFormatter)
  }


  implicit class LongExtensions(l : Long) {
    def toDate = new Date(l)
    def toDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneOffset.ofHours(1))
  }

  implicit class StringExtensions(str : String) {
    def toDateFromInput = (Try {
      val parsed = LocalDate.parse(str, DateExtensions.inputDateFormatter)
      new Date(parsed.getYear, parsed.getMonth.getValue, parsed.getDayOfMonth)
    }).toOption

    def toDateTimeFromInput = (Try{
      val parsed = LocalDateTime.parse(str, DateTimeExtensions.inputDateTimeFormatter)
      parsed
    }).toOption

    def toDoubleFromInput = (Try{
      str.toDouble
      //DoubleExtensions.inputFormat.parse(str).doubleValue()
    }).toOption

  }



  object DoubleExtensions {
    val numberLocale = Locale.forLanguageTag("da-DK")
    val formatPretty = NumberFormat.getInstance(numberLocale).asInstanceOf[DecimalFormat]
    formatPretty.applyPattern("###,##0.00")
    formatPretty.getDecimalFormatSymbols.setDecimalSeparator(',')
    formatPretty.getDecimalFormatSymbols.setGroupingSeparator('.')

    val formatPrettyInt = NumberFormat.getInstance(numberLocale).asInstanceOf[DecimalFormat]
    formatPrettyInt.applyPattern("###,##0")
    formatPrettyInt.getDecimalFormatSymbols.setDecimalSeparator(',')
    formatPrettyInt.getDecimalFormatSymbols.setGroupingSeparator('.')


    val inputNumberLocale = Locale.forLanguageTag("en-US")
    val inputFormat = NumberFormat.getInstance(inputNumberLocale).asInstanceOf[DecimalFormat]
    inputFormat.applyPattern("####0.00")
    inputFormat.getDecimalFormatSymbols.setDecimalSeparator('.')
    val inputFormatInt = NumberFormat.getInstance(inputNumberLocale).asInstanceOf[DecimalFormat]
    inputFormatInt.applyPattern("####0")
    inputFormatInt.getDecimalFormatSymbols.setDecimalSeparator('.')

  }



  object DateExtensions {
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val inputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  }

  object DateTimeExtensions {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    val inputDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  }




}
