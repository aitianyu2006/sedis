package io.github.junheng.sedis

import org.json4s.Formats
import redis.clients.jedis._

abstract class JedisResource(pool: JedisPool)(implicit formats: Formats = Sedis.formats) {
  def closable[T](process: (Jedis) => T) = {
    var resource: Jedis = null
    try {
      resource = pool.getResource
      process(resource)
    } finally {
      pool.returnResource(resource)
    }
  }
}
