package org.sireum.pilarform.lexer

object Tokens {

  val RECORD = TokenType("RECORD")
  val PROCEDURE = TokenType("PROCEDURE")
  val EXTENDS = TokenType("EXTENDS")

  val EQUALS = TokenType("EQUALS")
  
  val ID = TokenType("ID")
  val LID = TokenType("LID")
  val GID = TokenType("GID")
  
  val NEW = TokenType("NEW")
  val THROW = TokenType("THROW")
  val CATCH = TokenType("CATCH")
  val IF = TokenType("IF")
  val THEN_GOTO = TokenType("THEN_GOTO")
  val GOTO = TokenType("GOTO")
  val SWITCH = TokenType("SWITCH")
  val ELSE = TokenType("ELSE")
  val RETURN = TokenType("RETURN")
  val CALL = TokenType("CALL")
  
  val EOF = TokenType("EOF")
  
  val LBRACKET = TokenType("LBRACKET")
  val RBRACKET = TokenType("RBRACKET")
  val LPAREN = TokenType("LPAREN")
  val RPAREN = TokenType("RPAREN")
  val LBRACE = TokenType("LBRACE")
  val RBRACE = TokenType("RBRACE")

  val STRING_LITERAL = TokenType("STRING_LITERAL")
  val FLOATING_POINT_LITERAL = TokenType("FLOATING_POINT_LITERAL")
  val INTEGER_LITERAL = TokenType("INTEGER_LITERAL")
  val CHARACTER_LITERAL = TokenType("CHARACTER_LITERAL")
  val TRUE = TokenType("TRUE")
  val FALSE = TokenType("FALSE")
  val NULL = TokenType("NULL")
  
  val EXCLAMATION = TokenType("EXCLAMATION")
  val COMMA = TokenType("COMMA")
  val DOT = TokenType("DOT")
  val SEMI = TokenType("SEMI")
  val COLON = TokenType("COLON")
  
  val ANNOTATION = TokenType("ANNOTATION")
  
  val NEWLINE = TokenType("NEWLINE")
  val NEWLINES = TokenType("NEWLINES")
  
  val LINE_COMMENT = TokenType("LINE_COMMENT")
  val MULTILINE_COMMENT = TokenType("MULTILINE_COMMENT")
  
  val WS = TokenType("WS")
 
  val OP = TokenType("OP")
  
  val KEYWORDS = Set(
    RECORD, PROCEDURE, EXTENDS, IF, THEN_GOTO, NEW,
    RETURN, THROW, CALL, SWITCH, ELSE, GOTO, CATCH)

  val COMMENTS = Set(LINE_COMMENT, MULTILINE_COMMENT)

  val IDS = Set(ID, LID, GID)

  val LITERALS = Set(CHARACTER_LITERAL, INTEGER_LITERAL, FLOATING_POINT_LITERAL, STRING_LITERAL, TRUE, FALSE, NULL)

}

