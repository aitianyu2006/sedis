package io.github.junheng.sedis

import org.specs2.mutable.{Specification, BeforeAfter}
import org.specs2.specification.{Fragments, Step, BeforeAfterExample}

abstract class RedisTestSupport(var indexOfDb:Int = 1) extends BeforeAllAfterAll {

  def getSedis = Sedis(indexOfDb)

  def before() = {
    val sedis = getSedis
    locateTestDatabase(sedis) //find test database start with 1
    setup()
    sedis.close()
  }

  def locateTestDatabase(sedis: Sedis): Unit = {
    if (sedis.select(indexOfDb)) {
      if (sedis.exists("unit_test_inbound")) {
        indexOfDb += 1
        locateTestDatabase(sedis)
      } else {
        sedis.put("unit_test_inbound", "true")
        sedis.select(indexOfDb)
      }
    } else {
      throw new Exception("no available redis database for test case")
    }
  }

  def after() = {
    val sedis = getSedis
    sedis.flush()
    sedis.close()
  }
}

trait BeforeAllAfterAll extends Specification {
  override def map(fragments: => Fragments) = Step(before()) ^ fragments ^ Step(after())

  protected def before()

  protected def after()

  def setup() = {}
}