package org.sireum.pilarform.lexer

import org.sireum.pilarform.lexer.Tokens._
import org.sireum.pilarform.util._

class PilarLexerReader(val tokens: List[Token]) extends Reader[Token] {

  def first: Token = tokens.head

  def rest: Reader[Token] = new PilarLexerReader(tokens.tail)

  def pos: Position = new PilarLexerPosition(first)

  def atEnd: Boolean = tokens.isEmpty || tokens.head.tokenType == EOF

  private class PilarLexerPosition(token: Token) extends Position {

    def line: Int = -1

    def column: Int = -1

    protected def lineContents: String = token.rawText

    override def longString = lineContents

  }

}

