package org.sireum.pilarform.lexer

import org.sireum.pilarform.lexer.Tokens._
import org.sireum.pilarform.util.Range

/**
 * A token of Pilar source.
 *
 * @param text -- the text associated with the token after unicode escaping
 * @param rawText -- the text associated with the token before unicode escaping
 */
case class Token(tokenType: TokenType, text: String, offset: Int, rawText: String) {

  private[lexer] var associatedWhitespaceAndComments_ : HiddenTokens = null

  private[lexer] var containsUnicodeEscape = false

  def associatedWhitespaceAndComments: HiddenTokens = associatedWhitespaceAndComments_

  def length = rawText.length

  def range = Range(offset, length)

  def lastCharacterOffset = offset + length - 1

  def isNewline = tokenType.isNewline

}