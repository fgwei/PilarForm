package org.sireum.pilarform.lexer

import org.sireum.pilarform._
import org.sireum.pilarform.lexer.Tokens._
import org.scalatest._
import java.io._

class PilarLexerTest extends FlatSpec with ShouldMatchers {

  implicit def string2TestString(s: String)(implicit forgiveErrors: Boolean = false, pilarVersion: PilarVersion = PilarVersions.DEFAULT) =
    new TestString(s, forgiveErrors, pilarVersion)

  "" producesTokens ()

  """`format`""" producesTokens (ID)

  "`format`;`format`" producesTokens (ID, SEMI, ID)

  "|||" producesTokens (OP)

  ":=" producesTokens (OP)

  "^~" producesTokens (OP)

  "v0/2" producesTokens (ID, OP, INTEGER_LITERAL)

  "record" producesTokens (RECORD)

  "procedure" producesTokens (PROCEDURE)

  "foo  bar   baz" producesTokens (ID, WS, ID, WS, ID)

  "  " producesTokens (WS)

  "// comment" producesTokens (LINE_COMMENT)

  "//" producesTokens (LINE_COMMENT)

  "foo// comment" producesTokens (ID, LINE_COMMENT)

  "foo // comment" producesTokens (ID, WS, LINE_COMMENT)

  """foo// comment
    abc//comment""" producesTokens (ID, LINE_COMMENT, WS, ID, LINE_COMMENT)

  "foo/* comment */bar" producesTokens (ID, MULTILINE_COMMENT, ID)

  "/* bar /* baz */ var */" producesTokens (MULTILINE_COMMENT)

  "/**/" producesTokens (MULTILINE_COMMENT)

  "`yield`" producesTokens (ID)

  """"foobar"""" producesTokens (STRING_LITERAL)
  
  """`@@global`""" producesTokens (GID)
  
  """@@global""" producesTokens (GID)

  "\"\"\"f\"o\"o\"\"\"" producesTokens (STRING_LITERAL)

  """"\""""" producesTokens (STRING_LITERAL)

  "\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"" producesTokens (STRING_LITERAL)

  "foo.bar.baz()" producesTokens (ID, DOT, ID, DOT, ID, LPAREN, RPAREN)

  ".1234" producesTokens (FLOATING_POINT_LITERAL)
  ".1234e2" producesTokens (FLOATING_POINT_LITERAL)
  ".1234e+2" producesTokens (FLOATING_POINT_LITERAL)
  ".1e-2" producesTokens (FLOATING_POINT_LITERAL)
  ".1e+2345f" producesTokens (FLOATING_POINT_LITERAL)
  ".1e+2345d" producesTokens (FLOATING_POINT_LITERAL)

  "100" producesTokens (INTEGER_LITERAL)
  "1" producesTokens (INTEGER_LITERAL)
  "1L" producesTokens (INTEGER_LITERAL)
  "0" producesTokens (INTEGER_LITERAL)
  "0L" producesTokens (INTEGER_LITERAL)
  "0x2345" producesTokens (INTEGER_LITERAL)
  "0x" producesTokens (INTEGER_LITERAL)
  "0x32413L" producesTokens (INTEGER_LITERAL)
  
  "#" producesTokens (LID)
  "#L00011." producesTokens (LID)

  "0.1234" producesTokens (FLOATING_POINT_LITERAL)
  "0.1234e2" producesTokens (FLOATING_POINT_LITERAL)
  "0.1234e+2" producesTokens (FLOATING_POINT_LITERAL)
  "0.1e-2" producesTokens (FLOATING_POINT_LITERAL)
  "0.1e+2345f" producesTokens (FLOATING_POINT_LITERAL)
  "0.1e+2345d" producesTokens (FLOATING_POINT_LITERAL)

  "10e2" producesTokens (FLOATING_POINT_LITERAL)
  "10e+2" producesTokens (FLOATING_POINT_LITERAL)
  "10e-2" producesTokens (FLOATING_POINT_LITERAL)
  "10e+2345f" producesTokens (FLOATING_POINT_LITERAL)
  "10e+2345d" producesTokens (FLOATING_POINT_LITERAL)

  "22.`yield`" producesTokens (INTEGER_LITERAL, DOT, ID)
  "42.toString" producesTokens (INTEGER_LITERAL, DOT, ID)

  "'f'" producesTokens (CHARACTER_LITERAL)
  """'\n'""" producesTokens (CHARACTER_LITERAL)
  """'\025'""" producesTokens (CHARACTER_LITERAL)

  "#L0001. tokenTextBuffer:= new StringBuilder" producesTokens (LID, WS, ID, OP, WS, NEW, WS, ID)

  """println("bob")
println("foo")""" producesTokens (ID, LPAREN, STRING_LITERAL, RPAREN, WS, ID, LPAREN, STRING_LITERAL, RPAREN)

  "\\u0061" producesTokens (ID)
  "\\uuuuuuuuuuuuuuuuuuuuuuuuu0061" producesTokens (ID)
  "\"\\u0061\"" producesTokens (STRING_LITERAL)
  "\"\\u000a\"" producesTokens (STRING_LITERAL)

 
  "0X1234" producesTokens (INTEGER_LITERAL)

  
  "\"\\u001A\"" producesTokens (STRING_LITERAL)

  "\"\"\"\\u001A\"\"\"" producesTokens (STRING_LITERAL)

  "foo+\\u0061+bar" producesTokens (ID, OP, ID, OP, ID)

  "-5f.max(2)" producesTokens (OP, FLOATING_POINT_LITERAL, DOT, ID, LPAREN, INTEGER_LITERAL, RPAREN)
  "-5f max(2)" producesTokens (OP, FLOATING_POINT_LITERAL, WS, ID, LPAREN, INTEGER_LITERAL, RPAREN)
  "-5.max(2)" producesTokens (OP, INTEGER_LITERAL, DOT, ID, LPAREN, INTEGER_LITERAL, RPAREN)
  "-5 max(2)" producesTokens (OP, INTEGER_LITERAL, WS, ID, LPAREN, INTEGER_LITERAL, RPAREN)

  "Lexer" should "throw a lexer exception" in {
    evaluating { PilarLexer.rawTokenise("\"\"\"") } should produce[PilarLexerException]
  }

"""
record `com.ksu.passwordPassTest.MainActivity`  @type class @AccessFlag PUBLIC  extends `android.app.Activity` {
      `android.widget.EditText` `com.ksu.passwordPassTest.MainActivity.editText`    @AccessFlag ;
      `android.widget.Button` `com.ksu.passwordPassTest.MainActivity.passButton`    @AccessFlag ;
   }
    procedure `void` `com.ksu.passwordPassTest.MainActivity.<init>` (`com.ksu.passwordPassTest.MainActivity` v1 @type `this`) @owner `com.ksu.passwordPassTest.MainActivity` @signature `Lcom/ksu/passwordPassTest/MainActivity;.<init>:()V` @Access `PUBLIC_CONSTRUCTOR` {
      temp ;
        v0;
      
#L047178.   v0:= 0I  @length `4`;
#L04717a.   call temp:=  `android.app.Activity.<init>`(v1) @signature `Landroid/app/Activity;.<init>:()V` @classDescriptor `android.app.Activity` @type direct;
#L047180.   v1.`com.ksu.passwordPassTest.MainActivity.editText`  := v0 @type `object`;
#L047184.   v1.`com.ksu.passwordPassTest.MainActivity.passButton`  := v0 @type `object`;
#L047188.   return @void ;

   }
""" producesTokens 
  (WS, RECORD, WS, ID, WS, ANNOTATION, WS, ID, WS, ANNOTATION, WS, ID, WS, EXTENDS, WS, ID, WS, LBRACE,
   WS, ID, WS, ID, WS, ANNOTATION, WS, SEMI,
   WS, ID, WS, ID, WS, ANNOTATION, WS, SEMI,
   WS, RBRACE,
   WS, PROCEDURE, WS, ID, WS, ID, WS, LPAREN, ID, WS, ID, WS, ANNOTATION, WS, ID, RPAREN, WS, ANNOTATION, WS, ID, WS, ANNOTATION, WS, ID, WS, ANNOTATION, WS, ID, WS, LBRACE,
   WS, ID, WS, SEMI,
   WS, ID, SEMI,
   WS,
   LID, WS, ID, OP, WS, INTEGER_LITERAL, WS, ANNOTATION, WS, ID, SEMI, WS,
   LID, WS, CALL, WS, ID, OP, WS, ID, LPAREN, ID, RPAREN, WS, ANNOTATION, WS, ID, WS, ANNOTATION, WS, ID, WS, ANNOTATION, WS, ID, SEMI, WS,
   LID, WS, ID, DOT, ID, WS, OP, WS, ID, WS, ANNOTATION, WS, ID, SEMI, WS,
   LID, WS, ID, DOT, ID, WS, OP, WS, ID, WS, ANNOTATION, WS, ID, SEMI, WS,
   LID, WS, RETURN, WS, ANNOTATION, WS, SEMI,
   WS,
   RBRACE, WS)
  
  
  {
    implicit val forgiveErrors = true

    "\"\"\"" producesTokens (STRING_LITERAL)
    "\'" producesTokens (CHARACTER_LITERAL)
    "\"unclosed" producesTokens (STRING_LITERAL)
    "\\ufoob" producesTokens (WS)
    "`unclosed" producesTokens (ID)

  }

  class TestString(s: String, forgiveErrors: Boolean = false, pilarVersion: PilarVersion = PilarVersions.DEFAULT) {

    def producesTokens(toks: TokenType*)() {
      check(s.stripMargin, toks.toList)
    }

    private def check(s: String, expectedTokens: List[TokenType]) {
      it should ("tokenise >>>" + s + "<<< as >>>" + expectedTokens + "<<< forgiveErrors = " + forgiveErrors + ", pilarVersion = " + pilarVersion) in {
        val actualTokens: List[Token] = PilarLexer.rawTokenise(s, forgiveErrors, pilarVersion.toString)
        val actualTokenTypes = actualTokens.map(_.tokenType)
        require(actualTokenTypes.last == EOF, "Last token must be EOF, but was " + actualTokens.last.tokenType)
        require(actualTokenTypes.count(_ == EOF) == 1, "There must only be one EOF token")
        val reconstitutedSource = actualTokens.init.map(_.rawText).mkString
        require(actualTokenTypes.init == expectedTokens, "Tokens do not match. Expected " + expectedTokens + ", but was " + actualTokenTypes.init)
        require(s == reconstitutedSource, "tokens do not partition text correctly: " + s + " vs " + reconstitutedSource)
      }
    }

  }

}
