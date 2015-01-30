package io.github.junheng.sedis

import redis.clients.jedis._

import scala.collection.mutable

case class SedisSortedSet(id: String, jedis: Jedis) extends mutable.Iterable[(String, Double)] {

  def update(member: String, score: Double) = jedis.zadd(id, score, member)

  def del(members: String*) = jedis.zrem(id, members: _*)

  def rank(member: String) = jedis.zrank(id, member)

  def clear() = jedis.del(id)

  def size(start: Double, end: Double): Int = jedis.zcount(id, start, end).toInt

  override def size: Int = jedis.zcount(id, "-inf", "+inf").toInt

  override def iterator: Iterator[(String, Double)] = SedisSortedSetIterator(id, jedis)
}
