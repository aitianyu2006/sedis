package io.github.junheng.sedis

import java.util

import org.json4s.Formats
import redis.clients.jedis.JedisPool

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.{Try, Success, Failure}

/**
 * This is a limited implentation based on specific use-case.
 * Implemented a eval-by-name approach, so we can pass in an id instead remember the sha1.
 */
case class SedisScript(id: String, sedis: Sedis)(implicit formats: Formats = Sedis.formats) extends JedisResource(sedis.pool) {
  import SedisScript._
  private var _scr: String = _
  private var _sha: String = _

  def scr = _scr
  def sha = _sha

  /**
   * Load a script, and the result sha1 will be cached locally.
   * @param script
   * @return
   */
  def load(script: String): String = {
    if (script == null || script.trim() == "") return null

    this._scr = script.trim()

    val theSha = sha1sum(this.scr)
    if (sedis.scriptExists(id, theSha)) {
      this._sha = theSha
    }

    closable { jedis =>
      _sha = jedis.scriptLoad(this.scr)
      sedis.scriptUpdate(id, _sha)
    }
    _sha
  }

  /**
   * Call against cached sha1, and it can be cached by @{load(String)}
   * @param params
   * @return
   */
  def evalsha(params: Seq[String]): AnyRef = {
    import scala.collection.JavaConverters._
    checkSha

    closable { jedis =>
      retry(3) {
        jedis.evalsha(_sha, params.size, params: _*).asInstanceOf[util.ArrayList[_]].asScala
      }{
        reload() // in case the script is flushed by SCRIPT FLUSH
      } match {
        case Success(r) => r
        case Failure(f) => null
      }
    }
  }

  def eval(script: String)(params: Seq[String]): AnyRef = {
    load(script)
    evalsha(params)
  }

  def exists(): Boolean = {
    checkSha

    closable { jedis =>
      jedis.scriptExists(_sha)
    }
  }

  def reload() = this.load(this.scr)

  private def checkSha: Unit = {
    if (_sha == null)
      throw new IllegalStateException("Script should be loaded first before eval.")
  }
}

object SedisScript {

  // helper methods.
  def sha1sum(script: String) = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    md.reset()
    md.update(script.getBytes("UTF-8"))
    bytesToHex(md.digest(), hexArrayL)
  }

  val hexArrayL = "0123456789abcdef".toCharArray()
  val hexArrayU = "0123456789ABCDEF".toCharArray()

  def bytesToHex(bytes: Array[Byte], hexArr: Array[Char]): String = {
    val buff = StringBuilder.newBuilder
    for (c <- bytes) {
      val theBit = c & 0xFF
      buff += hexArr(theBit >>>4)
      buff += hexArr(theBit & 0x0F)
    }
    buff.toString()
  }

  /**
   * Retry methods.
   * @param times, times to retry, unit should be run (times + 1) times.
   * @param unit, the process to run.
   * @param fix, the fix method tu run, when Failure occured.
   * @tparam R, the result type.
   * @tparam S, the fix method result type.
   * @return, The Result that unit returns.
   */
  @tailrec
  def retry[R, S](times: Int)(unit: => R)(fix: => S): Try[R] = {
    Try{
      unit
    } match {
      case Success(r) => Success(r)
      case Failure(f) =>
        if (times > 0 || times == -1){
          fix
          retry(times - 1)(unit)(fix)}
        else Failure(f)
    }
  }
}