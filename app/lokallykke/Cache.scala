package lokallykke

object Cache {
  object InstagramImages {
    private val lockObject = "lock"
    val MaxImages = 500
    private var currentIds : List[String] = Nil
    private var imageMap = Map.empty[String, Array[Byte]]

    def cache(instagramid : String, bytes : Array[Byte]) = lockObject.synchronized {
      if(imageMap.contains(instagramid)) {

      }
      else {
        if(currentIds.size > MaxImages - 1) {
          currentIds.drop(MaxImages -1).foreach(id => imageMap = imageMap - id)
          currentIds = currentIds.take(MaxImages -1)
        }
        currentIds = instagramid :: currentIds
        imageMap += (instagramid -> bytes)
      }
    }

    def pop(instagramId : String) : Option[Array[Byte]] = lockObject.synchronized {
      val ret = imageMap.get(instagramId)
      currentIds = currentIds.filter(_ != instagramId)
      imageMap -= instagramId
      ret
    }
  }

}
