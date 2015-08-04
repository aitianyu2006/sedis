package io.github.junheng.sedis

import java.util.UUID

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

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

  test("test del") {
    val str = UUID.randomUUID().toString
    assert(!sedis.exists(str))
    sedis.put(str, str)
    assert(sedis.exists(str))
    sedis.del(str)
    assert(!sedis.exists(str))

    val setName = UUID.randomUUID().toString
    val set = sedis.set(setName)
    assert(!sedis.exists(set.id))
    set.add("1")
    set.add("2")
    set.add("3")
    set.add("1")
    assert(set.size() == 3)
    assert(sedis.exists(set.id))
    sedis.del(set.id)
    assert(!sedis.exists(set.id))
  }

  test("test rename") {
    val setName = UUID.randomUUID().toString
    val set = sedis.set(setName)
    set.add("test1")
    assert(sedis.exists(set.id))

    val set2 = sedis.set(setName + "-new")
    sedis.rename(set.id, set2.id)

    assert(!sedis.exists(set.id))
    assert(sedis.exists(set2.id))

    // cleanup
    //sedis.del(set2.id)
  }
}
