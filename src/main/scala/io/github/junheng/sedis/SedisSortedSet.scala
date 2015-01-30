package io.github.junheng.sedis

import redis.clients.jedis._

import scala.collection.mutable

case class SedisSortedSet(id: String, pool: JedisPool) extends JedisResource(pool) with mutable.Iterable[(String, Double)] {

  def update(member: String, score: Double) = {
    closable { jedis =>
      jedis.zadd(id, score, member)
    }
  }

  def del(members: String*) = {
    closable { jedis =>
      jedis.zrem(id, members: _*)
    }
  }

  def rank(member: String) = {
    closable { jedis =>
      jedis.zrank(id, member)
    }
  }

  def clear() = {
    closable { jedis =>
      jedis.del(id)
    }
  }

  def size(start: Double, end: Double): Int = {
    closable { jedis =>
      jedis.zcount(id, start, end).toInt
    }
  }

  override def size: Int = {
    closable { jedis =>
      jedis.zcount(id, "-inf", "+inf").toInt
    }
  }

  override def iterator: Iterator[(String, Double)] = SedisSortedSetIterator(id, pool)
}
