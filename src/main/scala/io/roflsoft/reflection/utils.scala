package io.roflsoft.reflection

import scala.reflect.runtime.universe._

object utils {

  def members[A : TypeTag]: Seq[String] = {
    typeOf[A].decls.sorted.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.name.toString
    }
  }

  def className[A: TypeTag]: String = {
    typeOf[A].toString
  }
}
