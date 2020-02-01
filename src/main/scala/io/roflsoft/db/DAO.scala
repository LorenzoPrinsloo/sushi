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
import logging.logger
import scala.reflect.runtime.universe.TypeTag
import scala.language.higherKinds

abstract class DAO[Model <: Product : TypeTag : Read, Patch <: Product : TypeTag : Read](tableName: String, logHandler: LogHandler = logging.logHandler) {

  type DAOTransactor[F[Model]] = Transactor[F] { type A = Unit }
  type ErrorBracket[F[Model]] = Bracket[F, Throwable]

  private def escapeLiteral(a: Any): String = {
    a match {
      case s: String => s"'$s'"
      case e: EnumEntry => s"'${e.entryName}'"
      case f => s"$f"
    }
  }

  def insert[F[Model] : DAOTransactor : ErrorBracket](m: Model): F[Model] = {
    val fieldValues = m.productIterator.map(escapeLiteral).mkString(",")

    val insert = fr"""INSERT INTO""" ++ Fragment.const(s""""$tableName"""") ++ fr"""(""" ++ Fragment.const(members[Model].mkString(",")) ++ fr""") VALUES (""" ++ Fragment.const(fieldValues) ++ fr""")"""
    complete(insert.updateWithLogHandler(logHandler).withUniqueGeneratedKeys[Model](members[Model]: _*))
  }


  def selectAll[F[Model] : DAOTransactor : ErrorBracket]: F[List[Model]] = {
    val select = fr"""SELECT""" ++ Fragment.const(members[Model].mkString(",")) ++ fr"""FROM""" ++ Fragment.const(s""""$tableName"""")
    complete(select.queryWithLogHandler[Model](logHandler).to[List])
  }

  private def selectByIdQuery(id: Long): ConnectionIO[Model] = {
    (fr"""SELECT""" ++ Fragment.const(members[Model].mkString(",")) ++ fr"""FROM""" ++ Fragment.const(s""""$tableName"""") ++ fr"""WHERE id = $id""")
      .queryWithLogHandler[Model](logHandler).unique
  }

  def selectById[F[Model] : DAOTransactor : ErrorBracket](id: Long): F[Model] = complete(selectByIdQuery(id))

  private def deleteByIdQuery(id: Long): ConnectionIO[Int] = {
    (fr"""DELETE FROM""" ++ Fragment.const(s""""$tableName"""") ++ fr"""WHERE id = $id""")
      .updateWithLogHandler(logHandler).run
  }

  def deleteById[F[_] : DAOTransactor : ErrorBracket](id: Long): F[Int] = complete(deleteByIdQuery(id))

  private def updateByIdQuery(id: Long)(update: Patch): ConnectionIO[Model] = {
    val updates = members[Patch].zip(update.productIterator)
    val updateFragment = Fragment.const(updates.foldLeft(Vector.empty[String]) {
      case (acc, (key, Some(value))) => acc :+ (s"""$key = ${escapeLiteral(value)}""")
      case (acc, (_, None)) => acc
    }.mkString(","))

    val query = (fr"""UPDATE""" ++ Fragment.const(s""""$tableName"""") ++ fr"""SET""" ++ updateFragment ++ fr"""WHERE id = $id""")
    query.updateWithLogHandler(logHandler).withUniqueGeneratedKeys[Model](members[Model]: _*)
  }

  def updateById[F[Model]: DAOTransactor : ErrorBracket](id: Long)(update: Patch): F[Model] = complete(updateByIdQuery(id)(update))

}
