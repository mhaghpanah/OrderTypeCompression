package OrderTypeCompression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SandBox {

  public static void exp(int n) {
    Database database = Database.read(n);
    System.out.println();
    byte[] ans = new byte[database.size() * Orientations.getByteSize(n)];
    int index = 0;
    for (int i = 0; i < database.size(); i++) {
      Points points = database.get(i);
//      System.out.println(points);
      byte[] bytes = Orientations.bytesOrientations(points);
      System.out.println(Arrays.toString(bytes));
      for (int j = 0; j < bytes.length; j++) {
        ans[index++] = bytes[j];
      }
    }

    MyBinaryFileIO.write(String.format("compressed%02d.out", n), ans);
  }

  public static void realization(int n) {
    Database database = Database.read(n);

    Map<Long, Integer> count = new HashMap<>();
    int c = 0;
    for (int i = 0; i < database.size(); i++) {
      Points points = database.get(i);

      System.out.println("----------------------------");
      PointsRealization solver1 = new RandomILP();
      PointsRealization solver2 = new QuadraticAlternative();
      Orientations orientations = new Orientations(points);
      Points points1 = solver1.solve(orientations);
      Points points2 = solver2.solve(orientations);
      boolean solved = points1 != null;

      long max = Long.MIN_VALUE;
      if (solved) {
        c++;
        Points ans = points1;
        for (int j = 0; j < n; j++) {
          max = Math.max(max, ans.get(j).getX());
          System.out.printf("(%d %d) \t", ans.get(j).getX(), ans.get(j).getX());
        }
      }
      System.out.println();
      System.out.println("----------------------------");
      count.put(max, count.getOrDefault(max, 0) + 1);

    }

    System.out.println(count);
    System.out.printf("Count %d out of %d\n", c, database.size());
  }

  public static void main(String[] args) {
    int n = 7;
//    exp(n);
    realization(n);

  }

}
