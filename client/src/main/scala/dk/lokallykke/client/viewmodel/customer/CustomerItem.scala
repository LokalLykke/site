package dk.lokallykke.client.viewmodel.customer

case class CustomerItem(
                       id : Long,
                       name : String,
                       caption : Option[String],
                       imageUrl : String
                       )
