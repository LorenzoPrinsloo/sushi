package io.roflsoft.db

import cats.effect.{Bracket, Sync}
import doobie.implicits._
import doobie.util.Read
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import enumeratum.EnumEntry
import io.roflsoft.db.transactorStore.StreamConverter
import scala.reflect.runtime.universe.TypeTag
import scala.language.higherKinds
import io.roflsoft.reflection.utils.members

/**
 * A simple Postgres repository using Doobie
 *
 * @param tableName Name of the Table for the repository
 * @param logHandler Database LogHandler
 * @param transactor implicit transactor for our D Monad
 * @param errorBracket implicit cats errorbracket for our Monad
 * @tparam A Database model stored in th postgres database
 * @tparam F Database Monad ie. Task, ZIO, Future, IO
 */
abstract class PostgresRepository[A <: PostgresModel: TypeTag: Read, F[A]](
    tableName: String,
    logHandler: LogHandler = logging.logHandler)(
    implicit transactor: Transactor[F] { type A = Unit },
    errorBracket: Bracket[F, Throwable],
    streamConverter: StreamConverter[F, A])
    extends Repository[A, F] {

  type Query = Fragment


  private def escapeLiteral(a: Any): String = {
    a match {
      case Some(any)    => escapeLiteral(any)
      case None         => "null"
      case s: String    => s"'$s'"
      case e: EnumEntry => s"'${e.entryName}'"
      case f            => s"$f"
    }
  }

  override def add(item: A): F[A] = {
    val fieldValues: String = item.productIterator.map(escapeLiteral).mkString(",")

    val insert: Fragment = fr"""INSERT INTO""" ++
      Fragment.const(s""""$tableName"""") ++
      fr"""(""" ++ Fragment.const(members[A].mkString(",")) ++ fr""") VALUES (""" ++ Fragment.const(fieldValues) ++ fr""")"""

    complete(
      insert
        .updateWithLogHandler(logHandler)
        .withUniqueGeneratedKeys[A](members[A]: _*))
  }

  override def add(items: A*): F[List[A]] = {

    val values: Fragment = Fragment.const(
      items.map { item =>
      fr"""(""" ++ Fragment.const(item.productIterator.map(escapeLiteral).mkString(",")) ++ fr""")"""
      }.mkString(",")
    )

    val insert: Fragment = fr"""INSERT INTO""" ++
      Fragment.const(s""""$tableName"""") ++
      fr"""(""" ++ Fragment.const(members[A].mkString(",")) ++
      fr""") VALUES (""" ++ values ++ fr")"


    streamConverter.toList(insert.updateWithLogHandler(logHandler).withGeneratedKeys[A](members[A]: _*).transact(transactor))
  }

  override def update(item: A): F[A] = {
    val fieldValues: List[String] = item.productIterator.map(escapeLiteral).toList
    val updates: Fragment = Fragment.const(
      members[A].zip(fieldValues).map {
        case (fieldName, value) => s"""$fieldName = $value"""
      }.mkString(",")
    )

    val query: Fragment = fr"""UPDATE""" ++ Fragment.const(s"""$tableName""") ++ fr"""SET""" ++ updates ++ fr"WHERE" ++ Fragment.const(s"""uuid = ${item.uuid}""")
    complete(query.updateWithLogHandler(logHandler).withUniqueGeneratedKeys[A](members[A]: _*))
  }

  override def remove(item: A): F[Int] = {
    val query: Fragment = fr"DELETE FROM" ++ Fragment.const(s"""$tableName""") ++ fr"WHERE" ++ Fragment.const(s"""uuid = ${item.uuid}""")

    complete(query.updateWithLogHandler(logHandler).run)
  }

  override def removeByQuery(q: Fragment = Fragment.empty): F[Int] = {
    val query: Fragment = fr"DELETE FROM" ++ Fragment.const(s"""$tableName""") ++ q

    complete(query.updateWithLogHandler(logHandler).run)
  }

  override def query(q: Fragment = Fragment.empty): F[List[A]] = {
    val query = fr"SELECT * FROM" ++ Fragment.const(s"""$tableName""") ++ q

    complete(query.queryWithLogHandler[A](logHandler).to[List])
  }
}
