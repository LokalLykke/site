package dk.lokallykke.client.viewmodel.holding

case class ViewHoldingItem(
                          id : Long,
                          name : String,
                          description : Option[String],
                          image : Option[Array[Byte]]
                          )
