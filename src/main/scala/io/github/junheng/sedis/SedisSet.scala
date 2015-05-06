package io.github.junheng.sedis

import java.util

import org.json4s.Formats
import redis.clients.jedis.{Tuple, JedisPool}

import scala.collection.mutable
import scala.collection.JavaConversions._

case class SedisSet(id: String, pool: JedisPool)(implicit formats: Formats = Sedis.formats) extends JedisResource(pool) with mutable.Iterable[String] {
  def del(members: String*) = {
    closable { jedis =>
      jedis.srem(id, members: _*)
    }
  }

  def add(members: String*): Unit = {
    closable { jedis =>
      jedis.sadd(id, members: _*)
    }
  }

  def isMember(member: String): Boolean = {
    closable { jedis =>
      jedis.sismember(id, member)
    }
  }

  override def size(): Int = scard().toInt

  def scard(): Long = {
    closable { jedis =>
      jedis.scard(id)
    }
  }

  def move(destId: String, member: String): Unit = {
    if (id == s"${destId}-set")                  // FixMe, with -set suffix,,
      return

    closable { jedis =>
      jedis.smove(id, s"${destId}-set", member)  // FixMe, with -set suffix,,
    }
  }

  def move(destSet: SedisSet, member: String): Unit = {
    if (this.id == destSet.id)
      return

    closable { jedis =>
      jedis.smove(id, destSet.id, member)
    }
  }

  override def iterator: Iterator[String] = SedisSetIterator(id, pool)
}

case class SedisSetIterator(id: String, pool: JedisPool)(implicit formats: Formats = Sedis.formats) extends JedisResource(pool) with Iterator[String] {

  var cursor = "0"

  var isEnd = false

  val cache = mutable.Queue[String]()

  private def update() = {
    if (cache.isEmpty && !isEnd) {
      scan().foreach(cache.enqueue(_))
    }
    cache
  }

  private def scan(): util.List[String] = {
    closable { jedis =>
      val result = jedis.sscan(id, cursor)
      isEnd = result.getStringCursor == "0"
      cursor = result.getStringCursor
      if (result.getResult.isEmpty && !isEnd) scan()
      else result.getResult
    }
  }

  override def hasNext: Boolean = update().nonEmpty

  override def next(): (String) = update().dequeue()
}