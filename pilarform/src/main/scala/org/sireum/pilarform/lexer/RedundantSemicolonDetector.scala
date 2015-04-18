package org.sireum.pilarform.lexer

import org.sireum.pilarform.util.Range
import org.sireum.pilarform.util.Utils._
import org.sireum.pilarform.util.TextEdit
import org.sireum.pilarform.util.TextEditProcessor
import org.sireum.pilarform.PilarVersions

object RedundantSemicolonDetector {

  /**
   * @return all semicolons in the source that could safely be removed without changing the meaning
   *  of the program.
   */
  def findRedundantSemis(source: String, pilarVersion: String = PilarVersions.DEFAULT_VERSION): List[Token] = {

    def isRedundant(semi: Token, index: Int): Boolean = {
      val sourceWithoutSemi = deleteRange(source, semi.range)
      val tokensWithoutSemi = PilarLexer.tokenise(sourceWithoutSemi, forgiveErrors = true, pilarVersion = pilarVersion)
      val replacementToken = tokensWithoutSemi(index)
      replacementToken.isNewline || replacementToken.tokenType == Tokens.EOF || replacementToken.tokenType == Tokens.RBRACE
    }

    PilarLexer.tokenise(source, forgiveErrors = true, pilarVersion = pilarVersion).zipWithIndex.collect {
      case (token, index) if token.tokenType == Tokens.SEMI && isRedundant(token, index) ⇒ token
    }

  }

  def removeRedundantSemis(s: String): String =
    TextEditProcessor.runEdits(s, getEditsToRemoveRedundantSemis(s))

  def getEditsToRemoveRedundantSemis(s: String): List[TextEdit] =
    findRedundantSemis(s).map(_.range).map(TextEdit.delete)

}