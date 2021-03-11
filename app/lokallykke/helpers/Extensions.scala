package lokallykke.helpers

import lokallykke.helpers.Extensions.DoubleExtensions.formatPretty

import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale

object Extensions {

  implicit class StringExtensions(str : String) {
    def afterLastOccurenceOf(seq : CharSequence) : String = {
      str.substring(str.lastIndexOf(seq), str.length)
    }

    def splitOnLastOccurrenceOf(ch : Char) : Option[(String, String)] = {
      if(!str.contains(ch)) None
      else {
        val split = str.split(ch)
        Some(split.take(split.length - 1).mkString(""), split(split.length -1 ))
      }
    }

    def ~(seq : CharSequence) : Boolean = {
      str.toLowerCase.contains(seq.toString.toLowerCase)
    }

    def ~~(seq : Seq[CharSequence]) : Boolean = {
      seq.exists(se => str.toLowerCase.contains(se.toString.toLowerCase))
    }

  }

  implicit class DoubleExtensions(d : Double) {
    def toPrettyString = formatPretty.format(d)

  }


  object DoubleExtensions {
    val numberLocale = Locale.forLanguageTag("da-DK")
    val formatPretty = NumberFormat.getInstance(numberLocale).asInstanceOf[DecimalFormat]
    formatPretty.applyPattern("###,##0.00")
    formatPretty.getDecimalFormatSymbols.setDecimalSeparator(',')
    formatPretty.getDecimalFormatSymbols.setGroupingSeparator('.')
  }


}
