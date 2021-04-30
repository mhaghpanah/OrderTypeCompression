package OrderTypeCompression.Exp;

import OrderTypeCompression.Database;
import OrderTypeCompression.MyBinaryFileIO;
import OrderTypeCompression.MyFile;
import OrderTypeCompression.MyFile.Address;
import OrderTypeCompression.MyFileWriter;
import OrderTypeCompression.Orientations;
import OrderTypeCompression.Points;
import OrderTypeCompression.PointsRealization;
import OrderTypeCompression.RealizeQuadraticAlternativeStretch2E;
import OrderTypeCompression.RealizeWithHint2;
import OrderTypeCompression.Timer;
import OrderTypeGraph.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class Phase0 {

  public static Pair<Boolean, Long> solveAndTime(Orientations orientations, PointsRealization[] solvers) {
    Timer timer = new Timer();

    Points points = null;
    for (int j = 0; j < solvers.length && points == null; j++) {
      points = solvers[j].solve(orientations);
    }

    long t = timer.duration();
    System.out.printf("Solved in %d microseconds time!\n", t);

    if (points == null) {
      return Pair.getInstance(Boolean.FALSE, t);
    } else {
      assert orientations.isSameOrderType(points);
      return Pair.getInstance(Boolean.TRUE, t);
    }
  }

  public static Pair<Boolean, Long> solveAndTime(Orientations orientations, PointsRealization solver) {
    Timer timer = new Timer();

    Points points = solver.solve(orientations);

    long t = timer.duration();
    System.out.printf("Solved in %d microseconds time!\n", t);

    if (points == null) {
      return Pair.getInstance(Boolean.FALSE, t);
    } else {
      assert orientations.isSameOrderType(points);
      return Pair.getInstance(Boolean.TRUE, t);
    }
  }


  public static void summary(Map<Long, Integer> time, int n) {
    System.out.println(time);
    long countSolved = 0;
    long timeSolved = 0;
    long countTotal = 0;
    long timeTotal = 0;
    for (Map.Entry<Long, Integer> e : time.entrySet()) {
      countTotal += e.getValue();
      timeTotal += e.getKey() * e.getValue();
      if (e.getKey() >= 0) {
        countSolved += e.getValue();
        timeSolved += e.getKey() * e.getValue();
      }
    }

    double avgSolvedTime = (double)timeSolved / countSolved;
    double avgSolvedCount = (countSolved * 100.0) / countTotal;

    String str = String.format("N: %d ", n)
        + String.format("AvgTime: %.2f micro sec. ", avgSolvedTime)
        + String.format("Solved: %d/%d ", countSolved, countTotal)
        + String.format("AvgSolved: %.2f %% ", avgSolvedCount);
    System.out.println(str);
  }

  //  public static Coordinate[] clusterWrapper(Database database, int k) {
  public static Coordinate[] clusterWrapper(Iterator<Points> it, int k) {

//      Coordinate[] coordinates = new Coordinate[database.size()];
    List<Coordinate> coordinateList = new ArrayList<>();
    int index = 0;
//    for (Points points : database) {
    while (it.hasNext()) {
      Points points = it.next();
      points.sort();
//      coordinates[index++] = new Coordinate(points);
      coordinateList.add(new Coordinate(points));
    }

    Coordinate[] coordinates = coordinateList.toArray(new Coordinate[coordinateList.size()]);
    return Cluster.solve(k, coordinates);
  }

  public static void firstPhaseCluster(int n, int k) {
    Database database = Database.read(n);

    PointsRealization[] pointsRealizations = new PointsRealization[k];
    pointsRealizations[0] = new RealizeWithHint2(n);

    int rate = 20;
    if (k > 1) {
//      Coordinate[] coordinates = clusterWrapper(database, k - 1);
      Coordinate[] coordinates = clusterWrapper(database.sampleIterator(rate), k - 1);

      System.out.println(Arrays.toString(coordinates));

      for (int i = 0; i < k - 1; i++) {
        pointsRealizations[i + 1] = new RealizeWithHint2(n, coordinates[i].getIntArray());
      }
    }


    Map<Long, Integer> time = new TreeMap<>();
    byte[] ans = new byte[Database.databaseSize(n) * Orientations.getByteSize(n)];
    int index = 0;
//    for (Points points : database) {
    int[] count = new int[k];
    for (Iterator<Points> it = database.sampleIterator(rate); it.hasNext(); ) {
      Points points = it.next();

      System.out.printf("%d ----------------------------\n", index);
      System.out.println(points);
//      points.sort();
//      points.reverse();
      Orientations orientations = new Orientations(points);
//      Pair<Orientations, Long> permutedPoints = permuteAndSolve(points, pointsRealizations);
//      Orientations orientations = permutedPoints.getKey();
      boolean solved = false;
      for (int i = 0; i < k; i++) {
        Pair<Boolean, Long> pair = solveAndTime(orientations, pointsRealizations[i]);
        long t = pair.getValue();
        if (pair.getKey()) {
          time.put(t, time.getOrDefault(t, 0) + 1);
          count[i]++;
          solved = true;
          break;
        }
      }
      if (!solved) {
        time.put(-1L, time.getOrDefault(-1L, 0) + 1);
      }

      byte[] bytes;
      if (solved) {
        bytes = orientations.toByte();
      } else {
        bytes = new byte[Orientations.getByteSize(n)];
        Arrays.fill(bytes, (byte) 0b11111111);
      }
      System.out.println(Arrays.toString(bytes));

      System.arraycopy(bytes, 0, ans, index * Orientations.getByteSize(n), bytes.length);
      index++;
    }

    System.out.printf("Solution distribution: %s\n", Arrays.toString(count));
    summary(time, n);
    MyBinaryFileIO.write(String.format("compressed_rotated_%02d.out", n), ans);
  }

  public static void firstPhase(int n) {
    Database database = Database.read(n);

    PointsRealization[] pointsRealizations = new PointsRealization[1];
//    pointsRealizations[0] = new RealizeWithHint();
    pointsRealizations[0] = new RealizeWithHint2(n);
//    pointsRealizations[0] = new RealizeQuadraticAlternative(2);
//    pointsRealizations[0] = new RealizeQuadraticAlternativeStretch(1000);
//    pointsRealizations[0] = new RealizeQuadraticAlternativeStretch2(20);
//    pointsRealizations[0] = new RealizeQuadraticAlternativeStretch2E(20);
//    pointsRealizations[2] = new RealizeRandomILP(10_000);
//    pointsRealizations[0]= new RealizeWithHint2Simplex(n);

    Map<Long, Integer> time = new TreeMap<>();
    List<Integer> unsolved = new ArrayList<>();

    byte[] ans = new byte[Database.databaseSize(n) * Orientations.getByteSize(n)];
    int i = 0;
    Set<Integer> set = new HashSet<>();
    for (Points points : database) {
      System.out.printf("%d ----------------------------\n", i);
      System.out.println(points);
//      points.sort();
//      points.reverse();
      Orientations orientations = new Orientations(points);
      Pair<Boolean, Long> pair = solveAndTime(orientations, pointsRealizations);
      long t = pair.getValue();

//      long t = permutedPoints.getValue();
//      if (!pair.getKey()) {
//        points.sort();
//        orientations = new Orientations(points);
//        pair = solveAndTime(orientations, pointsRealizations);
//        t += pair.getValue();
////        t = solveAndTime(orientations, pointsRealizations);
//
//        if (!pair.getKey()) {
//          points.reverse();
//          orientations = new Orientations(points);
//          pair = solveAndTime(orientations, pointsRealizations);
//          t += pair.getValue();
////          t = solveAndTime(orientations, pointsRealizations);
//        }
//      }


      if (pair.getKey()) {
        time.put(t, time.getOrDefault(t, 0) + 1);
        set.add(i);
      }

      if (!pair.getKey()) {
        time.put(-1L, time.getOrDefault(-1L, 0) + 1);
        unsolved.add(i);
      }

      byte[] bytes;
      if (t >= 0) {
        bytes = orientations.toByte();
      } else {
        bytes = new byte[Orientations.getByteSize(n)];
        Arrays.fill(bytes, (byte) 0b11111111);
      }
      System.out.println(Arrays.toString(bytes));

      System.arraycopy(bytes, 0, ans, i * Orientations.getByteSize(n), bytes.length);
      i++;
      summary(time, n);

    }

//    i = 0;
//    int j = 0;
//    int b = 2;
//    Orientations[] orientationsArr = new Orientations[b];
//    Map<Long, Integer> timeBatch = new TreeMap<>();
//
//    RealizeWithHint2Batch realizeWithHint2Batch = new RealizeWithHint2Batch(n, b);
//    for (Points points : database) {
//      System.out.printf("%d ----------------------------\n", i);
//      if (set.contains(i)) {
//
//        Orientations orientations = new Orientations(points);
//        orientationsArr[j++] = orientations;
//        if (j == b) {
//          j = 0;
//          Timer timer = new Timer();
//          Points[] pointsArr = realizeWithHint2Batch.solve(orientationsArr);
//          long t = timer.duration();
//          if (pointsArr == null) {
//            throw null;
//          } else {
//            for (int l = 0; l < b; l++) {
//              long tt = t / b + (t % b < l ? 1 : 0);
//              timeBatch.put(tt, timeBatch.getOrDefault(tt, 0) + 1);
//            }
//          }
//
//        }
//      }
//      i++;
//    }
//
//    if (j > 0) {
//      int tmp = j;
//      while (tmp < b) {
//        orientationsArr[tmp++] = orientationsArr[0];
//      }
//
//      Timer timer = new Timer();
//      Points[] pointsArr = realizeWithHint2Batch.solve(orientationsArr);
//      long t = timer.duration();
//      if (pointsArr == null) {
//        throw null;
//      } else {
//        for (int l = 0; l < j; l++) {
//          long tt = t / j + (t % j < l ? 1 : 0);
//          timeBatch.put(tt, timeBatch.getOrDefault(tt, 0) + 1);
//        }
//      }
//
//
//    }




    summary(time, n);
//    summary(timeBatch, n);

    StringBuilder sb = new StringBuilder();
    sb.append(unsolved.size()).append('\n');
    for (int a : unsolved) sb.append(a).append('\n');
    MyFileWriter.write(String.format("unsolved_phase0_%02d", n), sb.toString(), Address.RESULTS);

    MyBinaryFileIO.write(String.format("compressed_phase0_%02d.out", n), ans);
  }

  public static void secondPhase(int n) throws FileNotFoundException {
    Database database = Database.read(n);

    PointsRealization[] pointsRealizations = new PointsRealization[1];
//    pointsRealizations[0] = new RealizeWithHint();
//    pointsRealizations[0] = new RealizeWithHint2(n);
//    pointsRealizations[0] = new RealizeQuadraticAlternative(2);
//    pointsRealizations[0] = new RealizeQuadraticAlternativeStretch(1000);
//    pointsRealizations[0] = new RealizeQuadraticAlternativeStretch2(20);
    pointsRealizations[0] = new RealizeQuadraticAlternativeStretch2E(20);
//    pointsRealizations[2] = new RealizeRandomILP(10_000);
//    pointsRealizations[0]= new RealizeWithHint2Simplex(n);

    Map<Long, Integer> time = new TreeMap<>();

    Set<Integer> unsolvedPhase1 = new HashSet<>();

    File file = MyFile.getInstance(String.format("unsolved_phase0_%02d", n), Address.RESULTS);
    Scanner scanner = new Scanner(file);
    int m = scanner.nextInt();
    for (int i = 0; i < m; i++) {
      int t = scanner.nextInt();
      unsolvedPhase1.add(t);
    }

    int i = 0;

    List<Integer> unsolved = new ArrayList<>();

    for (Points points : database) {
      System.out.printf("%d ----------------------------\n", i);
      System.out.println(points);

      if (unsolvedPhase1.contains(i)) {
//      points.sort();
//      points.reverse();
        Orientations orientations = new Orientations(points);
        Pair<Boolean, Long> pair = solveAndTime(orientations, pointsRealizations);
        long t = pair.getValue();

//      long t = permutedPoints.getValue();
//      if (!pair.getKey()) {
//        points.sort();
//        orientations = new Orientations(points);
//        pair = solveAndTime(orientations, pointsRealizations);
//        t += pair.getValue();
////        t = solveAndTime(orientations, pointsRealizations);
//
//        if (!pair.getKey()) {
//          points.reverse();
//          orientations = new Orientations(points);
//          pair = solveAndTime(orientations, pointsRealizations);
//          t += pair.getValue();
////          t = solveAndTime(orientations, pointsRealizations);
//        }
//      }

        if (pair.getKey()) {
          time.put(t, time.getOrDefault(t, 0) + 1);
        }

        if (!pair.getKey()) {
          time.put(-1L, time.getOrDefault(-1L, 0) + 1);
          unsolved.add(i);
        }

        byte[] bytes;
        if (t >= 0) {
          bytes = orientations.toByte();
        } else {
          bytes = new byte[Orientations.getByteSize(n)];
          Arrays.fill(bytes, (byte) 0b11111111);
        }
        System.out.println(Arrays.toString(bytes));

//        System.arraycopy(bytes, 0, ans, i * Orientations.getByteSize(n), bytes.length);
        summary(time, n);

      }
      i++;

    }

    summary(time, n);
//    summary(timeBatch, n);

    StringBuilder sb = new StringBuilder();
    sb.append(unsolved.size()).append('\n');
    for (int a : unsolved) sb.append(a).append('\n');
    MyFileWriter.write(String.format("unsolved_phase1_%02d", n), sb.toString(), Address.RESULTS);

//    MyBinaryFileIO.write(String.format("compressed_phase0_%02d.out", n), ans);
  }

  public static void main(String[] args) throws FileNotFoundException {
    Timer timer = new Timer();
    int n = 10;
    int k = (1 << 16);

//    firstPhaseCluster(n, k);
    firstPhase(n);
    secondPhase(n);


    System.out.printf("Program takes %d micro sec.", timer.duration());
  }

}
