package io.github.junheng.sedis

import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats, Serializer}
import redis.clients.jedis._

class Sedis(pool: JedisPool) extends JedisResource(pool) {

  def objectQueue(id: String) = SedisObjectQueue(s"$id-object-queue", pool)

  def queue(id: String) = SedisQueue(s"$id-queue", pool)

  def hash[T <: AnyRef : Manifest](id: String) = SedisHashSet[T](s"$id-hash", pool)

  def sortedSet(id: String) = SedisSortedSet(s"$id-sorted_set", pool)

  def put(key: String, value: String) = {
    closable { jedis =>
      jedis.sadd(key, value)
    }
  }

  def exists(key: String): Boolean = {
    closable { jedis =>
      jedis.exists(key)
    }
  }

  implicit val imported = Sedis.formats

  /**
   * publish event to target channel
   * @param payload event's payload
   * @param channel target channel
   */
  def publish(payload: AnyRef, channel: String) = {
    closable { jedis =>
      jedis.publish(s"channel_$channel", write(payload))
    }
  }

  /**
   * subscribe the event
   * @param process (channel, payload) => _
   * @param channels listened channels
   * @tparam T payload type
   */
  def subscribe[T <: AnyRef : Manifest](process: (String, T) => Unit, channels: String*) = {
    closable { jedis =>
      jedis.subscribe(
        new JedisPubSub {
          override def onMessage(channel: String, message: String): Unit = process(channel, read[T](message))
        },
        channels.map(x => s"channel_$x"): _ *
      )
    }
  }
}


object Sedis {
  var pool: JedisPool = null

  var formats: Formats = DefaultFormats

  def apply(index: Int = 0) = {
    new Sedis(pool)
  }

  def open(server: String, port: Int, poolSize: Int = 128, idleSize: Int = 16, timeout: Int = 0) = {
    val config = new JedisPoolConfig()
    config.setMaxTotal(poolSize)
    config.setMaxIdle(idleSize)
    pool = new JedisPool(config, server, port, timeout)
  }

  def format(serializers: Serializer[_]*) = serializers.foreach(x => formats += x)

  def enums(enums: Enumeration*) = enums.foreach(x => formats += new RangedEnumSerializer(x))

  def close() = pool.close()
}

class SedisConnectionBrokenException extends Exception











