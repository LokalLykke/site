package lokallykke.model.pages

case class Page(
               id : Long,
               name : String,
               description : Option[String],
               isdeleted : Int
               )
