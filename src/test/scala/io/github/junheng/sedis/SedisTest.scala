package io.github.junheng.sedis

class SedisTest extends RedisTestSupport {
  Sedis.open("192.168.10.15", 6379)

  "SedisTest" should {
    "can operate queue" in {
      val queue = getSedis.objectQueue("test")
      val example = SedisTestObject("test1", "test2")
      queue.enqueue(example)
      val data = queue.dequeue[SedisTestObject]()
      example must beEqualTo(data)
    }
    "can operate hash map" in {
      val map = getSedis.hash[SedisTestObject]("test")
      map.put("1", SedisTestObject("test1", "result1"))
      map.put("2", SedisTestObject("test2", "result2"))
      map.put("3", SedisTestObject("test3", "result3"))

      map("1") must beEqualTo(SedisTestObject("test1", "result1"))
      map("2") must beEqualTo(SedisTestObject("test2", "result2"))
      map("3") must beEqualTo(SedisTestObject("test3", "result3"))
      map.size must beEqualTo(3)
    }
    "can operate sorted set" in {
      val sortedSet = getSedis.sortedSet("test")
      sortedSet.update("test0", 0)
      sortedSet.update("test1", 1)
      sortedSet.update("test2", 2)
      sortedSet.update("test3", 3)

      sortedSet.size must beEqualTo(4)

      sortedSet.rank("test1") must beEqualTo(1)
    }
  }
}

case class SedisTestObject(field1: String, field2: String)
