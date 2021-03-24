package OrderTypeCompression;

import java.io.File;
import java.io.IOException;

public class MyFile {

  private String pathname;
  private File file;

  public MyFile(String suffixPath, Address prefix) {
    if (prefix == Address.ROOT) {
      pathname = suffixPath;
    } else if (prefix == Address.RESULTS) {
      pathname = String.join(File.separator,
          System.getProperty("user.dir"), "results", suffixPath);
    } else if (prefix == Address.RESOURCE) {
      pathname = String.join(File.separator,
          System.getProperty("user.dir"), "src", "main", "resources", suffixPath);
    }
    System.err.printf("MyFile class is processing %s\n", pathname);
    createFile();
  }

  public static File getInstance(String suffixPath, Address prefix) {
    MyFile myFile = new MyFile(suffixPath, prefix);
    return myFile.getFile();
  }

  public static boolean isExists(String suffixPath, Address prefix) {
    String pathname = null;
    if (prefix == Address.ROOT) {
      pathname = suffixPath;
    } else if (prefix == Address.RESULTS) {
      pathname = String.join(File.separator,
          System.getProperty("user.dir"), "results", suffixPath);
    } else if (prefix == Address.RESOURCE) {
      pathname = String.join(File.separator,
          System.getProperty("user.dir"), "src", "main", "resources", suffixPath);
    }
    System.err.printf("MyFile class is processing %s\n", pathname);
    File file = new File(pathname);
    return file.exists();
  }

  public File getFile() {
    return file;
  }

  private void createFile() {
    file = new File(pathname);
    try {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      } else {
        System.err.println("Folder already exists.");
      }
      if (file.createNewFile()) {
        System.err.println("File created: " + file.getName());
      } else {
        System.err.println("File already exists.");
      }
    } catch (IOException e) {
      System.err.println("An error occurred.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public enum Address {
    ROOT, RESULTS, RESOURCE
  }

}
