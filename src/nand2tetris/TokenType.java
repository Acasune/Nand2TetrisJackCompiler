package nand2tetris;

public enum TokenType {
  KEY_WORD("keyword"),
  SYMBOL("symbol"),
  IDENTIFIER("identifier"),
  INT_CONST("integerConstant"),
  STRING_CONST("stringConstant");

  private String tag;
  TokenType(String tag) {
    this.tag=tag;
  }
  String getTag() {
    return this.tag;
  }

}
