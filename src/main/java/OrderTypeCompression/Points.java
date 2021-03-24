package OrderTypeCompression;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Points {

  private final List<Point> points;

  public Points() {
    points = new ArrayList<>();
  }

  public Points(List<Point> points) {
    this();
    for (Point p : points) {
      this.points.add(new Point(p));
    }
  }

  public Points(long[] x, long[] y) {
    this();
    assert x.length == y.length;
    for (int i = 0; i < x.length; i++) {
      this.points.add(new Point(x[i], y[i]));
    }
  }

  public Points(Scanner in) {
    this();
    int n = in.nextInt();
    for (int i = 0; i < n; i++) {
      long px = in.nextLong();
      long py = in.nextLong();
      Point p = new Point(px, py);
      add(p);
    }
  }

  public static Points randomPointGenerator(int n) {
    Random random = new Random();
    return randomPointGenerator(n, random);
  }

  public static Points randomPointGenerator(int n, Random random) {
    int bound = 10_000;
    return randomPointGenerator(n, random, bound);
  }

  public static Points randomPointGenerator(int n, Random random, int bound) {
    Points points = new Points();
    search:
    while (points.size() < n) {
      long x = Math.abs(random.nextInt(bound));
      long y = Math.abs(random.nextInt(bound));
      Point p = new Point(x, y);
      for (int i = 0; i < points.size(); i++) {
        for (int j = 0; j < i; j++) {
          if (Points.orientation(p, points.get(i), points.get(j)) == 0) {
            continue search;
          }
        }
      }
      points.add(p);
    }
    return points;
  }

  public static long crossProduct(Point p1, Point p2) {
    return p1.getX() * p2.getY() - p1.getY() * p2.getX();
  }

  public static int orientation(Point p1, Point p2, Point p3) {
    Point v1 = new Point(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    Point v2 = new Point(p2.getX() - p3.getX(), p2.getY() - p3.getY());
    long ans1 = crossProduct(v1, v2);
    long ans2 = crossProduct(p1, p2) - crossProduct(p1, p3) + crossProduct(p2, p3);
    long ans3 =
        (p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) - (p3.getX() - p1.getX()) * (p2.getY()
            - p1.getY());
    assert ans1 == ans2;
    assert ans1 == ans3;
    return sign(ans1);
  }

  private static int sign(long v) {
    return (int) (v == 0 ? v : v > 0 ? +1 : -1);
  }

  public Point get(int i) {
    return points.get(i);
  }

  public int size() {
    return points.size();
  }

  public boolean add(Point p) {
    points.add(p);
    return true;
  }

  public void sort() {
    points.sort(Point::compareTo);
  }

  public String toFileFormatString() {
    StringBuilder sb = new StringBuilder();
    sb.append(size());
    sb.append("\n");
    for (Point p : points) {
      sb.append(p.toFileFormatString());
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (Point p : points) {
      if (sb.length() > 1) {
        sb.append(", ");
      }
      sb.append(p);
    }
    sb.append("}");
    return sb.toString();
  }

//  public static void main(String[] args) {
//    int n = 9;
//    int id = 10;
//    Database database = Database.read(n);
//    Points points = database.get(id);
//    System.out.println(points);
//    points.sort();
//    System.out.println(points);
//  }

}
