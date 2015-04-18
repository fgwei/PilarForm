package org.sireum.pilarform.lexer

import org.sireum.pilarform.lexer.Tokens._

object Keywords {

  def apply(s: String): Option[TokenType] = keywords get s

  private val keywords = Map(
    "record" -> RECORD,
    "procedure" -> PROCEDURE,
    "catch" -> CATCH,
    "extends" -> EXTENDS,
    "if" -> IF,
    "then goto" -> THEN_GOTO,
    "goto" -> GOTO,
    "call" -> CALL,
    "new" -> NEW,
    "return" -> RETURN,
    "switch" -> SWITCH,
    "else" -> ELSE,
    "throw" -> THROW,
    "true" -> TRUE,
    "false" -> FALSE,
    "null" -> NULL)

}
