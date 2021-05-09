package nand2tetris.utils;

public enum VarAttributionType {
  STATIC("static"),
  FIELD("field"),
  ARG("argument"),
  VAR("var"),
  LOCAL("local"),
  NONE("none");

  String value;

  VarAttributionType(String value) {
    this.value = value;
  }

  public static VarAttributionType getEnum(String str) {
    switch (str) {
      case "static":
        return STATIC;
      case "field":
        return FIELD;
      case "arg":
        return ARG;
      case "var":
        return VAR;
      case "local":
        return LOCAL;
      default:
        return NONE;
    }
  }
}
