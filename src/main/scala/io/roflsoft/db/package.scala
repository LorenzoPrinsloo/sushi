package io.roflsoft

import cats.Monad
import cats.arrow.FunctionK
import cats.effect.{Bracket, ConcurrentEffect, ContextShift, IO, Sync}
import com.typesafe.scalalogging.Logger
import doobie.{ConnectionIO, ExecutionContexts, Transactor}
import doobie.implicits._
import doobie.util.log
import doobie.util.log.LogHandler
import monix.eval.Task
import doobie._
import cats.effect._
import fs2._
import doobie._
import doobie.quill.DoobieContext
import fs2.interop.reactivestreams._
import io.getquill.Literal
import io.roflsoft.db.transactorStore.StreamConverter
import monix.reactive.Observable
import scala.language.higherKinds

package object db {
  type MonadTransactor[F[_]] = Transactor[F] { type A = Unit  }
  type IOStream[A] = Stream[ConnectionIO, A]
  type ErrorBracket[F[_]] = Bracket[F, Throwable]

  val doobieCtx = new DoobieContext.Postgres(Literal) // Literal naming scheme

  object transactorStore {
    implicit val taskContextShift: ContextShift[Task] = Task.contextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

    implicit val taskTransactor: Transactor[Task] { type A = Unit  } = Transactor.fromDriverManager[Task]("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/Nigiri", "admin", "admin")
    implicit val ioTransactor: Transactor[IO] { type A = Unit  } = Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/Nigiri", "admin", "admin")

    def rollBackTransactor[F[_]](implicit transactor: Transactor[F]) = Transactor.oops.set(transactor, HC.rollback)

    abstract class StreamConverter[F[_] : ConcurrentEffect, L[_], A] {
      def as(stream: Stream[F, A]): F[L[A]]
    }

    implicit def taskStreamListConverter[A](implicit concurrentEffect: ConcurrentEffect[Task]): StreamConverter[Task, List, A] = new StreamConverter[Task, List, A] {
      override def as(stream: Stream[Task, A]): Task[List[A]] =
        Observable.fromReactivePublisher(stream.toUnicastPublisher()).toListL
    }

    implicit def ioStreamListConverter[A](implicit concurrentEffect: ConcurrentEffect[IO]): StreamConverter[IO, List, A] = new StreamConverter[IO, List, A] {
      override def as(stream: Stream[IO, A]): IO[List[A]] = stream.compile.toList
    }
  }

  object conversion {

    def complete[A, F[_]](io: ConnectionIO[A])(implicit transactor: MonadTransactor[F], bracket: ErrorBracket[F] ): F[A] = io.transact(transactor)

    def completeStream[A, F[_] : Monad, L[_]](stream: Stream[ConnectionIO, A])(implicit transactor: MonadTransactor[F], streamConverter: StreamConverter[F, L, A]): F[L[A]] =
      streamConverter.as(stream.transact(transactor))

    implicit def naturalTransformation[F[_]](implicit transactor: MonadTransactor[F], bracket: ErrorBracket[F]): FunctionK[ConnectionIO, F] = new FunctionK[ConnectionIO, F] {
      override def apply[A](fa: ConnectionIO[A]): F[A] = fa.transact(transactor)
    }

    implicit class ConnectionIOConversion[A](io: ConnectionIO[A]) {
      def runAs[F[_]](implicit ev: FunctionK[ConnectionIO, F]): F[A] = ev.apply(io)

      def runWith[F[_]](transactor: MonadTransactor[F])(implicit bracket: ErrorBracket[F]): F[A] = {
        io.transact(transactor)
      }
    }

    implicit class ConnectionIOStreamConversion[A](ioStream: IOStream[A]) {
      def runAs[F[_]: Monad, L[_]](implicit ev: StreamConverter[F, L, A], transactor: MonadTransactor[F]): F[L[A]] = {
        ev.as(ioStream.transact(transactor))
      }

      def runWith[F[_]: Monad, L[_]](transactor: MonadTransactor[F])(implicit ev: StreamConverter[F, L, A]): F[L[A]] = {
        ev.as(ioStream.transact(transactor))
      }
    }
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
