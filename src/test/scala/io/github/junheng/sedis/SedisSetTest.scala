package io.github.junheng.sedis

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll

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
}
