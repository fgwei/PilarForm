package org.sireum.pilarform.lexer

import org.sireum.pilarform._
import org.sireum.pilarform.lexer.Tokens._
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.TestFailedException
import org.scalatest.TestPendingException
import java.io._

/**
 * Test full tokeniser, including newline inferencing.
 */
class NewlineInferencerTest extends FlatSpec with ShouldMatchers {

  implicit def string2TestString(s: String)(implicit forgiveErrors: Boolean = false, pilarVersion: PilarVersion = PilarVersions.DEFAULT) =
    new TestString(s, forgiveErrors, pilarVersion);

  """
     #L1.   switch  v7
                 | 1 => goto Lx
                 | else => goto Ly;""" shouldProduceTokens (
    LID, SWITCH, ID,
    OP, INTEGER_LITERAL, OP, GOTO, ID, NEWLINE,
    OP, ELSE, OP, GOTO, ID, COMMA)

  class TestString(s: String, forgiveErrors: Boolean = false, pilarVersion: PilarVersion = PilarVersions.DEFAULT) {

    def shouldProduceTokens(toks: TokenType*)() {
      check(s.stripMargin, toks.toList)
    }

    private def check(s: String, expectedTokens: List[TokenType]) {
      it should ("tokenise >>>" + s + "<<< as >>>" + expectedTokens + "<<< forgiveErrors = " + forgiveErrors + ", pilarVersion = " + pilarVersion) in {
        val actualTokens: List[Token] = PilarLexer.tokenise(s, forgiveErrors, pilarVersion.toString)
        val actualTokenTypes = actualTokens.map(_.tokenType)
        require(actualTokenTypes.last == EOF, "Last token must be EOF, but was " + actualTokens.last.tokenType)
        require(actualTokenTypes.count(_ == EOF) == 1, "There must only be one EOF token")
        val reconstitutedSource = actualTokens.init.map(_.rawText).mkString
        require(actualTokenTypes.init == expectedTokens, "Tokens do not match. Expected " + expectedTokens + ", but was " + actualTokenTypes.init)
      }
    }

  }

}

