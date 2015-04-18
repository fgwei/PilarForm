package org.sireum.pilarform.lexer

import java.io.File
import scala.annotation._
import scala.collection.mutable.{ Queue, ListBuffer }
import scala.io.Source
import scala.math.min
import scala.xml.parsing.TokenTests
import org.sireum.pilarform.lexer.CharConstants.SU
import org.sireum.pilarform.lexer.Tokens._
import org.sireum.pilarform.util.Utils
import org.sireum.pilarform._

class PilarLexer(
  protected val reader: IUnicodeEscapeReader,
  protected val forgiveErrors: Boolean = false,
  protected val pilarVersion: PilarVersion = PilarVersions.DEFAULT)
  extends IPilarLexer with Iterator[Token] {

  import PilarLexer._

  // -- Character buffer ----------------------------------------------------------------------------------------

  /**
   * Circular buffer of characters yet to be processed (after unicode escaping)
   */
  private val charBuffer: Array[Char] = Array.fill(BUFFER_SIZE)(SU)

  /**
   * A circular buffer of the unicode escape, if any, associated with the corresponding character in charBuffer.
   */
  private val unicodeEscapesBuffer: Array[Option[String]] = Array.fill(BUFFER_SIZE)(None)

  private var bufferStart = 0

  private var bufferEnd = 0

  private def charsInBuffer = (BUFFER_SIZE + bufferEnd - bufferStart) & BUFFER_MASK
  
  /**
   * Is the current character the result of a unicode escape?
   */
  protected def isUnicodeEscape = unicodeEscapesBuffer(bufferStart).isDefined

  // ------------------------------------------------------------------------------------------------------------

  /**
   * Has a Unicode escape occurred somewhere in the current token?
   */
  private var seenUnicodeEscape = false

  /**
   * Start position of this token in the (pre-Unicode escaped) text
   */
  private var tokenOffset = 0

  /**
   * Length so far of this token (before unicode escaping)
   */
  private var tokenLength = 0

  /**
   * The previous character
   */
  protected var lastCh: Char = SU

  private var tokenText: String = _

  private var rawText: String = _

  private var stopIndex: Int = 0

  protected var builtToken: Token = _

  /**
   * Number of characters left in the character buffer before the end of file, or -1 if this is yet to be discovered.
   */
  private var untilEof = if (reader.isEof) 0 else -1

  protected def eof = untilEof == 0

  private var eofTokenEmitted = false

  /**
   * Get the current character.
   */
  protected def ch: Char = {
    if (bufferEnd == bufferStart)
      bufferOneCharacter()
    charBuffer(bufferStart)
  }

  /**
   * Get the character at the given lookahead from the current position.
   */
  protected def ch(lookahead: Int) = {
    for (n ← 1 to lookahead + 1 - charsInBuffer)
      bufferOneCharacter()
    charBuffer((bufferStart + lookahead) & BUFFER_MASK)
  }

  private def bufferOneCharacter() {
    charBuffer(bufferEnd) = reader.read()
    unicodeEscapesBuffer(bufferEnd) = reader.unicodeEscapeOpt
    bufferEnd = (bufferEnd + 1) & BUFFER_MASK
    if (untilEof == -1 && reader.isEof)
      untilEof = charsInBuffer
  }

  /**
   * Accept the current character and advance to the next.
   */
  protected def nextChar() {
    if (bufferEnd == bufferStart)
      bufferOneCharacter()
    lastCh = charBuffer(bufferStart)
    val unicodeEscapeOpt = unicodeEscapesBuffer(bufferStart)
    bufferStart = (bufferStart + 1) & BUFFER_MASK

    tokenLength +=
      (unicodeEscapeOpt match {
        case None    ⇒ 1
        case Some(s) ⇒ s.length
      })
    seenUnicodeEscape |= unicodeEscapeOpt.isDefined

    if (untilEof > 0)
      untilEof -= 1
  }

  // For debug
  private def bufferContents: String =
    (for (n ← 0 until charsInBuffer) yield charBuffer((bufferStart + n) & BUFFER_MASK)).mkString

  /**
   * Mark the end of a token of the given type.
   */
  protected def token(tokenType: TokenType) {
    //    require(tokenType == EOF || tokenLength > 0)
    finaliseTokenData()
    builtToken = Token(tokenType, tokenText, tokenOffset, rawText)

    if (seenUnicodeEscape)
      builtToken.containsUnicodeEscape = true
    resetTokenData()
  }

  private def resetTokenData() {
    rawText = null
    tokenText = null
    tokenOffset = stopIndex + 1
    tokenLength = 0
    seenUnicodeEscape = false
  }

  private def finaliseTokenData() {
    if (tokenText == null) {
      stopIndex = math.min(tokenOffset + tokenLength - 1, reader.text.length - 1) // min protects against overeager consumption past EOF
      rawText = reader.text.substring(tokenOffset, stopIndex + 1)
      tokenText =
        if (seenUnicodeEscape)
          UnicodeEscapeDecoder.decode(rawText, forgiveErrors)
        else
          rawText
    }
  }

  private[lexer] def text = reader.text

  protected def getTokenText: String = {
    finaliseTokenData()
    tokenText
  }

  protected def lookaheadIs(s: String): Boolean =
    s.zipWithIndex forall { case (c, index) ⇒ ch(index) == c }

  protected def munch(s: String) {
    //    require(lookaheadIs(s))
    for (_ ← 1 to s.length)
      nextChar()
  }

  def next(): Token = {
      fetchPilarToken()

    if (builtToken.tokenType == EOF)
      eofTokenEmitted = true
    builtToken
  }

  def hasNext = !eofTokenEmitted

}

object PilarLexer {

  /**
   * Convert the given Pilar source code into a list of "raw" tokens.
   *
   * This includes whitespace and comment tokens. No NEWLINE or NEWLINES tokens are inferred. The final token
   * will be of type EOF.
   *
   * @param forgiveErrors -- if true, no exceptions will be thrown when malformed tokens are encountered.
   * @param pilarVersion -- the version of Pilar to assume as the source type (e.g. "2.9.1"). This can affect the
   *   interpretation of certain tokens (for example, floating point literals).
   */
  @throws(classOf[PilarLexerException])
  def rawTokenise(s: String, forgiveErrors: Boolean = false, pilarVersion: String = PilarVersions.DEFAULT_VERSION): List[Token] =
    createRawLexer(s, forgiveErrors, pilarVersion).toList

  /**
   * Create a lexer for "raw" tokens.
   *
   * @see rawTokenise
   */
  def createRawLexer(s: String, forgiveErrors: Boolean = false, pilarVersion: String = PilarVersions.DEFAULT_VERSION): PilarLexer =
    makeRawLexer(s, forgiveErrors, PilarVersion.parseOrDefault(pilarVersion))

  /**
   * Convert the given Pilar source code into a list of tokens.
   *
   * NEWLINE or NEWLINES tokens are inferred, and whitespace and comments are absorbed into the token they
   * precede. The final token will be of type EOF.
   *
   * @param forgiveErrors -- if true, no exceptions will be thrown when malformed tokens are encountered.
   * @param pilarVersion -- the version of Pilar to assume as the source type (e.g. "4.0"). This can affect the
   *   interpretation of certain tokens (for example, floating point literals).
   */
  @throws(classOf[PilarLexerException])
  def tokenise(s: String, forgiveErrors: Boolean = false, pilarVersion: String = PilarVersions.DEFAULT_VERSION): List[Token] = {
    val rawLexer = createRawLexer(s, forgiveErrors, pilarVersion)
    val lexer = new NewlineInferencer(new WhitespaceAndCommentsGrouper(rawLexer))
    lexer.toList
  }

  private val BUFFER_SIZE = 16

  private val BUFFER_MASK = BUFFER_SIZE - 1

  private def makeRawLexer(s: String, forgiveErrors: Boolean = false, pilarVersion: PilarVersion = PilarVersions.DEFAULT): PilarLexer =
    new PilarLexer(new UnicodeEscapeReader(s, forgiveErrors), forgiveErrors, pilarVersion)

}