package nand2tetris.utils;

public enum TokenType {
  KEY_WORD("keyword"),
  SYMBOL("symbol"),
  IDENTIFIER("identifier"),
  INT_CONST("integerConstant"),
  STRING_CONST("stringConstant");

  private String tag;

  TokenType(String tag) {
    this.tag = tag;
  }

  public String getTag() {
    return this.tag;
  }
}
