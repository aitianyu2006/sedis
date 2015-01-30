package io.github.junheng.sedis

import org.json4s.Formats
import org.json4s.jackson.Serialization._
import redis.clients.jedis._

import scala.collection.JavaConversions._

case class SedisObjectQueue(id: String, pool: JedisPool)(implicit formats: Formats = Sedis.formats) extends JedisResource(pool) {

  def enqueue(payload: AnyRef) = {
    closable { jedis =>
      jedis.lpush(id, write(payload))
    }
  }

  //wait until available
  def dequeue[T <: AnyRef : Manifest](): T = {
    closable { jedis =>
      read[T](jedis.brpop(0, id).last)
    }
  }

  def clear() = {
    closable { jedis =>
      jedis.del(id)
    }
  }

  def size(): Long = {
    closable { jedis =>
      jedis.llen(id)
    }
  }
}
