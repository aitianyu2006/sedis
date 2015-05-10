package io.github.junheng.sedis

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll

@RunWith(classOf[JUnitRunner])
class SedisTest extends _DefaultSedisTest {
  test("test mexists") {
    //val keys = Seq("abc", "def", "xyz")
    val keys = (1 to 1000).map("Key-" + _)

    val start = System.currentTimeMillis()
    val vals = sedis.mexists(keys)
    val stop = System.currentTimeMillis()
    println(s"cost: ${stop - start} ms, vals: ${vals}")
  }
}
