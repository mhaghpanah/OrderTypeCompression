package OrderTypeCompression;

import OrderTypeCompression.Exp.RealizeLearnOrderQuadraticAlternative;
import OrderTypeCompression.MyFile.Address;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SandBox {

  public static void compress(int n) {
    Database database = Database.read(n);
    System.out.println();
//    byte[] ans = new byte[database.size() * Orientations.getByteSize(n)];
//    int index = 0;
//    for (int i = 0; i < database.size(); i++) {
//      Points points = database.get(i);
    BytesOrder bytesOrder = new BytesOrder();
    for (Points points : database) {
//      System.out.println(points);
      byte[] bytes = Orientations.bytesOrientations(points);
      bytesOrder.add(bytes);
//      System.out.println(Arrays.toString(bytes));
//      for (int j = 0; j < bytes.length; j++) {
//        ans[index++] = bytes[j];
//      }
    }
    bytesOrder.sort();
    byte[] ans = bytesOrder.compress();
    MyBinaryFileIO.write(String.format("compressed%02d_bo.out", n), ans);
  }


  public static int getCompressSize(int n) {
    int m = ((n * (n - 1) * (n - 2)) / 6 - ((n - 1) * (n - 2)) / 2 + 7) / 8;
    return m;
  }
  public static byte[] toCompressByte(Points points) {
    int n = points.size();
    int m = getCompressSize(n);
    byte[] bytes = new byte[m];
    int index = 0;
    boolean[][][] orderType = Orientations.getOrderType(points);

    System.out.println(Arrays.deepToString(Orientations.getOrientations(points)[0]));
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        for (int k = j + 1; k < n; k++) {
          if (i == 0) {
            assert orderType[i][j][k];
            continue;
          }
          if (!orderType[i][j][k]) {
//              System.out.printf("i: %d j: %d k: %d\n", i, j, k);
            bytes[index / 8] |= (1 << (index % 8));
          }
          index++;
        }
      }
    }
    assert (index + 7) / 8 == m;
    return bytes;
  }

  public static void doubleCompress(int n) {
    Database database = Database.read(n);
    System.out.println();

    BytesOrder bytesOrder = new BytesOrder();
//    byte[] ans = new byte[database.size() * getCompressSize(n)];
//    int index = 0;
//    for (int i = 0; i < database.size(); i++) {
//      Points points = database.get(i);
    for (Points points : database) {
//      System.out.println(points);
      points.sort(Point.YOrder());
      points.sortAroundFirstPoint();
//      points.reverseSortAroundFirstPoint();

      //      byte[] bytes = Orientations.bytesOrientations(points);
      byte[] bytes = toCompressByte(points);
      bytesOrder.add(bytes);

//      System.out.println(Arrays.toString(bytes));
//      for (int j = 0; j < bytes.length; j++) {
//        ans[index++] = bytes[j];
//      }
    }
    bytesOrder.sort();
    byte[] ans = bytesOrder.compress();
    MyBinaryFileIO.write(String.format("doubleCompressed%02d_bo.out", n), ans);
  }

  public static void realization(int n) {
    Database database = Database.read(n);

    PointsRealization solver = new RealizeRandomILP();
//    PointsRealization solver = new RealizeQuadraticAlternative();
//    PointsRealization solver = new RealizeQuadraticAlternativeStretch();
//    PointsRealization solver = new RealizeLearnOrderQuadraticAlternative();
//    PointsRealization solver = new RealizeWithHint();



    Map<Long, Integer> count = new HashMap<>();
    int c = 0;

    for (int i = 0; i < database.size(); i++) {
      Points points = database.get(i);

      System.out.println("----------------------------");
      Orientations orientations = new Orientations(points);
      Points ans = solver.solve(orientations);

      long max = Long.MIN_VALUE;
      if (ans != null) {
        c++;
        for (int j = 0; j < n; j++) {
          max = Math.max(max, ans.get(j).x());
        }
        System.out.println(ans);
        assert orientations.isSameOrderType(ans);
      }
      System.out.println();
      System.out.println("----------------------------");
      count.put(max, count.getOrDefault(max, 0) + 1);

    }

    System.out.println(count);
    System.out.printf("Count %d out of %d\n", c, database.size());
  }

  public static void sort(int n) {
    Database database = Database.read(n);

    PointsRealization solver = new RealizeQuadraticAlternative();

    int countWithoutSort = 0;
    int countAfterSort = 0;
    int countImprove = 0;
    int countSolved = 0;

    for (int i = 0; i < database.size(); i++) {
      Points points = database.get(i);
      Orientations orientations = new Orientations(points);

      Points pointsBeforeSort = solver.solve(orientations);

      System.out.println(points);

      points.sort();
      System.out.println(points);
      orientations = new Orientations(points);
      Points pointsAfterSort = solver.solve(orientations);

      if (pointsBeforeSort != null) countWithoutSort++;
      if (pointsAfterSort != null) countAfterSort++;
      if (pointsBeforeSort == null && pointsAfterSort != null) countImprove++;
      if (pointsBeforeSort != null || pointsAfterSort != null) countSolved++;

      System.out.println("----------------------------");
    }

    System.out.printf("Count solved without sort %d out of %d\n", countWithoutSort, database.size());
    System.out.printf("Count solved after sort %d out of %d\n", countAfterSort, database.size());
    System.out.printf("Count improved %d out of %d\n", countImprove, database.size());
    System.out.printf("Count solved %d out of %d\n", countSolved, database.size());

  }

  public static void savedUnsolvedId(int n) throws FileNotFoundException {
    Database database = Database.read(n);
//    PointsRealization solver = new RealizeQuadraticAlternative();
    PointsRealization solver = new RealizeWithHint();

    List<Integer> unsolved = new ArrayList<>();
    for (int i = 0; i < database.size(); i++) {
      Points points = database.get(i);
      points.sort();
      Orientations orientations = new Orientations(points);

      Points realizedPoints = solver.solve(orientations);

      if (realizedPoints == null) {
        unsolved.add(i);
      }
      System.out.println("----------------------------");
    }

    StringBuilder sb = new StringBuilder();
    sb.append(unsolved.size()).append('\n');
    for (int a : unsolved) sb.append(a).append('\n');
    MyFileWriter.write(String.format("sort_compress_unsolved_%d", n), sb.toString(), Address.RESULTS);
    File file = MyFile.getInstance(String.format("sort_compress_unsolved_%d", n), Address.RESULTS);
    Scanner scanner = new Scanner(file);
    int m = scanner.nextInt();
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < m; i++) {
      int t = scanner.nextInt();
      list.add(t);
    }

    System.out.println(unsolved);
    System.out.println(list);
    assert list.equals(unsolved);
  }

  public static void attackUnsolved(int n) throws FileNotFoundException {
    Database database = Database.read(n);
//    PointsRealization solver = new RealizeQuadraticAlternativeStretch();
    PointsRealization solver = new RealizeLearnOrderQuadraticAlternative();


    File file = MyFile.getInstance(String.format("compress_unsolved_%d", n), Address.RESULTS);
    Scanner scanner = new Scanner(file);
    int m = scanner.nextInt();
    List<Integer> unsolved = new ArrayList<>();
    for (int i = 0; i < m; i++) {
      int t = scanner.nextInt();
      unsolved.add(t);
    }

    List<Integer> list = new ArrayList<>();
    for (int id : unsolved) {
      Points points = database.get(id);
      points.sort();
      Orientations orientations = new Orientations(points);

      Points realizedPoints = solver.solve(orientations);

      if (realizedPoints == null) {
        list.add(id);
      }
      System.out.println("----------------------------");
    }

    System.out.println(unsolved);
    System.out.println(list);

    System.out.println(unsolved.size());
    System.out.println(list.size());

    System.out.printf("Solved %d out of %d\n", unsolved.size()- list.size(), unsolved.size());
  }

  public static void main(String[] args) throws FileNotFoundException {
    int n = 10;
//    compress(n);
    doubleCompress(n);
//    realization(n);
//    sort(n);
//    savedUnsolvedId(n);
//    attackUnsolved(n);
  }

}
