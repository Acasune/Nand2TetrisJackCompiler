package nand2tetris;

public enum VarAttributionType {
  STATIC("static"),
  FIELD("field"),
  ARG("argument"),
  VAR("var"),
  NONE("none");

  String value;
  VarAttributionType(String value) {
    this.value=value;
  }
}
