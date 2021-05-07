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
}
