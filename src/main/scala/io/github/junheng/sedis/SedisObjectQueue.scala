package io.github.junheng.sedis

import org.json4s.Formats
import org.json4s.jackson.Serialization._
import redis.clients.jedis._

import scala.collection.JavaConversions._

case class SedisObjectQueue(id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def enqueue(payload: AnyRef) = jedis.lpush(id, write(payload))

  def dequeue[T <: AnyRef : Manifest]() = read[T](jedis.brpop(0, id).last) //wait until available

  def clear() = jedis.del(id)

  def size() = jedis.llen(id).toInt
}
