package nand2tetris;

import nand2tetris.utils.CommandType;
import nand2tetris.utils.SegmentType;
import nand2tetris.utils.TokenType;
import nand2tetris.utils.VarAttributionType;

import java.io.BufferedWriter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompilationEngine {

  private static final Set<String> classVarDecSet = Stream.of("static", "field").collect(Collectors.toSet());
  private static final Set<String> subroutineDecSet = Stream.of("constructor", "function", "method", "void").collect(Collectors.toSet());
  private static final Set<String> opSet = Stream.of("+", "-", "*", "/", "&", "|", "<", ">", "=").collect(Collectors.toSet());
  private static final Set<String> unaryOpSet = Stream.of("-", "~").collect(Collectors.toSet());

  private static final String CONSTRUCTOR = "constructor";
  private static final String METHOD = "method";
  private static final String VOID = "void";


  private JackTokenizer jt;
  private VMWritter writer;
  private SymbolTable table;
  private int indentDepth;
  private String className;
  private int condIdx;

  CompilationEngine() {
    this.indentDepth = 0;
    this.condIdx = 0;
  }


  CompilationEngine setUp(JackTokenizer jt, BufferedWriter bWriter) {
    this.jt = jt;
    this.writer = new VMWritter(bWriter);
    this.table = new SymbolTable();
    return this;
  }

  public void compileClass() throws Exception {
    this.jt.advance();

    keywordGetter(); // class
    this.className = identifierGetter(); // Main
    symbolGetter(); // {

    // Variable declaration
    if (classVarDecSet.contains(jt.getKeyword())) {
      compileClassVarDec();
    }

    // class subroutine declaration
    while (this.jt.getTokenType() == TokenType.KEY_WORD && subroutineDecSet.contains(jt.getKeyword())) {
      compileSubroutine();
    }

    symbolGetter(); // }

  }

  public void compileClassVarDec() throws Exception {
    // Example: static boolean varName;

    while (this.jt.getTokenType() == TokenType.KEY_WORD && classVarDecSet.contains(this.jt.getKeyword())) {
      String
          kwd = keywordGetter(), // static/field
          type = returnTypeGetter(), //$Var.type
          name = identifierGetter();  // $Var.name

      this.table.define(name, type, VarAttributionType.getEnum(kwd));

      while (this.jt.getTokenType() == TokenType.SYMBOL && ",".equals(this.jt.getSymbol())) {
        symbolGetter(); // ,
        name = identifierGetter(); //$Var.name
        this.table.define(name, type, VarAttributionType.getEnum(kwd));
      }
      symbolGetter(); // ;

    }

  }

  public void compileSubroutine() throws Exception {

    int nLocalVars = 0;
    String subroutineName, subroutineType, returnType;
    this.table.startSubroutine();

    subroutineType = keywordGetter(); // constructor or etc...
    returnType = returnTypeGetter(); // $Subroutine.returnType
    subroutineName = identifierGetter(); // $Subroutine.name

    if (subroutineType == METHOD) {
      this.table.define("this_ptr", "int", VarAttributionType.ARG);
    }

    symbolGetter(); // (
    compileParameterList();
    symbolGetter(); // )

    // Subroutine Body
    symbolGetter(); // {

    // VarDec*

    if (this.jt.getTokenType() == TokenType.KEY_WORD && "var".equals(this.jt.getKeyword())) {
      nLocalVars = compileVarDec();

    }

    this.writer.writeFunction(this.className + "." + subroutineName, nLocalVars);

    if (subroutineType.equals(CONSTRUCTOR)) {
      this.writer.writeAlloc(this.table.varCount(VarAttributionType.FIELD));
      this.writer.writePop(SegmentType.POINTER, 0);

    }
    if (subroutineType.equals(METHOD)) {
      this.writer.writePush(SegmentType.ARG, 0);
      this.writer.writePop(SegmentType.POINTER, 0);

    }

    // statements
    if (this.jt.getTokenType() != TokenType.SYMBOL || "}".equals(this.jt.getSymbol())) {
      compileStatements();
    }

    // closing
    symbolGetter(); // }

    if (returnType.equals(VOID)) {
      this.writer.writePush(SegmentType.CONST, 0);
    }
    this.writer.writeReturn();

  }


  public void compileParameterList() throws Exception {
    //  Args of subroutines

    while (this.jt.getTokenType() != TokenType.SYMBOL || !")".equals(this.jt.getSymbol())) {
      String
          type = returnTypeGetter(),
          name = identifierGetter();
      this.table.define(name, type, VarAttributionType.ARG);

      while (this.jt.getTokenType() == TokenType.SYMBOL && ",".equals(this.jt.getSymbol())) {
        symbolGetter(); // ,
        type = returnTypeGetter();  // $Subroutine.returnType
        name = identifierGetter(); // $Subroutine.name
        this.table.define(name, type, VarAttributionType.ARG);
      }
    }


  }

  public int compileVarDec() throws Exception {
    int nLocalVar = 0;
    // Example: var int i, j;

    while (this.jt.getTokenType() == TokenType.KEY_WORD && "var".equals(this.jt.getKeyword())) {
      String
          kwd = keywordGetter(), // "var"
          type = returnTypeGetter(), // $var.type
          name = identifierGetter(); // $var.name

      this.table.define(name, type, VarAttributionType.VAR);
      nLocalVar += 1;

      // (',' varName)*
      while (this.jt.getTokenType() == TokenType.SYMBOL && ",".equals(this.jt.getSymbol())) {
        symbolGetter(); // ,
        name = identifierGetter(); // varName
        this.table.define(name, type, VarAttributionType.VAR);
        nLocalVar += 1;
      }
      symbolGetter(); // ;
    }
    return nLocalVar;
  }

  public void compileStatements() throws Exception {
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
  }

  public void compileLet() throws Exception {
    // Example: let s = "string constant";
    String varName;
    boolean isArray = false;
    keywordGetter(); // let
    varName = identifierGetter(); // $var.name
    if (this.jt.getTokenType() == TokenType.SYMBOL && "[".equals(this.jt.getSymbol())) {
      isArray = true;
      varPushWriter(varName);
      symbolGetter(); // [
      compileExpression();
      symbolGetter(); // ]
      this.writer.writeArithmetic(CommandType.ADD);
      this.writer.writePop(SegmentType.POINTER, 1);
    }

    symbolGetter(); // =
    compileExpression();
    if (isArray) {
      this.writer.writePop(SegmentType.THAT, 0);
    } else {
      varPopWriter(varName);
    }
    symbolGetter(); //;

  }

  public void compileIf() throws Exception {

    String trueLabel, falseLabel, ifendLabel;
    this.condIdx += 1;

    keywordGetter(); // if
    symbolGetter();  // (
    compileExpression();
    symbolGetter(); // )

    trueLabel = String.format("L_ifT%d", this.condIdx);
    falseLabel = String.format("L_ifF%d", this.condIdx);
    ifendLabel = String.format("L_ifEND%d", this.condIdx);

    this.writer.writeArithmetic(CommandType.NOT);
    this.writer.writeIf(falseLabel); // if false is correct, then jump to falseLabel over true label

    symbolGetter(); // {
    this.writer.writeLabel(trueLabel);
    compileStatements();
    this.writer.writeGoto(ifendLabel);
    symbolGetter(); // }
    this.writer.writeLabel(falseLabel);
    if (this.jt.getTokenType() == TokenType.KEY_WORD && "else".equals(this.jt.getKeyword())) {
      keywordGetter(); //else
      symbolGetter(); // {
      compileStatements();
      symbolGetter(); // }

    }

    this.writer.writeLabel(ifendLabel);

  }

  public void compileWhile() throws Exception {
    this.condIdx += 1;
    String whileBeginLabel = String.format("L_WB%d", this.condIdx),
        whileEndLabel = String.format("L_WE%d", this.condIdx);

    this.writer.writeLabel(whileBeginLabel);
    keywordGetter(); // while
    symbolGetter();  // (
    compileExpression();
    symbolGetter();  // )


    this.writer.writeArithmetic(CommandType.NOT);
    this.writer.writeIf(whileEndLabel);
    symbolGetter();  // {
    compileStatements();
    symbolGetter();  // }
    this.writer.writeGoto(whileBeginLabel);

    this.writer.writeLabel(whileEndLabel);

  }

  public void compileDo() throws Exception {

    String name;
    keywordGetter(); // Do
    // Subroutine Call
    name = identifierGetter(); // $class.name or $subroutine.name
    subroutineWriter(name);
    this.writer.writePop(SegmentType.TEMP, 0); // Because the returnType is void;
    symbolGetter(); // ;
  }

  public void compileReturn() throws Exception {

    keywordGetter(); // return
    if (this.jt.getTokenType() != TokenType.SYMBOL || !(";".equals(this.jt.getSymbol()))) {
      compileExpression();
    }
    symbolGetter(); //;

  }

  public void compileExpression() throws Exception {

    compileTerm();
    while (this.jt.getTokenType() == TokenType.SYMBOL && opSet.contains(this.jt.getSymbol())) {
      CommandType ct = CommandType.getEnum(symbolGetter()); // Expect: Arithmetic op
      compileTerm();
      this.writer.writeArithmetic(ct);
    }
  }

  public void compileTerm() throws Exception {

    TokenType tt = this.jt.getTokenType();

    if (tt == TokenType.INT_CONST) {
      this.writer.writePush(SegmentType.CONST, intConstGetter());

    } else if (tt == TokenType.STRING_CONST) {
      this.writer.writeString(stringConstGetter());

    } else if (tt == TokenType.KEY_WORD) {
      String kwd = keywordGetter();
      if (kwd.equals("false") || kwd.equals("null")) {
        this.writer.writePush(SegmentType.CONST, 0);

      } else if (kwd.equals("true")) {
        this.writer.writePush(SegmentType.CONST, 0);
        this.writer.writeArithmetic(CommandType.NOT);

      } else if (kwd.equals("this")) {
        this.writer.writePush(SegmentType.POINTER, 0);

      } else {
        throw new Exception("Unreachable!!");
      }

    } else if (tt == TokenType.IDENTIFIER) {
      String varName = identifierGetter();
//      VarAttributionType varKind = this.table.kindOf(varName);
      varPushWriter(varName);
      if (this.jt.getTokenType() == TokenType.SYMBOL && "[".equals(this.jt.getSymbol())) {
        // Array patterns
        symbolGetter(); // [
        this.writer.writePush(SegmentType.POINTER, 1);
        this.writer.writePop(SegmentType.TEMP, 0);
        compileExpression();
        this.writer.writeArithmetic(CommandType.ADD);
        this.writer.writePop(SegmentType.POINTER, 1);
        this.writer.writePush(SegmentType.THAT, 0);
        this.writer.writePop(SegmentType.TEMP, 1);
        this.writer.writePush(SegmentType.TEMP, 0);
        this.writer.writePop(SegmentType.POINTER, 1);
        this.writer.writePush(SegmentType.TEMP, 1);
        symbolGetter(); //]
      } else if (this.jt.getTokenType() == TokenType.SYMBOL && "(".equals(this.jt.getSymbol())) {
        // Subroutine patterns
        symbolGetter(); // (
        subroutineWriter(varName);
        symbolGetter(); // )
      } else if (this.jt.getTokenType() == TokenType.SYMBOL && ".".equals(this.jt.getSymbol())) {
        subroutineWriter(varName);
      }
    } else if (tt == TokenType.SYMBOL) {
      if ("(".equals(this.jt.getSymbol())) {
        symbolGetter(); // (
        compileExpression();
        symbolGetter(); //)
      } else if (unaryOpSet.contains(this.jt.getSymbol())) {
        String symbol = symbolGetter();
        CommandType ct = null;
        compileTerm();
        if (symbol.equals("-")) {
          ct = CommandType.NEQ;
        } else if (symbol.equals("~")) {
          ct = CommandType.NOT;
        }
        this.writer.writeArithmetic(ct);
      }
    }

  }

  public int compileExpressionList() throws Exception {
    int nExpression = 0;

    while (this.jt.getTokenType() != TokenType.SYMBOL || !(")".equals(this.jt.getSymbol()))) {
      compileExpression();
      nExpression += 1;
      if (this.jt.getTokenType() == TokenType.SYMBOL && (",".equals(this.jt.getSymbol()))) {
        symbolGetter();
      }
    }
    return nExpression;

  }


  /**
   * Private method
   */


  private String returnTypeGetter() throws Exception {
    // return type: primitives and identifier
    String ret;
    if (jt.getTokenType() == TokenType.KEY_WORD) {
      ret = keywordGetter(); // $Subroutine.type(primitive)
    } else {
      ret = identifierGetter(); // $Subroutine.type(user definition)
    }
    return ret;
  }

  private String keywordGetter() throws Exception {
    // keyword: class, constructor and etc...
    String ret = this.jt.getKeyword();
    this.jt.advance();
    return ret;
  }

  private String identifierGetter() throws Exception {
    // identifier: user definition terms.
    String ret = this.jt.getIdentifier(); // $Var.name
    this.jt.advance();
    return ret;
  }

  private String symbolGetter() throws Exception {
    // symbol: ;,{,},{ and etc...
    String ret = this.jt.getSymbol();
    this.jt.advance();
    return ret;
  }

  private int intConstGetter() throws Exception {
    // intConstant:
    int ret = this.jt.getIntVal();
    this.jt.advance();
    return ret;
  }

  private String stringConstGetter() throws Exception {
    // StringConstant:
    String ret = this.jt.getStringVal().substring(1, this.jt.getStringVal().length() - 1);
    this.jt.advance();
    return ret;
  }

  private void varPushWriter(String varName) throws Exception {
    VarAttributionType vType = this.table.kindOf(varName);
    if (vType == VarAttributionType.NONE) {
      return;
    }
    int varIdx = this.table.indexOf(varName);
    switch (vType) {
      case STATIC:
        this.writer.writePush(SegmentType.STATIC, varIdx);
        break;
      case FIELD:
        this.writer.writePush(SegmentType.THIS, varIdx);
        break;
      case ARG:
        this.writer.writePush(SegmentType.ARG, varIdx);
        break;
      case VAR:
        this.writer.writePush(SegmentType.LOCAL, varIdx);
        break;
    }
  }

  private void varPopWriter(String varName) throws Exception {
    VarAttributionType vType = this.table.kindOf(varName);
    if (vType == VarAttributionType.NONE) {
      return;
    }
    int varIdx = this.table.indexOf(varName);
    switch (vType) {
      case STATIC:
        this.writer.writePop(SegmentType.STATIC, varIdx);
        break;
      case FIELD:
        this.writer.writePop(SegmentType.THIS, varIdx);
        break;
      case ARG:
        this.writer.writePop(SegmentType.ARG, varIdx);
        break;
      case VAR:
        this.writer.writePop(SegmentType.LOCAL, varIdx);
        break;
    }
  }

  private void subroutineWriter(String name) throws Exception {
    String callName, methodName;
    boolean isPushPointer = false;

    if (this.jt.getTokenType() == TokenType.SYMBOL && ".".equals(this.jt.getSymbol())) {
      symbolGetter(); // .
      methodName = identifierGetter(); // $subroutine.name
      VarAttributionType kind = this.table.kindOf(name);
      if (kind == VarAttributionType.NONE) {
        callName = String.format("%s.%s", name, methodName);
      } else {
        String type = this.table.typeOf(name);
        callName = String.format("%s.%s", type, methodName);
        isPushPointer = true;
        varPushWriter(name);
      }
    } else {
      isPushPointer = true;
      this.writer.writePush(SegmentType.POINTER, 0);
      callName = String.format("%s.%s", this.className, name);
    }
    symbolGetter(); // (
    int nParam = compileExpressionList();
    symbolGetter(); // )
    if (isPushPointer) {
      nParam += 1;
    }
    this.writer.writeCall(callName, nParam);
  }


}
