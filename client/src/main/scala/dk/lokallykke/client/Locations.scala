package dk.lokallykke.client

object Locations {

  object Accounting {
    val WebSocket = "/accounting/ws"
  }

  object Items {
    def itemImage(itemId : Long) = s"/items/itemimage?itemId=$itemId"
  }



}
