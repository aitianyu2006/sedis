package io.github.junheng.sedis

import redis.clients.jedis._

import scala.collection.mutable

case class SedisSortedSet(id: String, jedis: Jedis) extends mutable.Iterable[(String, Double)] {

  def update(member: String, score: Double) = {
    Sedis.check(jedis)
    jedis.zadd(id, score, member)
  }

  def del(members: String*) = {
    Sedis.check(jedis)
    jedis.zrem(id, members: _*)
  }

  def rank(member: String) = {
    Sedis.check(jedis)
    jedis.zrank(id, member)
  }

  def clear() = {
    Sedis.check(jedis)
    jedis.del(id)
  }

  def size(start: Double, end: Double): Int = {
    Sedis.check(jedis)
    jedis.zcount(id, start, end).toInt
  }

  override def size: Int = {
    Sedis.check(jedis)
    jedis.zcount(id, "-inf", "+inf").toInt
  }

  override def iterator: Iterator[(String, Double)] = {
    Sedis.check(jedis)
    SedisSortedSetIterator(id, jedis)
  }
}
