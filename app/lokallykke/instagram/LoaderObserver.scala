package lokallykke.instagram

import lokallykke.instagram.LoaderObserver.ParsingState.ParsingState

trait LoaderObserver {
  val loaderObserverId = LoaderObserver.nextId

  def onCommandLineUpdate(str : String) : Unit
  def onProgressChange(state : ParsingState) : Unit

}


object LoaderObserver {
  private var currentId = 0L
  def nextId = {
    currentId += 1L
    currentId
  }

  object ParsingState extends Enumeration {
    type ParsingState = super.Value
    val Downloading, Parsing, Done = super.Value
  }

  object Sink extends LoaderObserver {
    override def onCommandLineUpdate(str: String): Unit = {}
    override def onProgressChange(state: ParsingState.ParsingState): Unit = {}
  }

}
