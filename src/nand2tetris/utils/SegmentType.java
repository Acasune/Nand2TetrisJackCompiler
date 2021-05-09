package nand2tetris.utils;

public enum SegmentType {
  CONST("constant"),
  ARG("argument"),
  LOCAL("local"),
  STATIC("static"),
  THIS("this"),
  THAT("that"),
  POINTER("pointer"),
  TEMP("temp");

  String seg;

  SegmentType(String seg) {
    this.seg = seg;
  }

  public String getSeg() {
    return this.seg;
  }
}
