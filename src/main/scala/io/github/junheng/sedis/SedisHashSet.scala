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

  override def ++=(xs: TraversableOnce[(String, T)]): this.type = {
    import scala.collection.mutable.{Map => MMap}
    import scala.collection.JavaConverters._

    //val m = MMap.empty[String, String]
    val m = new java.util.HashMap[String, String]()
    xs.seq.foreach(x => {
      m.put(x._1, write(x._2))
    })

    closable { jedis =>
      jedis.hmset(id, m)
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

  def mget(keys: Seq[String]): Seq[String] = {
    import scala.collection.JavaConverters._
    if (keys == Nil)
      return Nil

    closable { jedis =>
      if (jedis.exists(id)) {
        val list: java.util.List[String] = jedis.hmget(id, keys: _*)
        list.asScala.toSeq.map(removeQuote(_))
      } else {
        Nil
      }
    }
  }

  private def removeQuote(str: String): String = {
    if (str == null)
      return null.asInstanceOf[String]
    if (str.length >= 2) {
      str.take(str.length - 1).drop(1)
    } else {
      // something wrong? and will cause index-out-of-bound exception
      str
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
