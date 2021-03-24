package OrderTypeCompression;

import OrderTypeCompression.MyFile.Address;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MyBinaryFileIO {

  File file;

  public MyBinaryFileIO(String suffixPath) {
    file = MyFile.getInstance(suffixPath, Address.RESULTS);
  }

  public static boolean write(String pathname, byte[] bytes) {
    return write(pathname, 0, bytes);
  }

  public static boolean write(String pathname, int offset, byte[] bytes) {
    MyBinaryFileIO w = new MyBinaryFileIO(pathname);
    return w.writeFile(offset, bytes);
  }

  public static byte[] read(String pathname) {
    MyBinaryFileIO w = new MyBinaryFileIO(pathname);
    return w.readFile();
  }

  public static byte[] read(String pathname, int offset, int len) {
    MyBinaryFileIO w = new MyBinaryFileIO(pathname);
    return w.readFile(offset, len);
  }

  public long length() {
    return file.length();
  }

  public void writeFile(byte[] bytes) {
    writeFile(0, bytes);
  }

  public boolean writeFile(int offset, byte[] bytes) {
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
      randomAccessFile.seek(offset);
      randomAccessFile.write(bytes);
      randomAccessFile.close();
      System.err.printf("Successfully wrote to %s\n", file.getAbsoluteFile());
      return true;
    } catch (IOException e) {
      System.err.println("An error occurred.");
      e.printStackTrace();
      return false;
    }
  }

  public byte[] readFile() {
    return readFile(0, (int) file.length());
  }

  public byte[] readFile(int offset, int len) {
    byte[] bytes = new byte[len];
    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
      randomAccessFile.seek(offset);
      randomAccessFile.read(bytes);
      randomAccessFile.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return bytes;
  }

}
