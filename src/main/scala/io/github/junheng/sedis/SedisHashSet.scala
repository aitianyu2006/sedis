package io.github.junheng.sedis

import org.json4s.Formats
import org.json4s.jackson.Serialization._
import redis.clients.jedis._

import scala.collection.mutable

case class SedisHashSet[T <: AnyRef : Manifest](id: String, pool: JedisPool)(implicit formats: Formats = Sedis.formats) extends JedisResource(pool) with mutable.Map[String, T] {

  override def +=(kv: (String, T)): this.type = {
    closable { jedis =>
      jedis.hset(id, kv._1, write(kv._2))
      this
    }
  }

  override def -=(key: String): this.type = {
    closable { jedis =>
      jedis.hdel(id, key)
      this
    }
  }

  override def get(key: String): Option[T] = {
    closable { jedis =>
      if (jedis.exists(id)) {
        jedis.hget(id, key) match {
          case content: String => Option(read[T](content))
          case _ => None
        }
      } else {
        None
      }
    }
  }

  override def clear() = {
    closable { jedis =>
      jedis.del(id)
    }
  }

  override def size: Int = {
    closable { jedis =>
      jedis.hlen(id).toInt
    }
  }

  override def iterator: Iterator[(String, T)] = SedisHashSetIterator[T](id, pool)
}
