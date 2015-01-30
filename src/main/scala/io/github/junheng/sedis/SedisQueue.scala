package io.github.junheng.sedis

import org.json4s.Formats
import redis.clients.jedis._

import scala.collection.JavaConversions._

case class SedisQueue(id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def enqueue(payload: String) = jedis.lpush(id, payload)

  def dequeue() = jedis.brpop(0, id).last //wait until available

  def clear() = jedis.del(id)

  def size() = jedis.llen(id).toInt
}
