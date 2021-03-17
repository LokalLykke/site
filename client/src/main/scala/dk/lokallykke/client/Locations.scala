package dk.lokallykke.client

object Locations {

  object Accounting {
    val WebSocket = "/accounting/ws"
  }

  object Items {
    val prefix = "/items"
    def itemImage(itemId : Long) = s"$prefix/itemimage?itemId=$itemId"
    def upload = s"$prefix/upload"
    def uploadInstagramItem = s"$prefix/upload-instagram-item"
  }

  object Pages {
    val prefix = "/pages"
    def saveImage = s"$prefix/saveimage"
  }



}
