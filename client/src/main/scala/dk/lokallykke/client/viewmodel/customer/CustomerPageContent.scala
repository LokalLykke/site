package dk.lokallykke.client.viewmodel.customer

case class CustomerPageContent(
                              header : Option[(String,Int)] = None,
                              imageUrl : Option[String] = None,
                              paragraph : Option[String] = None,
                              orderedList : Option[Seq[String]] = None,
                              unOrderedList : Option[Seq[String]] = None
                              )
