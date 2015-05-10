package io.github.junheng.sedis

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll

@RunWith(classOf[JUnitRunner])
class SedisScriptTest extends _DefaultSedisTest {

  val SCRIPT_MEXISTS =
    """
      |local count = 0
      |local VALS = KEYS
      |for k,v in pairs(KEYS) do
      |  VALS[k] = redis.call('EXISTS',KEYS[k])
      |end
      |return VALS
      |""".stripMargin.trim()

  test("sha1") {
    val script = sedis.script("myscript");

    val str = SCRIPT_MEXISTS

    val shaServer = script.load(str)

    val sha1 = SedisScript.sha1sum(str)

    println(s"shaServer: ${shaServer}, sha1: ${sha1}")
    assert(shaServer == sha1)
  }

  test("test...") {
    val script = sedis.script("myscript");

    //val keys = Seq("abc", "def", "xyz")
    val keys = (1 to 1000).map("Key-" + _)

    script.load(SCRIPT_MEXISTS)

    val start = System.currentTimeMillis()
    val res = script.eval(keys: _*)
    val stop  = System.currentTimeMillis()
    println(s"cost: ${stop - start} ms, res: ${res}")

    val singleres = script.eval("abc")
    println(singleres)
  }
}
