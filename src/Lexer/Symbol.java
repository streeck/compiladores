/* ------------------------------
Charles David de Moraes RA: 489662
Vitor Kusiaki             RA: 408140
------------------------------ */

package Lexer;

public enum Symbol {
  EOF("eof"),
  IDENT("Ident"),
  NUMBER("Number"),
  PLUS("+"),
  MINUS("-"),
  MULT("*"),
  DIV("/"),
  MOD("%"),
  LT("<"),
  LTE("<="),
  GT(">"),
  GTE(">="),
  EQ("="),
  ASSIGN(":="),
  NEQ("!="),
  AND("&&"),
  OR("||"),
  NOT("!"),
  SEMICOLON(";"),
  COMMA(","),
  DOT("."),
  APOSTROPHE("'"),
  LEFTBRACKET("["),
  RIGHTBRACKET("]"),
  LEFTPAR("("),
  RIGHTPAR(")"),
  LEFTBRACE("{"),
  RIGHTBRACE("}"),
  VOID("void"),
  MAIN("main"),
  INT("int"),
  DOUBLE("double"),
  CHAR("char"),
  IF("if"),
  ELSE("else"),
  WHILE("while"),
  BREAK("break"),
  PRINT("print"),
  READINTEGER("readInteger"),
  READDOUBLE("readDouble"),
  READCHAR("readChar");

  Symbol(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }

  private String name;
}
