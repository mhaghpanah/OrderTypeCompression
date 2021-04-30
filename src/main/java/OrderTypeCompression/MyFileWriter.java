package OrderTypeCompression;

import OrderTypeCompression.MyFile.Address;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileWriter {

  File file;

  public MyFileWriter(String suffixPath, Address prefix) {
    file = MyFile.getInstance(suffixPath, prefix);
  }

  public static boolean write(String pathname, String str) {
    MyFileWriter w = new MyFileWriter(pathname, Address.RESULTS);
    return w.writeFile(str);
  }

  public static boolean write(String pathname, String str, Address prefix) {
    MyFileWriter w = new MyFileWriter(pathname, prefix);
    return w.writeFile(str);
  }

  public boolean writeFile(String str) {
    try {
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write(str);
      fileWriter.close();
      System.err.printf("Successfully wrote to %s\n", file.getAbsoluteFile());
      return true;
    } catch (IOException e) {
      System.err.println("An error occurred.");
      e.printStackTrace();
      return false;
    }
  }

}
