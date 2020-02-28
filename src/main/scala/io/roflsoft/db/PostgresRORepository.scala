package io.roflsoft.db

import fs2._
import doobie.implicits._
import doobie.util.Read
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie._
import scala.reflect.runtime.universe.TypeTag
import scala.language.higherKinds
import io.roflsoft.reflection.utils._

abstract class PostgresRORepository[A <: PostgresModel: TypeTag: Read](logHandler: LogHandler = logging.logHandler) extends RORepository[A] {
  type Query = Fragment

  override def find(f: Query): Stream[ConnectionIO, A] = {
    val q = fr"SELECT * FROM" ++ Fragment.const(s"""${className[A]}""") ++ f

    q.queryWithLogHandler[A](logHandler).stream
  }
}
