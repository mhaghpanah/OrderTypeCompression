package OrderTypeCompression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
//          if (Points.orientation(p, points.get(i), points.get(j)) == 0) {
          if (Point.ccw(p, points.get(i), points.get(j)) == 0) {
              continue search;
          }
        }
      }
      points.add(p);
    }
    return points;
  }

//  public static long crossProduct(Point p1, Point p2) {
//    return p1.x() * p2.y() - p1.y() * p2.x();
//  }

//  public static int orientation(Point p1, Point p2, Point p3) {
//    Point v1 = new Point(p1.x() - p2.x(), p1.y() - p2.y());
//    Point v2 = new Point(p2.x() - p3.x(), p2.y() - p3.y());
//    long ans1 = crossProduct(v1, v2);
//    long ans2 = crossProduct(p1, p2) - crossProduct(p1, p3) + crossProduct(p2, p3);
//    long ans3 =
//        (p2.x() - p1.x()) * (p3.y() - p1.y()) - (p3.x() - p1.x()) * (p2.y()
//            - p1.y());
//    assert ans1 == ans2;
//    assert ans1 == ans3;
//    assert sign(ans1) == Point.ccw(p1, p2, p3);
//    return sign(ans1);
//  }


//  private static int sign(long v) {
//    return (int) (v == 0 ? v : v > 0 ? +1 : -1);
//  }

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

  public Point min() {
    return Collections.min(points, Point::compareTo);
  }

  public void sort() {
    points.sort(Point::compareTo);
  }

  public void sort(Comparator<Point> comparator) {
    points.sort(comparator);
  }

//  public void sort(Comparator<Point> comparator) { points.sort(comparator); }

  public void sortAroundFirstPoint() {
    Point p0 = points.get(0);
    Comparator<Point> comparator = p0.polarOrder();
    points.subList(1, points.size()).sort(comparator);
  }

//  public void reverseSortAroundFirstPoint() {
//    Point p0 = points.get(0);
//    Comparator<Point> comparator = p0.reversePolarOrder();
//    points.subList(1, points.size()).sort(comparator);
//  }

  public void reverse() { Collections.reverse(points); }

  public void shuffle() { Collections.shuffle(points); }

  public void shuffle(Random random) { Collections.shuffle(points, random); }

  public boolean isSorted() {
    for (int i = 0; i + 1 < points.size(); i++) {
      if (points.get(i).compareTo(points.get(i + 1)) > 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Points)) {
      return false;
    }

    Points points1 = (Points) o;

    return points.equals(points1.points);
  }

  @Override
  public int hashCode() {
    return points.hashCode();
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

  public static void main(String[] args) {
    int n = 9;
    int id = 10;
    Database database = Database.read(n);
    Points points = database.get(id);
    System.out.println(points);
    System.out.println(points.min());
    points.sort();
    System.out.println(points);
    System.out.println(points.min());

    points = new Points();
    n = 3;
    points.add(new Point(0, 0));
    for (int i = 1; i <= n; i++) {
      points.add(new Point(i, i * i));
//      points.add(new Point(-i, i * i));
      points.add(new Point(i, -i * i));
//      points.add(new Point(-i, -i * i));
    }
    System.out.println(points);
    points.shuffle();
    System.out.println(points);
    System.out.println(points.min());
    points.sort();
    System.out.println(points);
    System.out.println(points.min());

    System.out.println("================");
    points.shuffle();
    System.out.println(points);
    points.sort();
    System.out.println(points);
    points.sortAroundFirstPoint();
    System.out.println(points);
  }

}
