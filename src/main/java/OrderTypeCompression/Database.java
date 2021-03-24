package OrderTypeCompression;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class Database {

  private final int n;
  private final List<Points> database;

  public Database(String pathname, int n, int s) {
    this.n = n;
    database = new ArrayList<>();

    System.err.printf("Reading order types database for %d points from %s\n", n, pathname);
    File file = new File(pathname);

    byte[] bFile = readContentIntoByteArray(file);
    assert bFile.length % n == 0;

    if (s == 8) {
      interpret8BitsBinaryFile(bFile);
    } else if (s == 16) {
      interpret16BitsBinaryFile(bFile);
    }

  }

  public static Database read(int n) {
    int[] size = new int[]{0, 0, 0,
        8, 8, 8,
        8, 8, 8,
        16, 16, 32};
    String filename = String.format("otypes%02d.b%02d", n, size[n]);
    String pathname = String.join(File.separator,
        System.getProperty("user.dir"), "data", filename);
    return new Database(pathname, n, size[n]);
  }

  public static int databaseSize(int n) {
    int[] size = new int[]{0, 0, 0,
        1, 2, 3,
        16, 135, 3_315,
        158_817, 14_309_547, 0};
    return size[n];
  }

  public static Points interpret16BitsByteArray(byte[] bFile, int n, int offset) {
    Points points = new Points();
    for (int j = 0; j < n; j++) {
      int x =
          Byte.toUnsignedInt(bFile[offset + 4 * j])
              + Byte.toUnsignedInt(bFile[offset + 4 * j + 1]) * 256;
      int y = Byte.toUnsignedInt(bFile[offset + 4 * j + 2])
          + Byte.toUnsignedInt(bFile[offset + 4 * j + 3]) * 256;
      Point p = new Point(x, y);
      points.add(p);
    }
    return points;
  }

  public static Points interpret16BitsByteArray(byte[] bFile, int n) {
    return interpret16BitsByteArray(bFile, n, 0);
  }

  public static Points interpret8BitsByteArray(byte[] bFile, int n, int offset) {
    Points points = new Points();
    for (int j = 0; j < n; j++) {
      int x = Byte.toUnsignedInt(bFile[offset + 2 * j]);
      int y = Byte.toUnsignedInt(bFile[offset + 2 * j + 1]);
      Point p = new Point(x, y);
      points.add(p);
    }
    return points;
  }

  public static Points interpret8BitsByteArray(byte[] bFile, int n) {
    return interpret8BitsByteArray(bFile, n, 0);
  }

  public int getN() {
    return n;
  }

  public int size() {
    return database.size();
  }

  public Points get(int a) {
    return database.get(a);
  }

  private void interpret16BitsBinaryFile(byte[] bFile) {
    for (int i = 0; i < bFile.length; i += 4 * n) {
      Points points = interpret16BitsByteArray(bFile, n, i);
      database.add(points);
    }
  }

  private void interpret8BitsBinaryFile(byte[] bFile) {
    for (int i = 0; i < bFile.length; i += 2 * n) {
      Points points = interpret8BitsByteArray(bFile, n, i);
      database.add(points);
    }
  }

  private byte[] readContentIntoByteArray(File file) {
    FileInputStream fileInputStream;
    byte[] bytes = new byte[(int) file.length()];
    try {
      //convert file into array of bytes
      fileInputStream = new FileInputStream(file);
      fileInputStream.read(bytes);
      fileInputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return bytes;
  }

  @Override
  public String toString() {
    return String.format("%s", database);
  }

}
