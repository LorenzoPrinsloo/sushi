package io.roflsoft.db

import doobie._
import doobie.implicits._
import doobie.quill.DoobieContext
import doobie.util.Read
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import enumeratum.EnumEntry
import fs2._
import io.getquill.Literal
import io.roflsoft.reflection.utils.members
import scala.language.higherKinds
import scala.reflect.runtime.universe.TypeTag

/**
 * A simple Postgres repository using Doobie
 *
 * @param tableName Name of the Table for the repository
 * @param logHandler Database LogHandler
 * @tparam A Database model stored in th postgres database
 */
abstract class PostgresRepository[A <: PostgresModel: TypeTag: Read](tableName: String, logHandler: LogHandler = logging.logHandler) extends Repository[A] {

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

  override def add(item: A): ConnectionIO[A] = {
    val fieldValues: String = item.productIterator.map(escapeLiteral).mkString(",")

    val insert: Fragment = fr"""INSERT INTO""" ++
      Fragment.const(s""""$tableName"""") ++
      fr"""(""" ++ Fragment.const(members[A].mkString(",")) ++ fr""") VALUES (""" ++ Fragment.const(fieldValues) ++ fr""")"""

      insert
        .updateWithLogHandler(logHandler)
        .withUniqueGeneratedKeys[A](members[A]: _*)
  }

  override def add(items: A*): Stream[ConnectionIO, A] = {

    val values: Fragment = Fragment.const(
      items.map { item =>
      fr"""(""" ++ Fragment.const(item.productIterator.map(escapeLiteral).mkString(",")) ++ fr""")"""
      }.mkString(",")
    )

    val insert: Fragment = fr"""INSERT INTO""" ++
      Fragment.const(s""""$tableName"""") ++
      fr"""(""" ++ Fragment.const(members[A].mkString(",")) ++
      fr""") VALUES (""" ++ values ++ fr")"


    insert.updateWithLogHandler(logHandler).withGeneratedKeys[A](members[A]: _*)
  }

  override def update(item: A): ConnectionIO[A] = {
    val fieldValues: List[String] = item.productIterator.map(escapeLiteral).toList
    val updates: Fragment = Fragment.const(
      members[A].zip(fieldValues).map {
        case (fieldName, value) => s"""$fieldName = $value"""
      }.mkString(",")
    )

    val query: Fragment = fr"""UPDATE""" ++ Fragment.const(s"""$tableName""") ++ fr"""SET""" ++ updates ++ fr"WHERE" ++ Fragment.const(s"""id = ${item.id}""")
    query.updateWithLogHandler(logHandler).withUniqueGeneratedKeys[A](members[A]: _*)
  }

  override def remove(item: A): ConnectionIO[Int] = {
    val query: Fragment = fr"DELETE FROM" ++ Fragment.const(s"""$tableName""") ++ fr"WHERE" ++ Fragment.const(s"""id = ${item.id}""")

    query.updateWithLogHandler(logHandler).run
  }

  override def removeByQuery(q: Fragment = Fragment.empty): ConnectionIO[Int] = {
    val query: Fragment = fr"DELETE FROM" ++ Fragment.const(s"""$tableName""") ++ q

    query.updateWithLogHandler(logHandler).run
  }

  override def find(q: Fragment = Fragment.empty): Stream[ConnectionIO, A] = {
    val query = fr"SELECT * FROM" ++ Fragment.const(s"""$tableName""") ++ q

    query.queryWithLogHandler[A](logHandler).stream
  }
}
