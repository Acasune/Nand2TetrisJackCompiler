package nand2tetris;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JackAnalyzer {
  static Pattern commentPattern = Pattern.compile("(.*)(\\/\\/.*)");

  public static void main(String[] args) throws Exception {

    JackTokenizer tokenizer;
    CompilationEngine engine = new CompilationEngine();
    List<File> targetFiles = new ArrayList<>();

    if (args.length != 1) {
      File workingDir = new File("./");
      for (String fileName : workingDir.list()) {
        int lastDotPos = fileName.lastIndexOf('.');
        if (lastDotPos != 1 && fileName.substring(lastDotPos + 1).equals("jack")) {
          targetFiles.add(new File(fileName));
        }
      }
    } else {
      String inputFile = args[0];
      int lastDotPos = inputFile.lastIndexOf('.');
      if (lastDotPos == -1) {
        System.out.println("The file is not .jack file");
        System.exit(1);
      }
      targetFiles.add(new File(inputFile));
    }


    for (File targetFile : targetFiles) {
      try (BufferedReader reader = new BufferedReader(new FileReader(targetFile))) {
        tokenizer = new JackTokenizer(reader.lines().collect(Collectors.toList()));
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s.xml",targetFile)))) {

        engine.setUp(tokenizer,writer);
        engine.compileClass();

      } catch (IOException e) {
        e.printStackTrace();
      }


    }

  }

}