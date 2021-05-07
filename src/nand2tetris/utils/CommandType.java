package nand2tetris.utils;

public enum CommandType {
  ADD("add"),
  SUB("sub"),
  MUT("call Math.multiply 2"),
  DIV("call Math.divide 2"),
  NEQ("neg"),
  EQ("="),
  GT(">"),
  LT("<"),
  AND("&"),
  OR("|"),
  NOT("~");
  String cmd;
  CommandType(String cmd) {
    this.cmd=cmd;
  }
  public String getCmd() {
    return this.cmd;
  }

  public static CommandType getEnum(String str) throws Exception {
    switch (str) {
      case "+":
        return ADD;
      case "-":
        return SUB;
      case "*":
        return MUT;
      case "/":
        return DIV;
      case "=":
        return EQ;
      case ">":
        return GT;
      case "<":
        return LT;
      case "&":
        return AND;
      case "|":
        return OR;
      default:
        throw  new Exception();
    }
  }

}
