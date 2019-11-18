package io.roflsoft

import cats.effect.{Bracket, ContextShift, IO}
import doobie.{ConnectionIO, ExecutionContexts, Transactor}
import doobie.implicits._
import monix.eval.Task

package object db {

  object transactorStore {
    implicit val taskContextShift: ContextShift[Task] = Task.contextShift
    implicit val ioContextShift: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

    implicit val taskTransactor: Transactor[Task] { type A = Unit  } = Transactor.fromDriverManager[Task]("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/Nigiri", "admin", "admin")
    implicit val ioTransactor: Transactor[IO] { type A = Unit  } = Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/Nigiri", "admin", "admin")
  }

  def complete[A, F[M]](io: ConnectionIO[A])(implicit transactor: Transactor[F] { type A = Unit }, bracket: Bracket[F, Throwable] ): F[A] = {
    io.transact(transactor)
  }

}
