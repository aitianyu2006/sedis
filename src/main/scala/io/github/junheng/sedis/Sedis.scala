package io.github.junheng.sedis

import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats, Serializer}
import redis.clients.jedis._
import scala.collection.mutable.{Map => MMap, ListBuffer}

class Sedis(val pool: JedisPool) extends JedisResource(pool) {
  /** Script Mapping as (id -> sha1) */
  @volatile var scriptCache = Map.empty[String, String]

  def objectQueue(id: String) = SedisObjectQueue(s"$id-object-queue", pool)

  def queue(id: String) = SedisQueue(s"$id-queue", pool)

  def hash[T <: AnyRef : Manifest](id: String) = SedisHashSet[T](s"$id-hash", pool)

  def sortedSet(id: String) = SedisSortedSet(s"$id-sorted_set", pool)

  def set(id: String) = SedisSet(s"$id-set", pool)

  def script(id: String) = SedisScript(s"$id", this)

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

  /**
   * mexists implemted using pipeline
   * For now the speed is slow than mexists implemented using script.
   * @param keys
   * @return
   */
  def mexists(keys: Seq[String]): Seq[Boolean] = {
    if (keys == null) return null
    if (keys == Nil) return Nil

    closable { jedis =>
      val pipeline = jedis.pipelined()

      val resp = keys.map(x => {
        pipeline.exists(x)
      })
      pipeline.sync()

      resp.map(x => {
        x.get().booleanValue()
      })
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

  def close(): Unit = pool.close()

  def scriptExists(scriptId: String, sha1: String): Boolean = {
    this.scriptCache.getOrElse(scriptId, "") == sha1
  }

  def scriptUpdate(scriptId: String, sha1: String): Unit = {
    scriptCache += scriptId -> sha1
  }
}

object Sedis {
  //val DEFAULT_NAME = "default"

  //val pools = MMap.empty[String, JedisPool]

  //val scriptsCache = MMap.empty[String, MMap[String, String]]  // cache for every instanceName, id : sha

  var formats: Formats = DefaultFormats

//  def apply(name: String = DEFAULT_NAME) = {
//    val pool = pools(name)
//    new Sedis(pool)
//  }

//  def open(server: String, port: Int): Sedis = {
//    open(server, port)
//  }

//  def open(server: String, port: Int, poolSize: Int): Sedis = {
//    open(server, port, poolSize = poolSize)
//  }

  def open(server: String, port: Int, db: Int = 0, poolSize: Int = 128, idleSize: Int = 16, timeout: Int = 0): Sedis = {
//    if (pools.contains(name)) {
//      throw new IllegalStateException(s"pool name with ${name} already exists.")
//    }
    val pool = newPool(server, port, db, poolSize, idleSize, timeout)
    //pools += (name -> pool)
    new Sedis(pool)
  }

  def newPool(server: String, port: Int, db: Int = 0, poolSize: Int = 128, idleSize: Int = 16, timeout: Int = 0) = {
    val config = new JedisPoolConfig()
    config.setMaxTotal(poolSize)
    config.setMaxIdle(idleSize)
    new JedisPool(config, server, port, timeout, null, db, null)
  }

  def format(serializers: Serializer[_]*) = serializers.foreach(x => formats += x)

  def enums(enums: Enumeration*) = enums.foreach(x => formats += new RangedEnumSerializer(x))

//  def close() = pools.foreach(x => {
//    x._2.close()
//  })
}

class SedisConnectionBrokenException extends Exception











