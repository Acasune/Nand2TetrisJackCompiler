package nand2tetris.utils;

public class SymbolValueBean {
  private final String name;
  private final String type;
  private final VarAttributionType kind;
  private int index;

  public SymbolValueBean(String name, String type, VarAttributionType kind, int index) {
    this.name = name;
    this.type = type;
    this.kind = kind;
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public VarAttributionType getKind() {
    return kind;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

}
