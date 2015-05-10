package io.github.junheng.sedis

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}

/**
 * Default Base Test
 */
trait _DefaultSedisTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll {
  var sedis: Sedis = _

  override def beforeEach() {
    //println("beforeEach")
  }

  override def afterEach() {
    //println("afterEach")
  }

  override def beforeAll() {
    sedis = Sedis.open("127.0.0.1", 6379)
  }

  override def afterAll() {
    sedis.close()
  }
}
