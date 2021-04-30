package OrderTypeCompression.Exp;

import OrderTypeCompression.Database;
import OrderTypeCompression.DoubleEpsilonCompare;
import OrderTypeCompression.Points;
import java.util.Arrays;
import java.util.Random;

public class Cluster {

  Coordinate[] centroids;
  int TRY_NUM = 1_00;
  int n;
  int d;
  int k;
  Coordinate[] coordinates;
  int[] closestCentroid;
  int[] count;

  Random random;

  public Cluster(int k, Coordinate[] coordinates) {
    this.k = k;
    this.coordinates = coordinates;

    n = coordinates.length;
    d = coordinates[0].dimension();

    centroids = new Coordinate[k];
    closestCentroid = new int[n];
    count = new int[k];

    random = new Random();
  }

  public Coordinate[] solve(int TRY_NUM) {

    for (int i = 0; i < k; i++) {
      centroids[i] = Coordinate.randomCoordinateGenerator(d, random);
    }

    for (int i = 0; i < TRY_NUM; i++) {
      System.out.printf("Round %d Centroids %s\n", i, Arrays.toString(centroids));

      if (assignCentroid()) {
        recomputeCentroids();
      } else {
        break;
      }
    }
    System.out.printf("Centroids %s\n", Arrays.toString(centroids));

    return centroids;
  }

  private boolean assignCentroid() {
    boolean changeFlag = false;
    Arrays.fill(count, 0);
    for (int i = 0; i < n; i++) {
      int c = coordinates[i].closetCoordinate(centroids);
      count[c]++;
      if (closestCentroid[i] != c) {
        closestCentroid[i] = c;
        changeFlag = true;
      }
    }

    return changeFlag;
  }

  public void recomputeCentroids() {

    for (int i = 0; i < k; i++) {
      centroids[i].clear();
    }

    for (int i = 0; i < n; i++) {
      int closestId = closestCentroid[i];
      centroids[closestId]
          .add(coordinates[i]);
    }

    for (int i = 0; i < k; i++) {
      if (count[i] > 0) {
        centroids[i].scalarDivision(count[i]);
//        centroids[i].integrality();
      }
    }

    System.out.println(Arrays.toString(count));
  }


  public static Coordinate[] solve(int k, Coordinate[] coordinates) {
    Cluster cluster = new Cluster(k, coordinates);
    return cluster.solve(1000);
  }

  public static void main(String[] args) {

    int n = 10;
    Database database = Database.read(n);
    Coordinate[] coordinates = new Coordinate[database.size()];
    int index = 0;
    for (Points points : database) {
      points.sort();
      coordinates[index++] = new Coordinate(points);
    }

    int k = 32;
    Coordinate[] centroids = Cluster.solve(k, coordinates);

  }

}



class Coordinate {
  int d;
  double[] c;

  public Coordinate(int d) {
    this.d = d;
    c = new double[d];
  }

  public Coordinate(double[] c) {
    this.d = c.length;
    this.c = c;
  }

  public Coordinate(Points points) {
    this(points.size());
    for (int i = 0; i < d; i++) {
      c[i] = points.get(i).x();
    }
  }

  public int dimension() {
    return d;
  }

  public double get(int index) {
    return c[index];
  }

  public double[] getDoubleArray() {
    return c;
  }

  public int[] getIntArray() {
    int[] arr = new int[d];
    for (int i = 0; i < d; i++) {
      arr[i] = DoubleEpsilonCompare.integrality(c[i]);
    }
    return arr;
  }

  public void clear() {
    Arrays.fill(c, 0.0);
  }

  public static Coordinate randomCoordinateGenerator(int d) {
    Random random = new Random();
    int bound = 1 << 16;
    return randomCoordinateGenerator(d, random, bound);
  }

  public static Coordinate randomCoordinateGenerator(int d, Random random) {
    int bound = (1 << 16) - 1;
    return randomCoordinateGenerator(d, random, bound);
  }

  public static Coordinate randomCoordinateGenerator(int d, Random random, int bound) {
    double[] c = new double[d];
    for (int i = 0; i < d; i++) {
      int r = random.nextInt(bound);
      c[i] = r;
    }
    Arrays.sort(c);

    return new Coordinate(c);
  }

  public void add(Coordinate other) {
    for (int i = 0; i < d; i++) {
      c[i] += other.c[i];
    }
  }

  public void scalarDivision(double s) {
    for (int i = 0; i < d; i++) {
      c[i] /= s;
    }
  }

  public void integrality() {
    for (int i = 0; i < d; i++) {
      c[i] = DoubleEpsilonCompare.integrality(c[i]);
    }
  }

  public double dist2(Coordinate other) {
    double ans = 0.0;
    for (int i = 0; i < d; i++) {
      ans += pow2(this.c[i] - other.c[i]);
    }
    return ans;
  }

  public int closetCoordinate(Coordinate[] coordinates) {
    int ans = -1;
    double d = 0.0;
    for (int i = 0; i < coordinates.length; i++) {
      double d2 = dist2(coordinates[i]);
      if (ans == -1 || d > d2) {
        ans = i;
        d = d2;
      }
    }
    return ans;
  }

  private double pow2(double a) {
    return a * a;
  }

  @Override
  public String toString() {
    return "Coordinate{" +
        "d=" + d +
        ", c=" + Arrays.toString(c) +
        '}';
  }
}