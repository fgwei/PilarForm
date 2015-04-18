package org.sireum.pilarform.lexer

sealed trait LexerMode

class PilarMode extends LexerMode {

  private var braceNestLevel: Int = 0

  def nestBrace() { braceNestLevel += 1 }

  def unnestBrace(): Int = {
    braceNestLevel -= 1
    braceNestLevel
  }

}