package lokallykke.scheduled


import scala.concurrent.duration.{Duration, FiniteDuration}

trait Scheduled {

  def name : String
  def initialDelay : FiniteDuration
  def delay : FiniteDuration
  def execute : () => Unit

}

object Scheduled {
  def apply(name : String, initialDelay : FiniteDuration, delay : FiniteDuration, proc : () => Unit) = new Scheduled {
    override def name: String = name
    override def initialDelay: FiniteDuration = initialDelay
    override def delay: FiniteDuration = delay

    override def execute: () => Unit = proc
  }
}