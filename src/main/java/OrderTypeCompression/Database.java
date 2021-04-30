package OrderTypeCompression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Database implements Iterable<Points> {

  private final int n;
  private final int s;
  private List<Points> database;

  private final File file;

  public Database(String pathname, int n, int s) {
    this.n = n;
    this.s = s;
//    database = new ArrayList<>();
    database = null;

    System.err.printf("Reading order types database for %d points from %s\n", n, pathname);
    file = new File(pathname);
  }

  private void initialize() {
    database = new ArrayList<>();
    byte[] bFile = readContentIntoByteArray(file);
    assert bFile.length % n == 0;

    if (s == 8) {
      interpret8BitsBinaryFile(bFile);
    } else if (s == 16) {
      interpret16BitsBinaryFile(bFile);
    }
  }

  public static Database read(int n) {
    String filename = String.format("otypes%02d.b%02d", n, databaseBitSize(n));
    String pathname = String.join(File.separator,
        System.getProperty("user.dir"), "data", filename);
    return new Database(pathname, n, databaseBitSize(n));
  }

  public static int databaseBitSize(int n) {
    int[] bitSize = new int[]{0, 0, 0,
        8, 8, 8,
        8, 8, 8,
        16, 16, 32};
    return bitSize[n];
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

  public static Points interpretBitsByteArray(byte[] bFile, int n) {
    if (Database.databaseBitSize(n) == 8) {
      return interpret8BitsByteArray(bFile, n);
    } else {
      return interpret16BitsByteArray(bFile, n);
    }
  }

  public int getN() {
    return n;
  }

  public int size() {
    return databaseSize(n);
//    return database.size();
  }

  public Points get(int a) {
    if (database == null) initialize();
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

  @Override
  public Iterator<Points> iterator() {
    Iterator<Points> it = null;
    try {
      it = new Iterator<Points>() {

        private final InputStream in = new FileInputStream(file);
        private final int k = 2 * n * (Database.databaseBitSize(n) == 8 ? 1 : 2);
        private final byte[] buf = new byte[k];
        private final ReadKBytes readKBytes = new ReadKBytes(k, in);
        private int index = 0;
        private final int size = Database.databaseSize(n);

        @Override
        public boolean hasNext() {
          return index < size;
        }

        @Override
        public Points next() {
          System.out.println(index);
//          System.out.println(size);
//          System.out.println(k);
          try {
            readKBytes.read(buf);
          } catch (IOException e) {
            e.printStackTrace();
          }
          System.out.println(Arrays.toString(buf));
          index++;
          return interpretBitsByteArray(buf, n);
        }
      };
    } catch (Exception e) {
      e.printStackTrace();
    }
    assert it != null;
    return it;
  }

  public Iterator<Points> sampleIterator(int rate) {
    Iterator<Points> it = null;
    try {
      it = new Iterator<Points>() {

        final Random random = new Random(123);
        final Iterator<Points> s = iterator();
        Points nextPoints = getNext();


        private Points getNext() {
          while (s.hasNext()) {
            Points points = s.next();
//            System.out.println(points);
            if (random.nextInt(rate) == 0) {
              return points;
            }
          }
          return null;
        }

        @Override
        public boolean hasNext() {
          return nextPoints != null;
        }

        @Override
        public Points next() {
          Points points = nextPoints;
          nextPoints = getNext();
          return points;
        }
      };
    } catch (Exception e) {
      e.printStackTrace();
    }

    return it;
  }

//  @Override
//  public void forEach(Consumer<? super Points> action) {
//
//  }
}
