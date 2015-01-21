package io.github.junheng.sedis

import org.json4s.JsonAST.JInt
import org.json4s._

import scala.reflect.ClassTag

class RangedEnumSerializer[E <: Enumeration : ClassTag](enum: E) extends Serializer[E#Value] {

  import org.json4s.JsonDSL._

  val EnumerationClass = classOf[E#Value]

  private[this] def isValid(json: JValue) = json match {
    case JInt(value) => value <= enum.maxId && value >= enum.values.head.id
    case _ => false
  }

  def deserialize(implicit format: Formats):
  PartialFunction[(TypeInfo, JValue), E#Value] = {
    case (TypeInfo(EnumerationClass, _), json) if isValid(json) => json match {
      case JInt(value) => enum(value.toInt)
      case value => throw new MappingException(s"Can't convert $value to $EnumerationClass")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: E#Value => i.id
  }
}