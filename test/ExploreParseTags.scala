import lokallykke.instagram.InstagramLoader

object ExploreParseTags {

  def main(args: Array[String]): Unit = {
    val str = "Kasse masse med kalot på og spræl #op #ned"
    val (cap, tags) = InstagramLoader.parseCaptionWithTags(str)
    println(cap)
    println(tags.mkString(" "))
  }

}
