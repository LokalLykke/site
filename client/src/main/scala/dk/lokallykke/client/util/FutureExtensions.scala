package dk.lokallykke.client.util

import scala.concurrent.{ExecutionContext, Future}

object FutureExtensions {

  implicit class FutureExtended[A](fut : Future[A]) {
    def whenDone[B](func : A => B) : Option[B] = {
      var ret : Option[B] = None
      implicit val ec = ExecutionContext.global
      fut.onComplete(tr => {
        tr.toOption.map {
          case futRes => ret = Some(func(futRes))
        }
      })
      ret
    }
  }



}
