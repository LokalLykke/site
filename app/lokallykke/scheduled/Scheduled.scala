package lokallykke.scheduled


import scala.concurrent.duration.{Duration, FiniteDuration}

trait Scheduled {

  def name : String
  def initialDelay : FiniteDuration
  def delay : FiniteDuration
  def execute : () => Unit

}

object Scheduled {
  def apply(inName : String, inInitialDelay : FiniteDuration, inDelay : FiniteDuration, inProc : () => Unit) = new Scheduled {
    override def name: String = inName
    override def initialDelay: FiniteDuration = inInitialDelay
    override def delay: FiniteDuration = inDelay
    override def execute: () => Unit = inProc
  }
}