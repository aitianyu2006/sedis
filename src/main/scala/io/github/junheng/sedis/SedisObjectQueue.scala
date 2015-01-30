package io.github.junheng.sedis

import org.json4s.Formats
import org.json4s.jackson.Serialization._
import redis.clients.jedis._

import scala.collection.JavaConversions._

case class SedisObjectQueue(id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def enqueue(payload: AnyRef) = {
    Sedis.check(jedis)
    jedis.lpush(id, write(payload))
  }

  def dequeue[T <: AnyRef : Manifest]() = {
    Sedis.check(jedis)
    read[T](jedis.brpop(0, id).last)
  } //wait until available

  def clear() = {
    Sedis.check(jedis)
    jedis.del(id)
  }

  def size() = {
    Sedis.check(jedis)
    jedis.llen(id).toInt
  }
}
