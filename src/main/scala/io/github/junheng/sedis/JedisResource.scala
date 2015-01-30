package io.github.junheng.sedis

import org.json4s.Formats
import redis.clients.jedis._

abstract class JedisResource(jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def close() = jedis.close()

  def select(index: Int): Boolean = jedis.select(index) == "OK"

  def flush() = jedis.flushDB()
}
