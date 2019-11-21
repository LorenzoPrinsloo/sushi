package io.roflsoft.db

import cats.effect.Bracket
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.Read
import enumeratum.EnumEntry
import io.roflsoft.reflection.utils.{className, members}

import scala.reflect.runtime.universe.TypeTag
import scala.language.higherKinds

abstract class DAO[M <: Product : TypeTag : Read](tableName: String) {

  type DAOTransactor[F[M]] = Transactor[F] { type A = Unit }
  type ErrorBracket[F[M]] = Bracket[F, Throwable]

  def insert[F[M] : DAOTransactor : ErrorBracket](m: M): F[M] = {
    val fieldValues = m.productIterator.map {
      case s: String => s"'$s'"
      case e: EnumEntry => s"'${e.entryName}'"
      case f => s"$f"
    }.mkString(",")

    val insert = fr"""INSERT INTO""" ++ Fragment.const(s""" "$tableName" """) ++ fr"""(""" ++ Fragment.const(members[M].mkString(",")) ++ fr""") VALUES (""" ++ Fragment.const(fieldValues) ++ fr""")"""
    println(insert)
    complete(insert.update.withUniqueGeneratedKeys[M](members[M]: _*))
  }


  def selectAll[F[M] : DAOTransactor : ErrorBracket]: F[List[M]] = {
    val select = fr"""SELECT""" ++ Fragment.const(members[M].mkString(",")) ++ fr"""FROM""" ++ Fragment.const(s""""$tableName"""")
    complete(select.query[M].to[List])
  }

  private def selectByIdQuery(id: Long): ConnectionIO[M] = {
    (fr"""SELECT""" ++ Fragment.const(members[M].mkString(",")) ++ fr"""FROM""" ++ Fragment.const(s""""$tableName"""") ++ fr"""WHERE id = $id""").query[M].unique
  }

  def selectById[F[M] : DAOTransactor : ErrorBracket](id: Long): F[M] = complete(selectByIdQuery(id))

  private def deleteByIdQuery(id: Long): ConnectionIO[Int] = {
    (fr"""DELETE FROM""" ++ Fragment.const(s""""$tableName"""") ++ fr"""WHERE id = $id""").update.run
  }

  def deleteById[F[_] : DAOTransactor : ErrorBracket](id: Long): F[Int] = complete(deleteByIdQuery(id))

}
