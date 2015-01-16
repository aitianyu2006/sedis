package io.github.junheng.sedis

import java.util
import java.util.Map.Entry

import org.json4s.jackson.Serialization._
import org.json4s.{Formats, DefaultFormats, Serializer}
import redis.clients.jedis._

import scala.collection.JavaConversions._
import scala.collection.mutable

class Sedis(jedis: Jedis) extends JedisResource(jedis) {

  def objectQueue(id: String) = SedisObjectQueue(s"object_queue_$id", jedis)

  def queue(id: String) = SedisQueue(s"queue_$id", jedis)

  def hash[T <: AnyRef : Manifest](id: String) = SedisHashSet[T](s"hash_$id", jedis)

  def sortedSet(id: String) = SedisSortedSet(s"sorted_set_$id", jedis)

  def put(key: String, value: String) = jedis.sadd(key, value)

  def exists(key: String) = jedis.exists(key)

  implicit val imported = Sedis.formats

  /**
   * publish event to target channel
   * @param payload event's payload
   * @param channel target channel
   */
  def publish(payload: AnyRef, channel: String) = {
    jedis.publish(s"channel_$channel", write(payload))
  }

  /**
   * subscribe the event
   * @param process (channel, payload) => _
   * @param channels listened channels
   * @tparam T payload type
   */
  def subscribe[T <: AnyRef : Manifest](process: (String, T) => Unit, channels: String*) = {
    jedis.subscribe(
      new JedisPubSub {
        override def onMessage(channel: String, message: String): Unit = process(channel, read[T](message))
      },
      channels.map(x => s"channel_$x"): _ *
    )
  }
}

abstract class JedisResource(jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def close() = jedis.close()

  def select(index: Int): Boolean = jedis.select(index) == "OK"

  def flush() = jedis.flushDB()
}

object Sedis {
  var pool: JedisPool = null

  var formats: Formats = DefaultFormats

  def apply(index: Int = 0) = {
    val resource: Jedis = pool.getResource
    resource.select(index)
    new Sedis(resource)
  }

  def open(server: String, port: Int, poolSize: Int = 128, idleSize: Int = 16) = {
    val config = new JedisPoolConfig()
    config.setMaxTotal(poolSize)
    config.setMaxIdle(idleSize)
    config.setMaxWaitMillis(1000 * 10)
    pool = new JedisPool(config, server, port)
  }

  def format(serializers: Serializer[_]*) = serializers.foreach(x => formats += x)

  def close() = pool.close()
}


case class SedisObjectQueue(id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def enqueue(payload: AnyRef) = jedis.lpush(id, write(payload))

  def dequeue[T <: AnyRef : Manifest]() = read[T](jedis.brpop(0, id).last) //wait until available

  def clear() = jedis.del(id)

  def size() = jedis.llen(id).toInt
}

case class SedisQueue(id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) {

  def enqueue(payload: String) = jedis.lpush(id, payload)

  def dequeue() = jedis.brpop(0, id).last //wait until available

  def clear() = jedis.del(id)

  def size() = jedis.llen(id).toInt
}

case class SedisSortedSet(id: String, jedis: Jedis) extends mutable.Iterable[(String, Double)] {

  def update(member: String, score: Double) = jedis.zadd(id, score, member)

  def del(members: String*) = jedis.zrem(id, members: _*)

  def rank(member: String) = jedis.zrank(id, member)

  def clear() = jedis.del(id)

  def size(start: Double, end: Double): Int = jedis.zcount(id, start, end).toInt

  override def size: Int = jedis.zcount(id, "-inf", "+inf").toInt

  override def iterator: Iterator[(String, Double)] = SedisSortedSetIterator(id, jedis)
}

case class SedisSortedSetIterator(id: String, jedis: Jedis) extends Iterator[(String, Double)] {

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
    val result = jedis.zscan(id, cursor)
    isEnd = result.getStringCursor == "0"
    cursor = result.getStringCursor
    if (result.getResult.isEmpty && !isEnd) scan()
    else result.getResult
  }

  override def hasNext: Boolean = update().nonEmpty

  override def next(): (String, Double) = update().dequeue()
}



case class SedisHashSetIterator[T <: AnyRef : Manifest](id: String, jedis: Jedis)(implicit formats: Formats = Sedis.formats) extends Iterator[(String, T)] {

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
    val result: ScanResult[Entry[String, String]] = jedis.hscan(id, cursor)
    isEnd = result.getStringCursor == "0"
    cursor = result.getStringCursor
    if (result.getResult.isEmpty && !isEnd) scan()
    else result.getResult
  }

  override def hasNext: Boolean = update().nonEmpty

  override def next(): (String, T) = update().dequeue()
}