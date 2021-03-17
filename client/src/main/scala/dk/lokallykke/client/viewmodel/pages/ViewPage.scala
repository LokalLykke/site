package dk.lokallykke.client.viewmodel.pages

case class ViewPage(
                   pageId : Long,
                   name : String,
                   description : Option[String],
                   tags : Seq[String]
                   )
