package lokallykke.scheduled

import akka.actor.ActorSystem
import play.api.inject._
import scala.concurrent.duration._
import javax.inject._
import scala.concurrent.ExecutionContext
import scala.util.Try

class SchedulerStarter @Inject()(actorSystem : ActorSystem){
  Scheduler.init(actorSystem)
}

class Scheduler extends SimpleModule(bind[SchedulerStarter].toSelf.eagerly()) {
}


object Scheduler {
  def schedules : List[Scheduled] = List(
     //Scheduled("Ping clients", 10.seconds, 20.seconds, () => Pingable.ping)
  )

  schedules.foreach(s => println(s.name))

  protected[scheduled] def init(actorSystem: ActorSystem) = {
    /*println("init'ing")
    schedules.foreach {
      case sched => {
        implicit val context = actorSystem.dispatcher.asInstanceOf[ExecutionContext]
        val runnable = new Runnable {
          override def run(): Unit = Try {
            sched.execute()
          }
        }
        //actorSystem.scheduler.scheduleAtFixedRate(sched.initialDelay, sched.delay)(runnable)
      }
    }*/

  }



}
