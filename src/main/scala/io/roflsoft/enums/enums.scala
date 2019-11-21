package io.roflsoft

import enumeratum._

package object enums {
  abstract class Entry(override val entryName: String) extends EnumEntry with Product with Serializable
  trait Enum[A <: EnumEntry] extends enumeratum.Enum[A] with enumeratum.DoobieEnum[A] with enumeratum.CirceEnum[A]
}
