package lokallykke.helpers

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

}
