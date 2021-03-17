package lokallykke.model.pages

case class Image(
                id : Long,
                bytes : Array[Byte],
                contenttype : String
                )
