package io.github.junheng.sedis

import org.json4s.Formats
import redis.clients.jedis._

import scala.collection.JavaConversions._

case class SedisQueue(id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def enqueue(payload: String) = {
    Sedis.check(jedis)
    jedis.lpush(id, payload)
  }

  def dequeue() = {
    Sedis.check(jedis)
    jedis.brpop(0, id).last
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
