package nand2tetris;

import nand2tetris.utils.CommandType;
import nand2tetris.utils.SegmentType;
import nand2tetris.utils.VarAttributionType;

import java.io.BufferedWriter;
import java.io.IOException;

public class VMWritter {

  BufferedWriter writer;

  VMWritter(BufferedWriter writer) {
    this.writer = writer;
  }

  public void writePush(SegmentType seg, int idx) throws IOException {
    writer.write("push " + seg.getSeg() + " " + idx + "\n");
  }

  public void writePop(SegmentType seg, int idx) throws IOException {
    writer.write("pop " + seg.getSeg() + " " + idx + "\n");
  }

  public void writeArithmetic(CommandType cmd) throws IOException {
    writer.write(cmd.getCmd() + "\n");
  }

  public void writeLabel(String label) throws IOException {
    writer.write("label " + label + "\n");
  }

  public void writeGoto(String label) throws IOException {
    writer.write("goto " + label + "\n");
  }

  public void writeIf(String label) throws IOException {
    writer.write("if-goto " + label + "\n");
  }

  public void writeCall(String name, int nArg) throws IOException {
    writer.write("call " + name + " " + nArg + "\n");
  }

  public void writeFunction(String name, int nLocal) throws IOException {
    writer.write("function " + name + " " + nLocal + "\n");
  }

  public void writeReturn() throws IOException {
    writer.write("return\n");
  }

  public void writeAlloc(int size) throws IOException {
    this.writePush(SegmentType.CONST, size);
    this.writer.write("call Memory.alloc 1\n");

  }

  public void writeString(String str) throws IOException {
    this.writePush(SegmentType.CONST, str.length());
    this.writeCall("String.new", 1);
    for (char c : str.toCharArray()) {
      this.writePush(SegmentType.CONST, (int)c);
      this.writeCall("String.appendChar", 2);

    }
  }

}
