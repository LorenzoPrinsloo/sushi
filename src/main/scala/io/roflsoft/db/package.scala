package io.roflsoft

import cats.effect.{Bracket, ContextShift, IO}
import com.typesafe.scalalogging.Logger
import doobie.{ConnectionIO, ExecutionContexts, Transactor}
import doobie.implicits._
import doobie.util.log
import doobie.util.log.LogHandler
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

  object logging {
    val logger: Logger = Logger("DAO")

    val logHandler = LogHandler {
      case log.Success(sql, args, exec, processing) => logger.info(s"${Console.WHITE}Success: ${Console.GREEN}${sql} ${Console.WHITE}Arguments: ${Console.GREEN}${args.mkString(",")} ${Console.WHITE}Duration: ${Console.BLUE}exec ${exec.toSeconds}s processing ${processing.toSeconds}s${Console.RESET}")
      case log.ProcessingFailure(sql, args, exec, processing, failure) => logger.error(s"${Console.RED}SQL Processing Failure: ${Console.WHITE}${sql} ${Console.RED}Arguments: ${Console.WHITE}${args.mkString(",")} ${Console.WHITE}Duration: ${Console.BLUE}exec ${exec.toSeconds}s processing ${processing.toSeconds}s${Console.RESET}", failure)
      case log.ExecFailure(sql, args, exec, failure) => logger.error(s"${Console.RED}SQL Execution Failure: ${Console.WHITE}${sql} ${Console.RED}Arguments: ${Console.WHITE}${args.mkString(",")} ${Console.WHITE}Duration: ${Console.BLUE}exec ${exec.toSeconds}s${Console.RESET}", failure)
    }
  }

}
