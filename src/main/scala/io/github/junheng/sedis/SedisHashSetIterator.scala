package io.github.junheng.sedis

import java.util
import java.util.Map.Entry

import org.json4s.Formats
import org.json4s.jackson.Serialization._
import redis.clients.jedis._

import scala.collection.JavaConversions._
import scala.collection.mutable

case class SedisHashSetIterator[T <: AnyRef : Manifest](id: String, pool: JedisPool)(implicit formats: Formats = Sedis.formats) extends JedisResource(pool) with Iterator[(String, T)] {

  var cursor = "0"

  var isEnd = false

  val cache = mutable.Queue[(String, T)]()

  private def update() = {
    if (cache.isEmpty && !isEnd) {
      scan().foreach(x => {
        val value: T = read[T](x.getValue)
        cache.enqueue((x.getKey, value))
      })
    }
    cache
  }

  private def scan(): util.List[Entry[String, String]] = {
    closable { jedis =>
      val result: ScanResult[Entry[String, String]] = jedis.hscan(id, cursor)
      isEnd = result.getStringCursor == "0"
      cursor = result.getStringCursor
      if (result.getResult.isEmpty && !isEnd) scan()
      else result.getResult
    }
  }

  override def hasNext: Boolean = update().nonEmpty

  override def next(): (String, T) = update().dequeue()
}
