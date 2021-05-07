package nand2tetris.utils;

import nand2tetris.utils.VarAttributionType;

public class SymbolValueBean {
  private String name;
  private String type;
  private VarAttributionType kind;
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
