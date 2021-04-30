//package OrderTypeCompression.Exp;
//
//import OrderTypeCompression.Database;
//import OrderTypeCompression.MyBinaryFileIO;
//import OrderTypeCompression.MyFile;
//import OrderTypeCompression.MyFile.Address;
//import OrderTypeCompression.Orientations;
//import OrderTypeCompression.Points;
//import OrderTypeCompression.PointsRealization;
//import OrderTypeCompression.ReadKBytes;
//import OrderTypeCompression.RealizeWithHint2;
//import OrderTypeCompression.Timer;
//import OrderTypeGraph.Pair;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.TreeMap;
//
//public class EXP {
//
//
//  public static Pair<Orientations, Long> permuteAndSolve(Points points, PointsRealization[] solvers) {
//    Orientations orientations = new Orientations(points);
//    points.sort();
//    Orientations sortedOrientations = new Orientations(points);
//
//    long threshold = 200;
//    int TRY_NUM = 1_0;
//    long t;
//    for (long i = threshold; i < 5000; i *= 3) {
//      Pair<Boolean, Long> pair = solveAndTime(orientations, solvers);
//      t = pair.getValue();
//      if (0 <= t && t <= threshold) {
//        return Pair.getInstance(orientations, t);
//      }
//      pair = solveAndTime(orientations, solvers);
//      t = pair.getValue();
//      if (0 <= t && t <= threshold) {
//        return Pair.getInstance(sortedOrientations, t);
//      }
//
//      for (int j = 0; j < TRY_NUM; j++) {
//        points.shuffle();
//        Orientations randomOrientations = new Orientations(points);
//        pair = solveAndTime(orientations, solvers);
//        t = pair.getValue();
//        if (0 <= t && t <= threshold) {
//          return Pair.getInstance(randomOrientations, t);
//        }
//      }
//    }
//
//    return Pair.getInstance(orientations, -1L);
//  }
//
//  public static void secondPhase(int n) throws IOException {
//
//    File file = MyFile.getInstance(String.format("compressed_rotated_%02d.out", n), Address.RESULTS);
//    InputStream in = new FileInputStream(file);
//    int k = Orientations.getByteSize(n);
//    ReadKBytes readKBytes = new ReadKBytes(k, in);
//    byte[] failedBytes = new byte[k];
//    Arrays.fill(failedBytes, (byte) 0b11111111);
//
//    byte[] buf = new byte[k];
//    int count = 0 ;
//    for (int i = 0; i < Database.databaseSize(n); i++) {
//      readKBytes.read(buf);
//      System.out.println(Arrays.toString(buf));
//      if (Arrays.equals(failedBytes, buf)) {
//        count++;
//      } else {
//
//      }
//    }
//    System.out.println(count);
//
//
//  }
//
//  public static void main(String[] args) {
//    int n = 9;
//    int k = 1;
//
////    f(n);
////    firstPhaseCluster(n, k);
////    firstPhase(n);
////    secondPhase(n);
//  }
//
//}
