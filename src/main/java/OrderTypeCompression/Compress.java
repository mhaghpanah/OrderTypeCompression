package OrderTypeCompression;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class Compress {

  public static void helper(int n) throws IOException {
    String filename = String.format("otypes%02d.b%02d", n, Database.databaseBitSize(n));
    String pathname = String.join(File.separator,
        System.getProperty("user.dir"), "data", filename);

    InputStream in = new FileInputStream(pathname);
    OutputStream out = new GZIPOutputStream(
        new BufferedOutputStream(new FileOutputStream("test.gz")));

//    byte[] bytes = new byte[32*1024];

    int len;

    int index = 0;
//    Database database = Database.read(n);

    int k = (2 * n * Database.databaseBitSize(n) + 7)/8;
    ReadKBytes readKBytes = new ReadKBytes(k, in);
    byte[] inputBytes = new byte[k];

    while((len = readKBytes.read(inputBytes)) > 0) {
//      System.out.println(len);
//      Points pointsVerify = database.get(index++);
      Points points = Database.interpretBitsByteArray(inputBytes, n);

//      System.out.println(index);
//      System.out.println(points);
//      System.out.println(pointsVerify);

//      assert points.equals(pointsVerify);

      byte[] outputBytes = Orientations.bytesOrientations(points);
//      System.out.println(len);
//      System.out.println(Arrays.toString(outputBytes));

      out.write(outputBytes, 0, outputBytes.length);
//      System.out.println(len);

    }

    in.close();
    out.close();
  }

  public static void check(int n) {

  }

  public static void main(String[] args) throws IOException {
    Instant start;
    Instant finish;

    start = Instant.now();

    int n = 10;
    helper(n);

    finish = Instant.now();
    long t = TimeUnit.NANOSECONDS.toMicros(Duration.between(start, finish).getNano());
    System.out.printf("Compressed Solved in %d microseconds time!\n", t);

  }

}
