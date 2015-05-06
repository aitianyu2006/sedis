package io.github.junheng.sedis

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll

/**
 * Created by qyou on 15/5/6.
 */
@RunWith(classOf[JUnitRunner])
class SedisSetTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll {

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

  test("test Set") {
    val set = Sedis().set("MySet")

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
