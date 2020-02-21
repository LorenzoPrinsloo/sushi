package io.roflsoft.db

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.Read
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import io.roflsoft.db.transactorStore.StreamConverter
import scala.reflect.runtime.universe.TypeTag
import scala.language.higherKinds
import io.roflsoft.reflection.utils._

abstract class PostgresRORepository[A <: PostgresModel: TypeTag: Read, F[A]]
  (logHandler: LogHandler = logging.logHandler)
  (implicit transactor: Transactor[F] { type A = Unit }, errorBracket: Bracket[F, Throwable])
  extends RORepository[A, F] {

  final def query(f: Fragment = Fragment.empty): F[List[A]] = {
    val q = fr"SELECT * FROM" ++ Fragment.const(s"""${className[A]}""") ++ f

    complete(q.queryWithLogHandler[A](logHandler).to[List])
  }
}
