package nand2tetris;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompilationEngine {

  private static final Set<String> classVarDecSet = Stream.of("static", "field").collect(Collectors.toSet());
  private static final Set<String> subroutineDecSet = Stream.of("constructor", "function", "method", "void").collect(Collectors.toSet());
  private static final Set<String> opSet = Stream.of("+", "-", "*", "/", "&", "|", "<", ">", "=").collect(Collectors.toSet());
  private static final Set<String> unaryOpSet = Stream.of("-", "~").collect(Collectors.toSet());


  JackTokenizer jt;
  BufferedWriter writer;
  private int indentDepth;

  CompilationEngine() {
    this.indentDepth = 0;
  }


  CompilationEngine setUp(JackTokenizer jt, BufferedWriter writer) {
    this.jt = jt;
    this.writer = writer;
    return this;
  }

  public void compileClass() throws Exception {
    this.jt.advance();
    preTermWriter("class"); // <class>

    keywordWriter(); // <keyword> class </keyword>
    identifierWriter(); // <identifier> Main </identifier>
    symbolWriter(); // <symbol> { </symbol>

    // Variable declaration
    if (classVarDecSet.contains(jt.getKeyword())) {
      compileClassVarDec();
    }

    // class subroutine declaration
    while (this.jt.getTokenType()==TokenType.KEY_WORD&&subroutineDecSet.contains(jt.getKeyword())) {
      compileSubroutine();
    }

    nonTermWriter(jt.getTagType(), jt.getSymbol()); // <symbol> } </symbol>
    postTermWriter("class"); // </class>

  }

  public void compileClassVarDec() throws Exception {
    String tag = "classVarDec";

    while (this.jt.getTokenType() == TokenType.KEY_WORD && classVarDecSet.contains(this.jt.getKeyword())) {
      preTermWriter(tag); // <classVarDec>
      keywordWriter(); // <keyword> static/field <keyword>
      returnTypeWriter(); // <identifier> $Var.type <identifier>
      identifierWriter(); // <identifier> $Var.name1 </identifier>

      while (this.jt.getTokenType() == TokenType.SYMBOL && ",".equals(this.jt.getSymbol())) {
        symbolWriter(); // <symbol> , <symbol>
        identifierWriter(); // <identifier> $Var.name2 </identifier>
      }
      symbolWriter(); // <symbol> ; </symbol>
      postTermWriter(tag); // </classVarDec>
    }



  }

  public void compileSubroutine() throws Exception {
    String subroutineDecTag = "subroutineDec";
    String subroutineBodyTag = "subroutineBody";

    preTermWriter(subroutineDecTag); // <subroutineDec>

    keywordWriter(); // <keyword> constructor or etc... <keyword>
    returnTypeWriter();
    identifierWriter(); // <identifier> $Subroutine.name </identifier>
    symbolWriter(); // <symbol> ( </symbol>
    compileParameterList();
    symbolWriter(); // <symbol> ) </symbol>

    // Subroutine Body
    preTermWriter(subroutineBodyTag); // <subroutineBody>
    symbolWriter();
    // VarDec*
    compileVarDec();
    // statements
    if (this.jt.getTokenType()!=TokenType.SYMBOL || "}".equals(this.jt.getSymbol())) {
      compileStatements();
    }

    // closing
    symbolWriter(); // }
    postTermWriter(subroutineBodyTag); // </subroutineBody>
    postTermWriter(subroutineDecTag); // </subroutineDec>

  }


  public void compileParameterList() throws Exception {
    String tag = "parameterList";
    preTermWriter(tag); // <parameterList>

    while (this.jt.getTokenType() != TokenType.SYMBOL || !")".equals(this.jt.getSymbol())) {
      returnTypeWriter();
      identifierWriter();
      while (this.jt.getTokenType() == TokenType.SYMBOL && ",".equals(this.jt.getSymbol())) {
        symbolWriter(); // ,
        returnTypeWriter();
        identifierWriter();
      }
    }
    postTermWriter(tag); // <parameterList>


  }

  public void compileVarDec() throws Exception {
    String tag = "varDec";

    while (this.jt.getTokenType() == TokenType.KEY_WORD && "var".equals(this.jt.getKeyword())) {
      preTermWriter(tag);
      keywordWriter(); // var
      returnTypeWriter(); // type
      identifierWriter(); //var.name


      // (',' varName)*
      while (this.jt.getTokenType() == TokenType.SYMBOL && ",".equals(this.jt.getSymbol())) {
        symbolWriter(); // ,
        identifierWriter(); // varName
      }
      symbolWriter(); // <symbol> ; </symbol>
      postTermWriter(tag); // </varDec>

    }

  }

  public void compileStatements() throws Exception {
    String tag = "statements";
    preTermWriter(tag);
    while (this.jt.getTokenType() != TokenType.SYMBOL || !("}".equals(this.jt.getSymbol()))) {
      switch (this.jt.getKeyword()) {
        case "let":
          compileLet();
          break;
        case "if":
          compileIf();
          break;
        case "while":
          compileWhile();
          break;
        case "do":
          compileDo();
          break;
        case "return":
          compileReturn();
          break;
      }
    }
    postTermWriter(tag);
  }


  public void compileLet() throws Exception {
    String tag = "letStatement";
    preTermWriter(tag);
    keywordWriter();
    identifierWriter(); // var.name
    if (this.jt.getTokenType() == TokenType.SYMBOL && "[".equals(this.jt.getSymbol())) {
      symbolWriter();
      compileExpression();
      symbolWriter();
    }
    symbolWriter(); // =
    compileExpression();
    symbolWriter(); //;
    postTermWriter(tag);

  }

  public void compileIf() throws Exception {
    String tag = "ifStatement";
    preTermWriter(tag);
    keywordWriter();
    symbolWriter();
    compileExpression();
    symbolWriter();
    symbolWriter();
    compileStatements();
    symbolWriter();
    if (this.jt.getTokenType() == TokenType.KEY_WORD && "else".equals(this.jt.getKeyword())) {
      keywordWriter(); //else
      symbolWriter(); // {
      compileStatements();
      symbolWriter(); // }

    }
    postTermWriter(tag);
  }

  public void compileWhile() throws Exception {
    String tag = "whileStatement";
    preTermWriter(tag);
    keywordWriter();
    symbolWriter();
    compileExpression();
    symbolWriter();
    symbolWriter();
    compileStatements();
    symbolWriter();
    postTermWriter(tag);
  }

  public void compileDo() throws Exception {
    String tag = "doStatement";
    preTermWriter(tag);
    keywordWriter(); // do
    // Subroutine Call
    identifierWriter();

    if (this.jt.getTokenType()==TokenType.SYMBOL && ".".equals(this.jt.getSymbol())) {
      symbolWriter();
      identifierWriter();
    }
    symbolWriter();
    compileExpressionList();
    symbolWriter();
    symbolWriter(); // ;
    postTermWriter(tag);

  }

  public void compileReturn() throws Exception {
    String tag = "returnStatement";
    preTermWriter(tag);
    keywordWriter();
    if (this.jt.getTokenType()!=TokenType.SYMBOL||!(";".equals(this.jt.getSymbol()))){
      compileExpression();
    }
    symbolWriter();
    postTermWriter(tag);

  }

  public void compileExpression() throws Exception {
    String tag = "expression";
    preTermWriter(tag);
    compileTerm();
    while (this.jt.getTokenType() == TokenType.SYMBOL && opSet.contains(this.jt.getSymbol())) {
      symbolWriter();
      compileTerm();
    }
    postTermWriter(tag);
  }

  public void compileTerm() throws Exception {
    String tag = "term";
    preTermWriter(tag);

    TokenType tt=this.jt.getTokenType();

      if(tt==TokenType.INT_CONST) {
        intConstWriter();
      } else if (tt==TokenType.STRING_CONST) {
        stringConstWriter();
      } else if (tt==TokenType.KEY_WORD) {
        keywordWriter();
      }  else if (tt==TokenType.IDENTIFIER) {
        identifierWriter();
        if (this.jt.getTokenType() == TokenType.SYMBOL && "[".equals(this.jt.getSymbol())) {
          symbolWriter();
          compileExpression();
          symbolWriter();
        } else if (this.jt.getTokenType() == TokenType.SYMBOL && "(".equals(this.jt.getSymbol())) {
          symbolWriter();
          compileExpressionList();
          symbolWriter();
        } else if (this.jt.getTokenType() == TokenType.SYMBOL && ".".equals(this.jt.getSymbol())){
          symbolWriter();
          identifierWriter();
          symbolWriter();
          compileExpressionList();
          symbolWriter();
        }
      } else if (tt==TokenType.SYMBOL) {
        if ("(".equals(this.jt.getSymbol())) {
          symbolWriter();
          compileExpression();
          symbolWriter();
        } else if(unaryOpSet.contains(this.jt.getSymbol())) {
          symbolWriter();
          compileTerm();
        }
      }

    postTermWriter(tag);

  }

  public void compileExpressionList() throws Exception {
    String tag="expressionList";
    preTermWriter(tag);
    while(this.jt.getTokenType()!=TokenType.SYMBOL||!(")".equals(this.jt.getSymbol()))) {
      compileExpression();
      if (this.jt.getTokenType()==TokenType.SYMBOL && (",".equals(this.jt.getSymbol()))){
        symbolWriter();
      }
    }
    postTermWriter(tag);

  }


  /**
   * Private method
   */

  private void lineWriter(String line) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.indentDepth; i++) {
      sb.append(" ");
    }
    sb.append(line).append("\n");
    writer.write(sb.toString());
  }

  private void nonTermWriter(String tag, String value) throws IOException {
    String line = "<" + tag + ">" + value + "</" + tag + ">";
    lineWriter(line);
    this.jt.advance();
  }

  private void preTermWriter(String tag) throws IOException {
    String line = String.format("<%s>", tag);
    lineWriter(line);
    this.indentDepth += 2;
  }

  private void postTermWriter(String tag) throws IOException {
    this.indentDepth += -2;
    String line = String.format("</%s>", tag);
    lineWriter(line);
  }

  private void returnTypeWriter() throws Exception {
    // return type: primitives and identifier
    if (jt.getTokenType() == TokenType.KEY_WORD) {
      keywordWriter(); // <keyword> $Subroutine.type(primitive) <keyword>
    } else {
      identifierWriter(); // <identifier> $Subroutine.type(user definition) <identifier>
    }

  }

  private void keywordWriter() throws Exception {
    // keyword: class, constructor and etc...
    nonTermWriter(jt.getTagType(), jt.getKeyword()); // <keyword> class <keyword>
  }

  private void identifierWriter() throws Exception {
    // identifier: user definition terms.
    nonTermWriter(jt.getTagType(), jt.getIdentifier()); // <identifier> $Var.name </identifier>
  }

  private void symbolWriter() throws Exception {
    // symbol: ;,{,},{ and etc...
    String escape=jt.getSymbol();
    if ("\"".equals(escape)){
      escape = "&qout;";
    } else if ("&".equals(escape)) {
      escape = "&amp;";
    } else if ("<".equals(escape)) {
      escape = "&lt;";
    } else if (">".equals(escape)) {
      escape = "&gt;";
    }

    nonTermWriter(jt.getTagType(), escape); // <symbol> ; </symbol>
  }

  private void intConstWriter() throws Exception {
    // intConstant:
    nonTermWriter(jt.getTagType(), String.valueOf(jt.getIntVal()));
  }

  private void stringConstWriter() throws Exception {
    // StringConstant:
    nonTermWriter(jt.getTagType(), jt.getStringVal().substring(1,jt.getStringVal().length()-1));
  }


}
