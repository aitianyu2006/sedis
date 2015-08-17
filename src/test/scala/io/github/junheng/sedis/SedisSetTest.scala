package io.github.junheng.sedis

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class SedisSetTest extends _DefaultSedisTest {
  test("test Set") {
    val set = sedis.set("MySet")

    set.add("ok", "yes", "abc", "def", "xyz")

    val it = set.iterator

    while(it.hasNext) {
      val e = it.next()
      println(s"e: ${e}")
    }

    set.move("mysetdest", "ok")

    val it2 = set.iterator
    while (it2.hasNext) {
      val e = it2.next
      println(s"it2: ${e}")
    }
  }

  test("test Test O(1) ismember") {
    val maxCount = 10000000
    val testCount = 100
    val set = sedis.set("Set10m")
//    for (i ← 0 until maxCount) {
//      set.add(formatedKey(i))
//    }

    val mid = maxCount / 2
    // test check existance
    for (i ← 0 until testCount) {
      val keyId = Random.nextInt(maxCount) + mid
      val key = formatedKey(keyId)
      val start = System.currentTimeMillis()
      val res = set.isMember(key)
      val stop = System.currentTimeMillis()
      println(s"res: of key $key is $res Time to check: ${stop - start} ")
    }
  }

  @inline private def formatedKey(id: Int): String = s"SetMember-xxxxxx-abcdef-xxxxxxxx${id}"
}
