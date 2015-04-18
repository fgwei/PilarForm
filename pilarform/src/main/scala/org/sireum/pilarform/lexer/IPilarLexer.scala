package org.sireum.pilarform.lexer

import scala.annotation._
import org.sireum.pilarform.lexer.CharConstants.SU
import org.sireum.pilarform.lexer.PilarLexer._
import org.sireum.pilarform.lexer.Tokens._
import org.sireum.pilarform.util.Utils
import org.sireum.pilarform._
import org.sireum.pilarform.util.LexerHelper

/**
 * Lexer implementation for pilar
 */
private[lexer] trait IPilarLexer { self: PilarLexer =>

  protected def fetchPilarToken() {
    ch match {
      case x if LexerHelper.isWhitespace(x) =>
        nextChar()
        getWhitespaceRest()
      case x if LexerHelper.isLETTER(x) =>
        nextChar()
        getIDRest()
        finishNamed()
      case '/' =>
        (ch(1): @switch) match {
          case '/' => getSingleLineComment()
          case '*' => getMultilineComment()
          case _   => getOpRest()
        }
      case x if LexerHelper.isOPStart(x) =>
        getOpRest()
      case '@' =>
        nextChar()
        if(ch == '@'){
          nextChar()
          getIDRest()
          token(GID)
        } else if(LexerHelper.isIDFragmentStart(ch)) {
          getIDRest()
          token(ANNOTATION)
        } else if (forgiveErrors) {
          token(ANNOTATION)
        } else
          throw new PilarLexerException("illegal character: " + ch)
      case '0' =>
        if (ch(1) == 'x' || ch(1) == 'X')
          getHexNumber()
        else if (ch(1) == 'b' || ch(1) == 'B')
          getBinNumber()
        else {
          nextChar()
          getNumber(base = 8)
        }
      case x if LexerHelper.isDigit(x) =>
        getNumber(base = 10)
      case '#' => getLocation()
      case '`' => getBackquotedID()
      case '"' =>
        if (ch(1) == '"' && ch(2) == '"')
          getMultiLineStringLit()
        else
          getStringLit()
      case '\'' =>
        getLitChar()
      case '.' =>
        nextChar()
        if (LexerHelper.isDigit(ch))
          getFraction()
        else
          token(DOT)
      case ';' =>
        nextChar(); token(SEMI)
      case ',' =>
        nextChar(); token(COMMA)
      case '(' =>
        nextChar(); token(LPAREN)
      case '{' =>
        nextChar(); token(LBRACE)
      case ')' =>
        nextChar(); token(RPAREN)
      case '}' =>
        nextChar(); token(RBRACE)
      case '[' =>
        nextChar(); token(LBRACKET)
      case ']' =>
        nextChar(); token(RBRACKET)
      case SU =>
        token(EOF)
      case _ =>
        if (Character.isUnicodeIdentifierStart(ch)) {
          nextChar()
          getIDRest()
          token(ID)
        } else if (forgiveErrors) {
          nextChar()
          getWhitespaceRest()
        } else
          throw new PilarLexerException("illegal character: " + ch)
    }
  }
  
  @tailrec
  private def getWhitespaceRest(): Unit = ch match {
    case x if LexerHelper.isWhitespace(x) =>
      nextChar()
      getWhitespaceRest()
    case _ =>
      token(WS)
  }

  private def getStringLit() {
    nextChar()
    def scanForClosingQuotes(): Unit = ch match {
      case SU if eof =>
        if (forgiveErrors) token(STRING_LITERAL) else throw new PilarLexerException("unclosed quoted literal")
      case x if LexerHelper.isEscapeSequenceStart(x) =>
        getEscapeSequence()
        scanForClosingQuotes()
      case '"' =>
        nextChar()
        token(STRING_LITERAL)
      case '\\' =>
        if (forgiveErrors) token(STRING_LITERAL) else throw new PilarLexerException("unclosed quoted literal")
      case _ =>
        nextChar()
        scanForClosingQuotes()
    }
    scanForClosingQuotes()
  }

  private def getBackquotedID() {
    var tokenType = ID
    nextChar()
    if(ch == '@' && ch(1) == '@') tokenType = GID
    @tailrec
    def scanForClosingBackQuotes(firstTime: Boolean): Unit = ch match {
      case SU if eof =>
        if (forgiveErrors) token(tokenType) else throw new PilarLexerException("unclosed backquoted id")
      case x if LexerHelper.isWhitespace(x) =>
        if (forgiveErrors) token(tokenType) else throw new PilarLexerException("unclosed backquoted id")
      case '`' if firstTime =>
        if (forgiveErrors) token(tokenType) else throw new PilarLexerException("empty backquoted id")
      case '`' =>
        nextChar()
        token(tokenType)
      case _ =>
        nextChar()
        scanForClosingBackQuotes(firstTime = false)
    }
    scanForClosingBackQuotes(firstTime = true)
  }
  
  private def getLocation() {
    nextChar()
    ch match {
      case SU if eof =>
        token(LID)
      case '\r' | '\n' if !isUnicodeEscape =>
        token(LID)
      case '.' =>
        nextChar()
        token(LID)
      case x if LexerHelper.isIDFragmentPart(ch) =>
        getLocation()
    }
  }

  private def getLitChar() {
    nextChar()
    ch match {
      case SU if eof =>
        if(forgiveErrors) token(CHARACTER_LITERAL) else throw new PilarLexerException("unclosed char literal")
      case x if LexerHelper.isEscapeSequenceStart(x) =>
        getEscapeSequence()
        ch match {
          case '\'' => 
            nextChar()
            token(CHARACTER_LITERAL)
          case _ => if(!forgiveErrors) throw new PilarLexerException("unclosed char literal")
        }
      case _ =>
        nextChar()
        ch match {
          case '\'' => 
            nextChar()
            token(CHARACTER_LITERAL)
          case _ => if(!forgiveErrors) throw new PilarLexerException("unclosed char literal")
        }
    }
  }

  private def getMultiLineStringLit() {
    munch("\"\"\"")

    @tailrec
    def scanForClosingTripleQuotes() {
      if (lookaheadIs("\"\"\"")) {
        munch("\"\"\"")
        while (ch == '\"') { nextChar() }
        token(STRING_LITERAL)
      } else if (eof) {
        if (forgiveErrors) token(STRING_LITERAL) else throw new PilarLexerException("unclosed multi-line string literal")
      } else {
        nextChar()
        scanForClosingTripleQuotes()
      }
    }
    scanForClosingTripleQuotes()
  }

  private def getEscapeSequence() {
    ch match {
      case '\\' =>
        nextChar()
        ch match {
          case 'b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\' =>
            nextChar()
          case 'u' =>
            getUnicodeEscape()
          case _ =>
            getOctalEscape()
        }
    }
  }
  
  private def getOctalEscape() : Unit = {
    if((('0' to '3') contains ch(0)) && (('0' to '7') contains ch(1)) && (('0' to '7') contains ch(2))){
      nextChar(); nextChar(); nextChar()
    }
    else if ((('0' to '7') contains ch(0)) && (('0' to '7') contains ch(1))){
      nextChar(); nextChar()
    }
    else if (('0' to '7') contains ch(0)){
      nextChar()
    }
  }
  
  private def getUnicodeEscape() : Unit = {
    ch match {
      case 'u' => 
        next
        if(LexerHelper.isHexDigit(1) && LexerHelper.isHexDigit(ch(2)) && LexerHelper.isHexDigit(ch(3)) && LexerHelper.isHexDigit(ch(4))){
          nextChar(); nextChar(); nextChar(); nextChar()
        }
    }
  }
  
  @tailrec
  final protected def getStringPart(multiLine: Boolean) {
    if (ch == '"') {
      if (multiLine) {
        nextChar()
        if (isTripleQuote()) {
          token(STRING_LITERAL)
        } else
          getStringPart(multiLine)
      } else {
        nextChar()
        token(STRING_LITERAL)
      }
    } else {
      val isUnclosedLiteral = !isUnicodeEscape && (ch == SU || (!multiLine && (ch == '\r' || ch == '\n')))
      if (isUnclosedLiteral) {
        if (forgiveErrors) {
          token(STRING_LITERAL)
        } else
          throw new PilarLexerException(if (!multiLine) "unclosed string literal" else "unclosed multi-line string literal")
      } else {
        nextChar()
        getStringPart(multiLine)
      }
    }
  }

  private def isTripleQuote(): Boolean =
    if (ch == '"') {
      nextChar()
      if (ch == '"') {
        nextChar()
        while (ch == '"')
          nextChar()
        true
      } else
        false
    } else
      false
      
  private def getIDRest(): Unit = ch match {
    case x if LexerHelper.isIDFragmentPart(x) =>
      nextChar()
      getIDRest()
    case '\'' =>
      while(ch == '\''){nextChar()}
    case _ =>
      if (Character.isUnicodeIdentifierPart(ch) && ch != SU) {
        nextChar()
        getIDRest()
      }
  }

  private def getOpRest(): Unit = (ch: @switch) match {
    case ':' =>
      nextChar()
      getOPChar()
      lastCh match {
        case '=' => token(OP)
        case _ => if(forgiveErrors) token(OP) else throw new PilarLexerException("wrong assign op")
      }
    case '&' =>
      nextChar()
      getOPSuffix()
      token(OP)
    case '|' =>
      nextChar()
      ch match {
        case '|' => 
          nextChar()
          getOPSuffix()
          token(OP)
        case _ => if(forgiveErrors) token(OP) else throw new PilarLexerException("wrong op")
      }
    case '^' =>
      nextChar()
      ch match {
        case '&' | '~' | '|' | '<' =>
          nextChar()
          getOPSuffix()
          token(OP)
        case '>' =>
          nextChar()
          ch match {
            case '>' =>
              nextChar()
              getOPSuffix()
              token(OP)
            case x if LexerHelper.isOPCharMGT(x) =>
              nextChar()
              getOPSuffix()
              token(OP)
            case _ =>
              if(forgiveErrors) token(OP) else throw new PilarLexerException("wrong op")
          }
        case _ => if(forgiveErrors) token(OP) else throw new PilarLexerException("wrong op")
      }
    case '=' =>
      nextChar()
      ch match {
        case '=' =>
          nextChar()
          getOPSuffix()
          token(OP)
        case _ => if(forgiveErrors) token(OP) else throw new PilarLexerException("wrong op")
      }
    case '!' =>
      nextChar()
      ch match {
        case '=' =>
          nextChar()
          getOPSuffix()
          token(OP)
        case _ =>
          getOPSuffix()
          token(OP)
      }
    case '+' | '-' | '/' | '%' | '*' | '~' =>
      nextChar()
      getOPSuffix()
      token(OP)
    case _ =>
      if(forgiveErrors) token(OP) else throw new PilarLexerException("wrong op")
  }

  private def finishNamed() {
    val tokenType = Keywords(getTokenText).getOrElse(ID)
    token(tokenType)
  }

  private def getSingleLineComment() {
    //    require(ch == '/')
    nextChar()
    //    require(ch == '/')
    nextChar()

    @tailrec
    def consumeUntilNewline(): Unit = (ch: @switch) match {
      case '\n' =>
        nextChar()
        token(LINE_COMMENT)
      case '\r' if ch(1) != '\n' =>
        nextChar()
        token(LINE_COMMENT)
      case SU if eof =>
        token(LINE_COMMENT)
      case _ =>
        nextChar()
        consumeUntilNewline()
    }

    consumeUntilNewline()
  }

  private def getMultilineComment() {
    munch("/*")

    @tailrec
    def consumeUntilSplatSlash(nesting: Int) {
      if (nesting == 0)
        token(MULTILINE_COMMENT)
      else
        (ch: @switch) match {
          case '*' if (ch(1) == '/') =>
            nextChar()
            nextChar()
            consumeUntilSplatSlash(nesting - 1)
          case '/' if (ch(1) == '*') =>
            nextChar()
            nextChar()
            consumeUntilSplatSlash(nesting + 1)
          case SU if eof =>
            if (forgiveErrors) token(MULTILINE_COMMENT) else throw new PilarLexerException("Unterminated comment")
          case _ =>
            nextChar()
            consumeUntilSplatSlash(nesting)
        }
    }

    consumeUntilSplatSlash(nesting = 1)
  }
  
  private def getOPSuffix() {
    while(LexerHelper.isOPChar(ch)) nextChar()
    if(ch == '_'){
      nextChar()
      ch match {
        case x if LexerHelper.isIDFragmentStart(ch) => nextChar(); getIDRest()
        case _ => if (!forgiveErrors) throw new PilarLexerException("incorrect OPSuffix")
      }
    }
  }

  private def getFraction() {
    while (LexerHelper.isDigit(ch)) { nextChar() }
    if (ch == 'e' || ch == 'E') {
      nextChar()
      if (ch == '+' || ch == '-')
        nextChar()
      while ('0' <= ch && ch <= '9') { nextChar() }
    }
    if (LexerHelper.isFloatTypeSuffix(ch))
      nextChar()
    checkNoLetter()
    token(FLOATING_POINT_LITERAL)
  }
  
  private def getOPChar() {
    while(LexerHelper.isOPChar(ch)) nextChar()
  }

  private def getHexNumber() {
    nextChar()
    nextChar()
    @tailrec
    def munchHexDigits(): Unit = (ch: @switch) match {
      case x if LexerHelper.isHexDigit(x) =>
        nextChar(); munchHexDigits()
      case x if LexerHelper.isIntegerTypeSuffix(x) =>
        nextChar(); token(INTEGER_LITERAL)
      case _ =>
        token(INTEGER_LITERAL); checkNoLetter()
    }
    munchHexDigits()
  }
  
  private def getBinNumber() {
    nextChar()
    nextChar()
    @tailrec
    def munchBinDigits(): Unit = (ch: @switch) match {
      case x if LexerHelper.isBinaryDigit(x) =>
        nextChar(); munchBinDigits()
      case x if LexerHelper.isIntegerTypeSuffix(x) =>
        nextChar(); token(INTEGER_LITERAL)
      case _ =>
        token(INTEGER_LITERAL); checkNoLetter()
    }
    munchBinDigits()
  }

  private def getNumber(base: Int) {
    def isDigit(c: Char) = if (c == SU) false else (Character isDigit c)
    val base1 = if (base < 10) 10 else base

    // read 8,9's even if format is octal, produce a malformed number error afterwards.
    while (Utils.digit2int(ch, base1) >= 0)
      nextChar()

    def restOfUncertainToken() = {
      def isEfd = ch match {
        case 'e' | 'E' | 'f' | 'F' | 'd' | 'D' => true
        case _                                 => false
      }
      def isInt = ch match {
        case x if LexerHelper.isIntegerTypeSuffix(x) => true
        case _         => false
      }

      if (isEfd)
        getFraction()
      else {
        if (isInt)
          nextChar()
        else
          checkNoLetter()
        token(INTEGER_LITERAL)
      }
    }

    if (ch == '.') {
      val c = ch(1)

      if (!isDigit(c)) {
        token(INTEGER_LITERAL)
        return
      }

      val isDefinitelyNumber =
        (c: @switch) match {
          /** Another digit is a giveaway. */
          case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' =>
            true

          /** Backquoted idents like 22.`foo`. */
          case '`' =>
            token(INTEGER_LITERAL); return
          /** Note the early return */

          /** These letters may be part of a literal, or a method invocation on an Int */
          case 'd' | 'D' | 'f' | 'F' =>
            !LexerHelper.isIDFragmentPart(ch(2))

          /** A little more special handling for e.g. 5e7 */
          case 'e' | 'E' =>
            val ch2 = ch(2)
            !LexerHelper.isLETTER(ch2) || (isDigit(ch2) || ch2 == '+' || ch2 == '-')

          case x =>
            !LexerHelper.isLETTER(x)
        }

      if (isDefinitelyNumber) {
        nextChar()
        getFraction()
      } else
        restOfUncertainToken()
    } else
      restOfUncertainToken()

  }

  private def checkNoLetter() {
    if (LexerHelper.isLETTER(ch) && ch >= ' ' && !forgiveErrors)
      throw new PilarLexerException("Invalid literal number: " + ch)
  }

}