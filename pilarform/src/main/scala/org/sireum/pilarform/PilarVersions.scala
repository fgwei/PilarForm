package org.sireum.pilarform

import scala.util.Properties
import scala.math.Ordering
import org.sireum.pilarform.util.Utils._

object PilarVersion {
  def parseOrDefault(s: String): PilarVersion = parse(s).getOrElse(PilarVersions.DEFAULT)

  def parse(s: String): Option[PilarVersion] = {
    s.toIntOpt.map {
      case v => PilarVersion(v)
    }
  }
    
}

case class PilarVersion(x : Int) {

  private def version = x

  def compare(that: PilarVersion) : Boolean = this.x == that.x

  override def toString = x.toString()

}

object PilarVersions {

  val Pilar_4 = PilarVersion.parse("4").get

  lazy val DEFAULT_VERSION = "4"

  lazy val DEFAULT = PilarVersion.parse(DEFAULT_VERSION).get

}