package io.roflsoft.db

import cats.effect.Bracket
import com.typesafe.scalalogging.Logger
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.Read
import doobie.util.log.LogHandler
import enumeratum.EnumEntry
import io.roflsoft.http.server.SimpleWebServer
import io.roflsoft.reflection.utils.{className, members}

import scala.reflect.runtime.universe.TypeTag
import scala.language.higherKinds

abstract class DAO[M <: Product : TypeTag : Read](tableName: String, logHandler: LogHandler = logging.logHandler) {

  type DAOTransactor[F[M]] = Transactor[F] { type A = Unit }
  type ErrorBracket[F[M]] = Bracket[F, Throwable]

  private def escapeLiteral(a: Any): String = {
    a match {
      case s: String => s"'$s'"
      case e: EnumEntry => s"'${e.entryName}'"
      case f => s"$f"
    }
  }

  def insert[F[M] : DAOTransactor : ErrorBracket](m: M): F[M] = {
    val fieldValues = m.productIterator.map(escapeLiteral).mkString(",")

    val insert = fr"""INSERT INTO""" ++ Fragment.const(s""""$tableName"""") ++ fr"""(""" ++ Fragment.const(members[M].mkString(",")) ++ fr""") VALUES (""" ++ Fragment.const(fieldValues) ++ fr""")"""
    complete(insert.updateWithLogHandler(logHandler).withUniqueGeneratedKeys[M](members[M]: _*))
  }


  def selectAll[F[M] : DAOTransactor : ErrorBracket]: F[List[M]] = {
    val select = fr"""SELECT""" ++ Fragment.const(members[M].mkString(",")) ++ fr"""FROM""" ++ Fragment.const(s""""$tableName"""")
    complete(select.queryWithLogHandler[M](logHandler).to[List])
  }

  private def selectByIdQuery(id: Long): ConnectionIO[M] = {
    (fr"""SELECT""" ++ Fragment.const(members[M].mkString(",")) ++ fr"""FROM""" ++ Fragment.const(s""""$tableName"""") ++ fr"""WHERE id = $id""")
      .queryWithLogHandler[M](logHandler).unique
  }

  def selectById[F[M] : DAOTransactor : ErrorBracket](id: Long): F[M] = complete(selectByIdQuery(id))

  private def deleteByIdQuery(id: Long): ConnectionIO[Int] = {
    (fr"""DELETE FROM""" ++ Fragment.const(s""""$tableName"""") ++ fr"""WHERE id = $id""")
      .updateWithLogHandler(logHandler).run
  }

  def deleteById[F[_] : DAOTransactor : ErrorBracket](id: Long): F[Int] = complete(deleteByIdQuery(id))

  private def updateByIdQuery(id: Long)(updates: (String, Any)*): ConnectionIO[M] = {
    val updateFragment = Fragment.const(updates.map { case (key, value) =>
      s"""$key = ${escapeLiteral(value)}"""
    }.mkString(","))

    val query = (fr"""UPDATE""" ++ Fragment.const(s""""$tableName"""") ++ fr"""SET""" ++ updateFragment ++ fr"""WHERE id = $id""")
    query.updateWithLogHandler(logHandler).withUniqueGeneratedKeys[M](members[M]: _*)
  }

  def updateById[F[M]: DAOTransactor : ErrorBracket](id: Long)(updates: (String, Any)*): F[M] = complete(updateByIdQuery(id)(updates: _*))

}
