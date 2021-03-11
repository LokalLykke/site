import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale

object ExploreDoubleFormat {
  def main(args: Array[String]): Unit = {
    val numberLocale = Locale.forLanguageTag("da-DK")
    val formatPretty = NumberFormat.getInstance(numberLocale).asInstanceOf[DecimalFormat]
    formatPretty.applyPattern("###,##0.00")

    val tessa = 100.23
    println(formatPretty.format(tessa))


  }

}
