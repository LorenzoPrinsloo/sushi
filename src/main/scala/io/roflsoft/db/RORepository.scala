package io.roflsoft.db

import fs2._
import doobie._

trait RORepository[A] {

  type Query

  def find(q: Query): Stream[ConnectionIO, A]
}
