package io.github.junheng.sedis

import java.util

import redis.clients.jedis._

import scala.collection.JavaConversions._
import scala.collection.mutable

case class SedisSortedSetIterator(id: String, pool: JedisPool) extends JedisResource(pool) with Iterator[(String, Double)] {

  var cursor = "0"

  var isEnd = false

  val cache = mutable.Queue[(String, Double)]()

  private def update() = {
    if (cache.isEmpty && !isEnd) {
      scan().foreach(x => cache.enqueue((x.getElement, x.getScore)))
    }
    cache
  }

  private def scan(): util.List[Tuple] = {
    closable { jedis =>
      val result = jedis.zscan(id, cursor)
      isEnd = result.getStringCursor == "0"
      cursor = result.getStringCursor
      if (result.getResult.isEmpty && !isEnd) scan()
      else result.getResult
    }
  }

  override def hasNext: Boolean = update().nonEmpty

  override def next(): (String, Double) = update().dequeue()
}
