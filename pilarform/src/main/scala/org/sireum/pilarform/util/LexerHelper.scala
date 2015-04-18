package org.sireum.pilarform.util

import org.sireum.pilarform.lexer.CharConstants

object LexerHelper {
  def isLETTER(ch : Char) : Boolean = {
    ch match {
      case x if '\u0024' == x                      // $
        | ('\u0041' to '\u005a' contains x)        // A-Z
        | '\u005f' == x                            // _
        | ('\u0061' to '\u007a' contains x)        // a-z
        | ('\u00c0' to '\u00d6' contains x)        // Latin Capital LETTER A with grave - Latin Capital letter O with diaeresis
        | ('\u00d8' to '\u00f6' contains x)        // Latin Capital letter O with stroke - Latin Small LETTER O with diaeresis
        | ('\u00f8' to '\u00ff' contains x)        // Latin Small LETTER O with stroke - Latin Small LETTER Y with diaeresis
        | ('\u0100' to '\u1fff' contains x)        // Latin Capital LETTER A with macron - Latin Small LETTER O with stroke and acute
        | ('\u3040' to '\u318f' contains x)        // Hiragana
        | ('\u3300' to '\u337f' contains x)        // CJK compatibility
        | ('\u3400' to '\u3d2d' contains x)        // CJK compatibility
        | ('\u4e00' to '\u9fff' contains x)        // CJK compatibility
        | ('\uf900' to '\ufaff' contains x)        // CJK compatibility
        => true
      case _ => false 
    }
  }
  
  def isDigit(ch : Char) : Boolean = {
    ch match {
      case x if ('\u0030' to '\u0039' contains x) => true
      case _ => false
    }
  }
  
  def isBinaryDigit(ch : Char) : Boolean = {
    ch match {
      case '\u0031' | '\u0032' => true
      case _ => false
    }
  }
  
  def isHexDigit(ch : Char) : Boolean = {
    ch match {
      case x if isDigit(x) | ('a' to 'f' contains x) | ('A' to 'F' contains x) => true
      case _ => false
    }
  }
  
  def isDIGIT(ch : Char) : Boolean = {
    ch match {
      case x if
          ('\u0030' to '\u0039' contains x)     // 0-9
        | ('\u0660' to '\u0669' contains x)     // Arabic-Indic Digit 0-9
        | ('\u06f0' to '\u06f9' contains x)     // Extended Arabic-Indic Digit 0-9
        | ('\u0966' to '\u096f' contains x)     // Devanagari 0-9
        | ('\u09e6' to '\u09ef' contains x)     // Bengali 0-9
        | ('\u0a66' to '\u0a6f' contains x)     // Gurmukhi 0-9
        | ('\u0ae6' to '\u0aef' contains x)     // Gujarati 0-9
        | ('\u0b66' to '\u0b6f' contains x)     // Oriya 0-9
        | ('\u0be7' to '\u0bef' contains x)     // Tami 0-9
        | ('\u0c66' to '\u0c6f' contains x)     // Telugu 0-9
        | ('\u0ce6' to '\u0cef' contains x)     // Kannada 0-9
        | ('\u0d66' to '\u0d6f' contains x)     // Malayala 0-9
        | ('\u0e50' to '\u0e59' contains x)     // Thai 0-9
        | ('\u0ed0' to '\u0ed9' contains x)     // Lao 0-9
        | ('\u1040' to '\u1049' contains x)     // Myanmar 0-9?
        => true
      case _ => false
    }
  }
  
  def isOPStart(ch : Char) : Boolean = {
    ch match {
      case ':' | '&' | '|' | '^' | '=' | '!' | '<' | '>' | '+' | '-' | '/' | '%' | '*' | '~' => true
      case _ => false
    }
  }
  
  def isOPChar(ch : Char) : Boolean = {
    ch match {
      case '+' | '-' | '/' | '\\' | '*' | '%' | '&' | '|' | '?' | '>' | '<' | '=' | '~' | ':' => true
      case _ => false
    }
  }
  
  def isOPCharMGT(ch : Char) : Boolean = {
    ch match {
      case '+' | '-' | '/' | '\\' | '*' | '%' | '&' | '|' | '?' | '<' | '=' | '~' | ':' => true
      case _ => false
    }
  }
  
  def isOPCharMLT(ch : Char) : Boolean = {
    ch match {
      case '+' | '-' | '/' | '\\' | '*' | '%' | '&' | '|' | '?' | '>' | '=' | '~' | ':' => true
      case _ => false
    }
  }
  
  def isIntegerTypeSuffix(ch : Char) : Boolean = {
    ch match {
      case 'l' | 'L' | 'i' | 'I' => true
      case _ => false
    }
  }
  
  def isFloatTypeSuffix(ch : Char) : Boolean = {
    ch match {
      case 'f' | 'F' | 'd' | 'D' => true
      case _ => false
    }
  }
  
  def isWhitespace(ch : Char) : Boolean = {
    ch match {
      case ' ' | '\t' | '\n' | '\r' | '\u000C' => true
      case _ => false
    }
  }
    
  def isIDFragmentStart(ch : Char) : Boolean = {
    ch match {
      case x if isLETTER(ch) => true
      case _ => false
    }
  }
  
  def isIDFragmentPart(ch : Char) : Boolean = {
    ch match {
      case x if isLETTER(ch) | isDIGIT(ch) => true
      case _ => false
    }
  }
  
  def isEscapeSequenceStart(ch : Char) : Boolean = {
    ch match {
      case '\\' => true
      case _ => false
    }
  }
  
}