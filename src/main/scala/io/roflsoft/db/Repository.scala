package io.roflsoft.db

import fs2._
import doobie._

trait Repository[A] extends RORepository[A] {

  def add(item: A): ConnectionIO[A]

  def add(items: A*): Stream[ConnectionIO, A]

  def update(item: A): ConnectionIO[A]

  def remove(item: A): ConnectionIO[Int]

  def removeByQuery(q: Query): ConnectionIO[Int]
}
