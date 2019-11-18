package io.roflsoft

import akka.http.scaladsl.marshalling.{ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.server.{Directive, Directive1, Route}
import akka.http.scaladsl.server.directives.FutureDirectives
import monix.eval.Task
import monix.execution.Scheduler
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import scala.util.Try

package object http {

  object task extends FutureDirectives {

    object implicits {

      implicit def completeTask[A: ToResponseMarshaller](t: Task[A])(implicit scheduler: Scheduler): Route = {
        t.onComplete { data =>
          complete(ToResponseMarshallable(data))
        }
      }

      implicit class TaskExtensions[T](task: Task[T])(implicit scheduler: Scheduler) {
        def onComplete: Directive1[Try[T]] =
          Directive { inner => ctx =>
            task.runToFuture.transformWith(t => inner(Tuple1(t))(ctx))
          }
      }
    }
  }
}
