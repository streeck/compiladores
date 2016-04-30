/* ------------------------------
Charles David de Moraes RA: 489662
Vitor Kusiaki             RA: 408140
------------------------------ */

import AST.*;
import java.util.*;
import Lexer.*;
import java.io.*;

public class Compiler {
  private Hashtable<String, Variable> symbolTable;
  private Lexer lexer;
  private CompilerError error;

  public Program compile(char []input, PrintWriter outError, String filename) {
    symbolTable = new Hashtable<String, Variable>();
    error = new CompilerError(outError);
    error.setFilename(filename);
    lexer = new Lexer(input, error);
    error.setLexer(lexer);

    lexer.nextToken();
    return declaration();
  }

  public Program declaration() {
    // Decl ::= 'void' 'main' '(' ')' StmtBlock
    if(lexer.token != Symbol.VOID)
      error.signal("'void' expected");
    lexer.nextToken();

    if(lexer.token != Symbol.MAIN)
      error.signal("'main' expected");
    lexer.nextToken();

    if(lexer.token != Symbol.LEFTPAR)
      error.signal("'(' expected");
    lexer.nextToken();

    if(lexer.token != Symbol.RIGHTPAR)
      error.signal("')' expected");
    lexer.nextToken();

    return new Program(stmtBlock());
  }

  public StatementBlock stmtBlock() {
    // StmtBlock ::= '{' {VariableDecl} {Stmt} '}'
    if(lexer.token != Symbol.LEFTBRACE)
      error.signal("'{' expected");
    lexer.nextToken();

    ArrayList<Variable> vars = new ArrayList<Variable>();
    ArrayList<Statement> statements = new ArrayList<Statement>();

    vars = variableDeclaration();
    statements = statementList();

    if(lexer.token != Symbol.RIGHTBRACE)
      error.signal("'}' expected");
    lexer.nextToken();

    return new StatementBlock(vars, statements);
  }

  public ArrayList<Variable> variableDeclaration() {
    ArrayList<Variable> vars = new ArrayList<Variable>();
    while(lexer.token == Symbol.INT ||
      lexer.token == Symbol.DOUBLE ||
      lexer.token == Symbol.CHAR) {
      variable(vars);
    }
    return vars;
  }

  public void variable(ArrayList<Variable> vars) {
    Type type = type();

    if(lexer.token != Symbol.IDENT)
      error.signal("Identifier expected");

    String identifier = lexer.getStringValue();
    lexer.nextToken();

    if(symbolTable.get(identifier) != null)
      error.signal("Variable '" + identifier + "' has already been declared!");

    Variable var = new Variable(type, identifier);

    symbolTable.put(identifier, var);
    vars.add(var);

    if(lexer.token != Symbol.SEMICOLON)
      error.signal("';' expected");
    lexer.nextToken();
  }

  public Type type() {
    Type type;

    switch(lexer.token) {
      case INT:
        type = Type.intType;
        break;
      case DOUBLE:
        type = Type.doubleType;
        break;
      case CHAR:
        type = Type.charType;
        break;
      default:
        error.signal("Type expected");
        type = null;
    }
    lexer.nextToken();

    if(lexer.token == Symbol.LEFTBRACKET) {
      lexer.nextToken();

      if(lexer.token != Symbol.NUMBER)
        if(lexer.token == Symbol.MINUS)
          error.signal("Invalid array length!");
        else
          error.signal("Array size expected");

      Integer length = lexer.getNumberValue();
      lexer.nextToken();

      if(lexer.token != Symbol.RIGHTBRACKET)
        error.signal("']' expected");
      lexer.nextToken();

      type.setLength(length);
    } else {
      type.setLength(null);
    }

    return type;
  }

  public ArrayList<Statement> statementList() {
    ArrayList<Statement> statements = new ArrayList<>();

    while(lexer.token == Symbol.IDENT ||
          lexer.token == Symbol.IF    ||
          lexer.token == Symbol.WHILE ||
          lexer.token == Symbol.BREAK ||
          lexer.token == Symbol.PRINT) {
      statements.add(statement());
    }

    return statements;
  }

  public Statement statement() {
    switch(lexer.token) {
      case IF:
        lexer.nextToken();
        return ifStatement();
      case WHILE:
        lexer.nextToken();
        return whileStatement();
      case BREAK:
        lexer.nextToken();
        return breakStatement();
      case PRINT:
        lexer.nextToken();
        return printStatement();
      case IDENT:
        return expression();
      default:
        error.signal("Statement expected");
    }
    return null;
  }

  public IfStatement ifStatement() {
    if(isUnary())
      lexer.nextToken();

    if(lexer.token != Symbol.LEFTPAR)
      error.signal("'(' expected");
    lexer.nextToken();

    ExpressionStatement expression = expression();

    // Then part
    if(lexer.token != Symbol.RIGHTPAR)
      error.signal("')' expected");
    lexer.nextToken();

    if(lexer.token != Symbol.LEFTBRACE)
      error.signal("'{' expected");
    lexer.nextToken();

    ArrayList<Statement> thenStatements = statementList();

    if(lexer.token != Symbol.RIGHTBRACE)
      error.signal("'}' expected");
    lexer.nextToken();

    ArrayList<Statement> elseStatements = null;

    // If there's an Else part.
    if(lexer.token == Symbol.ELSE) {
      lexer.nextToken();

      if(lexer.token != Symbol.LEFTBRACE)
        error.signal("'{' expected");
      lexer.nextToken();

      elseStatements = statementList();

      if(lexer.token != Symbol.RIGHTBRACE)
        error.signal("'}' expected");
      lexer.nextToken();
      }
      return new IfStatement(expression, thenStatements, elseStatements);
    }

  public WhileStatement whileStatement() {
    ArrayList<Statement> statements = new ArrayList<>();
    ExpressionStatement expression = null;

    lexer.nextToken();
    // Expression block
    if(lexer.token != Symbol.LEFTPAR)
      error.signal("'(' expected");

    lexer.nextToken();
    expression = expression();
    lexer.nextToken();

    if(lexer.token != Symbol.RIGHTPAR)
      error.signal("')' expected");

    // Statements block
    lexer.nextToken();
    if(lexer.token != Symbol.LEFTBRACE)
      error.signal("'{' expected");

    lexer.nextToken();
    while(lexer.token == Symbol.IDENT ||
          lexer.token == Symbol.IF    ||
          lexer.token == Symbol.WHILE ||
          lexer.token == Symbol.BREAK ||
          lexer.token == Symbol.PRINT)
      statements.add(statement());

    lexer.nextToken();
    if(lexer.token != Symbol.RIGHTBRACE)
      error.signal("'}' expected");

    return new WhileStatement(expression, statements);
  }

  public BreakStatement breakStatement() {
    if(lexer.token != Symbol.BREAK)
      error.signal("'break' expected");
    lexer.nextToken();
    if(lexer.token != Symbol.SEMICOLON)
      error.signal("';' expected");

    return new BreakStatement();
  }

  public PrintStatement printStatement() {
    ArrayList<ExpressionStatement> expressions = new ArrayList<>();

    lexer.nextToken();
    if(lexer.token != Symbol.LEFTPAR)
      error.signal("'(' expected");

    expressions.add(expression());
    lexer.nextToken();

    while(lexer.token == Symbol.COMMA) {
      expressions.add(expression());
      lexer.nextToken();
    }

    lexer.nextToken();
    if(lexer.token != Symbol.RIGHTPAR)
        error.signal("')' expected");

    return new PrintStatement(expressions);
  }

  public ExpressionStatement expression() {
    SimpleExpression simExpr = simpleExpression();
    lexer.nextToken();

    if(lexer.token != Symbol.SEMICOLON)
      error.signal("';' expected");
    lexer.nextToken();

    String relOp = null;
    ExpressionStatement expr = null;

    if(isRelationalOperator()){
      relOp = lexer.getStringValue();
      lexer.nextToken();
      expr = expression();
    }
    return new ExpressionStatement(simExpr, relOp, expr);
  }

  // SimExpr ::= [Unary] Term { AddOp Term }
  public SimpleExpression simpleExpression() {
    Character unaryOp = null;
    Term term = null;
    ArrayList<AddOperation> operations = new ArrayList<>();

    if(isUnary()){
      unaryOp = lexer.getCharValue();
      lexer.nextToken();
    }

    term = term();

    while(isAddOperator()) {
      operations.add(new AddOperation(lexer.getStringValue(), term()));
      lexer.nextToken();
    }

    return new SimpleExpression(unaryOp, term, operations);
  }

  public Term term() {
    Factor factor = factor();
    ArrayList<MultOperation> operations = new ArrayList<>();

    while(isMultiplicationOperator()) {
      operations.add(new MultOperation(Character.toString(lexer.getCharValue()), factor()));
      lexer.nextToken();
    }

    return new Term(factor, operations);
  }

  public Boolean isRelationalOperator() {
    if(lexer.token == Symbol.EQ  ||
       lexer.token == Symbol.NEQ ||
       lexer.token == Symbol.LT  ||
       lexer.token == Symbol.LTE ||
       lexer.token == Symbol.GT  ||
       lexer.token == Symbol.GTE)
      return true;
    return false;
  }

  public Boolean isUnary() {
    if(lexer.token == Symbol.PLUS  ||
       lexer.token == Symbol.MINUS ||
       lexer.token == Symbol.OR)
      return true;
    return false;
  }

  public Boolean isAddOperator() {
    if(lexer.token == Symbol.PLUS  ||
       lexer.token == Symbol.MINUS ||
       lexer.token == Symbol.OR)
      return true;
    return false;
  }

  public Boolean isMultiplicationOperator() {
    if(lexer.token == Symbol.MULT ||
       lexer.token == Symbol.DIV  ||
       lexer.token == Symbol.MOD  ||
       lexer.token == Symbol.AND)
      return true;
    return false;
  }


  public Factor factor() {
    LValue lvalue = leftValue();
    String currentToken = null;

    if(lexer.token != Symbol.ASSIGN)
      error.signal("':=' expected");
    lexer.nextToken();

    // 'readInteger' '(' ')' | 'readDouble' '(' ')' | 'readChar' '(' ')'
    if(lexer.token == Symbol.READINTEGER ||
       lexer.token == Symbol.READDOUBLE  ||
       lexer.token == Symbol.READCHAR) {

      switch(lexer.token) {
        case READINTEGER:
          currentToken = "readInteger";
          break;
        case READDOUBLE:
          currentToken = "readDouble";
          break;
        case READCHAR:
          currentToken = "readChar";
          break;
      }

      lexer.nextToken();
      if(lexer.token != Symbol.LEFTPAR)
        error.signal("'(' expected");

      lexer.nextToken();
      if(lexer.token != Symbol.RIGHTPAR)
        error.signal("')' expected");

      return new ReadTypeFactor(lvalue, currentToken);
    }
    // Number
    else if(lexer.token == Symbol.PLUS  ||
            lexer.token == Symbol.MINUS ||
            lexer.token == Symbol.NUMBER)
      return new NumberFactor(lvalue, number());
    // '(' Expr ')'
    else if(lexer.token == Symbol.LEFTPAR){
      lexer.nextToken();
      ExpressionStatement expr = expression();

      lexer.nextToken();
      if(lexer.token == Symbol.RIGHTPAR)
        error.signal("')' expected");

      return new ExpressionFactor(lvalue, expr);
    }
    // Char
    else if(lexer.token == Symbol.APOSTROPHE) {
      return new CharFactor(lvalue, lexer.getCharValue());
    }
    // Expr
    else if(lexer.token == Symbol.IDENT)
      return new CompositeFactor(lvalue, leftValue());
    // LValue
    else
      return new ExpressionFactor(lvalue, expression());
  }

  public LValue leftValue() {
    if(lexer.token != Symbol.IDENT)
      error.signal("Identifier expected");

    String identifier = lexer.getStringValue();
    ExpressionStatement expression = null;

    lexer.nextToken();

    if(lexer.token == Symbol.LEFTBRACKET) {
      lexer.nextToken();
      expression = expression();

      if(lexer.token != Symbol.RIGHTBRACKET)
        error.signal("']' expected");
    }

    return new LValue(identifier, expression);
  }

  public Numberino number() {
    Integer multiplier = 1;

    if(lexer.token != Symbol.PLUS &&
    lexer.token != Symbol.MINUS &&
    lexer.token != Symbol.NUMBER)
      error.signal("Number expected");

    if(lexer.token == Symbol.MINUS) {
      multiplier = -1;
      lexer.nextToken();
    } else if(lexer.token == Symbol.PLUS)
      lexer.nextToken();

    if(lexer.token != Symbol.NUMBER)
      error.signal("Number expected");

    StringBuffer number = new StringBuffer();
    number.append(lexer.getNumberValue());

    if(lexer.token == Symbol.DOT) {
      number.append(lexer.getNumberValue());
      lexer.nextToken();

      if(lexer.token != Symbol.NUMBER)
        error.signal("Number expected");

      number.append(lexer.getNumberValue());

      return new DoubleNumber(Double.parseDouble(number.toString()) * multiplier);
    } else {
      return new IntNumber(Integer.parseInt(number.toString()) * multiplier);
    }
  }
}
