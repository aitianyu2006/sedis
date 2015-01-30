package io.github.junheng.sedis

import org.json4s.Formats
import redis.clients.jedis._
import scala.collection.JavaConversions._

case class SedisQueue(id: String, pool: JedisPool)(implicit formats: Formats = Sedis.formats) extends JedisResource(pool) {

  def enqueue(payload: String) = {
    closable { jedis =>
      jedis.lpush(id, payload)
    }
  }

  //wait until available
  def dequeue(): String = {
    closable { jedis =>
      jedis.brpop(0, id).last
    }
  }

  def clear() = {
    closable { jedis =>
      jedis.del(id)
    }
  }

  def size() = {
    closable { jedis =>
      jedis.llen(id).toInt
    }
  }
}
