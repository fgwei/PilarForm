package org.sireum.pilarform.lexer

import org.sireum.pilarform._
import org.sireum.pilarform.lexer.Tokens._
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.TestFailedException
import org.scalatest.TestPendingException
import org.sireum.pilarform.util.Utils._

class RedundantSemicolonDetectorTest extends FlatSpec with ShouldMatchers {

  implicit def stringToCheckable(s: String)(implicit pilarVersion: String = PilarVersions.DEFAULT_VERSION) =
    new { def check() = checkSemis(s, pilarVersion) }; // Expected redundant semicolons are indicated with <;>

  """
    class A { 
      def foo = 42<;>
      def bar = 123; def baz = 1234 
    }<;>
  """.check();

  """
    { 
      println("Foo")<;>
    }
  """.check();

  """
    class A { 
      for (
        x <- 1 to 10; 
        y <- 1 to 10
      ) yield x + y<;>
    }
  """.check()

  {
    implicit val pilarVersion = "4.0";
    """
      s"my name is ${person.name<;>}"
    """.check
  }

  private def checkSemis(encodedSource: String, pilarVersion: String) {
    val ordinarySource = encodedSource.replace("<;>", ";")
    val semis = RedundantSemicolonDetector.findRedundantSemis(ordinarySource, pilarVersion)
    val encodedSourceAgain = semis.reverse.foldLeft(ordinarySource) { (s, semi) ⇒ replaceRange(s, semi.range, "<;>") }
    encodedSourceAgain should equal(encodedSource)
  }

}