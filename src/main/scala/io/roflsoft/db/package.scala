package io.roflsoft

import cats.effect.{Bracket, ConcurrentEffect, ContextShift, IO, Sync}
import com.typesafe.scalalogging.Logger
import doobie.{ConnectionIO, ExecutionContexts, Transactor}
import doobie.implicits._
import doobie.util.log
import doobie.util.log.LogHandler
import monix.eval.Task
import doobie._

package object db {

  object transactorStore {
    implicit val taskContextShift: ContextShift[Task] = Task.contextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

    implicit val taskTransactor: Transactor[Task] { type A = Unit  } = Transactor.fromDriverManager[Task]("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/Nigiri", "admin", "admin")
    implicit val ioTransactor: Transactor[IO] { type A = Unit  } = Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/Nigiri", "admin", "admin")

    def rollBackTransactor[F[_]](implicit transactor: Transactor[F]) = Transactor.oops.set(transactor, HC.rollback)

    // Stream interop
    import cats.effect._, fs2._
    import fs2.interop.reactivestreams._
    import monix.reactive.Observable

    abstract class StreamConverter[F[_] : ConcurrentEffect, A] {
      def toList(stream: Stream[F, A]): F[List[A]]
    }

    implicit def taskStreamConverter[A](implicit concurrentEffect: ConcurrentEffect[Task]): StreamConverter[Task, A] = new StreamConverter[Task, A] {
      override def toList(stream: Stream[Task, A]): Task[List[A]] =
        Observable.fromReactivePublisher(stream.toUnicastPublisher()).toListL
    }

    implicit def ioStreamConverter[A](implicit concurrentEffect: ConcurrentEffect[IO]): StreamConverter[IO, A] = new StreamConverter[IO, A] {
      override def toList(stream: Stream[IO, A]): IO[List[A]] = stream.compile.toList
    }
  }


  def complete[A, F[M]](io: ConnectionIO[A])(implicit transactor: Transactor[F] { type A = Unit }, bracket: Bracket[F, Throwable] ): F[A] = {
    io.transact(transactor)
  }


  object logging {
    val logger: Logger = Logger("DAO")

    val logHandler = LogHandler {
      case log.Success(sql, args, exec, processing) => logger.info(s"${Console.WHITE}Success: ${Console.GREEN}${sql} ${Console.WHITE}Arguments: ${Console.GREEN}${args.mkString(",")} ${Console.WHITE}Duration: ${Console.BLUE}exec ${exec.toSeconds}s processing ${processing.toSeconds}s${Console.RESET}")
      case log.ProcessingFailure(sql, args, exec, processing, failure) => logger.error(s"${Console.RED}SQL Processing Failure: ${Console.WHITE}${sql} ${Console.RED}Arguments: ${Console.WHITE}${args.mkString(",")} ${Console.WHITE}Duration: ${Console.BLUE}exec ${exec.toSeconds}s processing ${processing.toSeconds}s${Console.RESET}", failure)
      case log.ExecFailure(sql, args, exec, failure) => logger.error(s"${Console.RED}SQL Execution Failure: ${Console.WHITE}${sql} ${Console.RED}Arguments: ${Console.WHITE}${args.mkString(",")} ${Console.WHITE}Duration: ${Console.BLUE}exec ${exec.toSeconds}s${Console.RESET}", failure)
    }
  }

}
