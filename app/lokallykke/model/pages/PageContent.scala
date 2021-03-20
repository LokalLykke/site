package lokallykke.model.pages

case class PageContent(
                      id : Long,
                      pageid : Long,
                      indx : Int,
                      contenttype : String,
                      parentid : Option[Long] = None,
                      text : Option[String] = None,
                      style : Option[String] = None,
                      level : Option[Int] = None,
                      imageid : Option[Long] = None,
                      caption : Option[String] = None,
                      withborder : Option[Int] = None,
                      stretched : Option[Int] = None,
                      withbackground : Option[Int] = None
                      )
