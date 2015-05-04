package io.github.junheng.sedis


import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import redis.clients.jedis.{JedisPoolConfig, JedisPool}

/**
 * Test of SedisHashSetTest
 */
@RunWith(classOf[JUnitRunner])
class SedisHashSetTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll {

  override def beforeEach() {
    //println("beforeEach")
  }

  override def afterEach() {
    //println("afterEach")
  }

  override def beforeAll() {
    Sedis.open("localhost", 6379)
  }

  override def afterAll() {
    //println("afterAll")

  }

  test("test hmget") {
    val hash = Sedis().hash[String]("SedisHashSetTest-test-hmget")


    hash ++= Map(
      "Key1" -> "val1",
      "Key2" -> "val2"
    )

    val get1 = hash.mget(Nil)
    println(s"get1: ${get1}")

    val get11 = hash.mget(Seq.empty[String])
    println(s"get11: ${get11}")

    val get2 = hash.mget(Seq("Key1", "Key2"))
    println(s"get2: ${get2(0).length}")
    assert(get2 == Seq("val1", "val2"))

    val get3 = hash.mget(Seq("Key3"))
    println(s"get3: ${get3}")

    val get4 = hash.mget(Seq("Key1", "Key2", "Key3"))
    println(s"get4: ${get4}")
  }

  test("jedis hmget") {
    val pool = new JedisPool(new JedisPoolConfig(),"localhost", 6379)

    val jedis = pool.getResource();
    val list: java.util.List[String] = jedis.hmget("SedisHashSetTest-test-hmget-hash", "Key1", "Key2")
    println(s"list: ${list}")
    println(s"list: ${list.get(0).length}")

    import scala.collection.JavaConverters._
    val list2 = list.asScala
    val list3 = list2.map(x => {
      if (x != null) {
        x.take(x.length - 1).drop(1)
      } else {
        null.asInstanceOf[String]
      }
    })

    println(s"list: ${list3}")
    println(s"list: ${list3(0).length}")
  }
}
