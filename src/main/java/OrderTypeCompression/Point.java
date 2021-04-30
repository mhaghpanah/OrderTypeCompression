package OrderTypeCompression;

import java.util.Comparator;

public class Point {

  private final long x;
  private final long y;

  public Point(long x, long y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point p) {
    x = p.x();
    y = p.y();
  }

  public long x() {
    return x;
  }

  public long y() {
    return y;
  }

  public static int ccw(Point a, Point b, Point c) {
    long area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
    if (area2 == 0) return 0;
    return area2 < 0 ? -1 : +1;
  }
  
  public int compareTo(Point that) {
    if (this.x() == that.x()) {
      return Long.compare(this.y(), that.y());
    }
    return Long.compare(this.x(), that.x());
  }

  public static Comparator<Point> XOrder() {
    return new Comparator<Point>() {
      @Override
      public int compare(Point p1, Point p2) {
        if (p1.x < p2.x) return -1;
        if (p1.x > p2.x) return +1;
        if (p1.y < p2.y) return -1;
        if (p1.y > p2.y) return +1;
        return 0;
      }
    };
  }

  public static Comparator<Point> YOrder() {
    return new Comparator<Point>() {
      @Override
      public int compare(Point p1, Point p2) {
        if (p1.y < p2.y) return -1;
        if (p1.y > p2.y) return +1;
        if (p1.x < p2.x) return -1;
        if (p1.x > p2.x) return +1;
        return 0;
      }
    };
  }

  public Comparator<Point> polarOrder() {
//    System.out.printf("x: %d y: %d\n", x, y);
    return (q1, q2) -> {
      double dx1 = q1.x - x;
      double dy1 = q1.y - y;
      double dx2 = q2.x - x;
      double dy2 = q2.y - y;

      if      (dy1 >= 0 && dy2 < 0) return -1;    // q1 above; q2 below
      else if (dy2 >= 0 && dy1 < 0) return +1;    // q1 below; q2 above
      else if (dy1 == 0 && dy2 == 0) {            // 3-collinear and horizontal
        if      (dx1 >= 0 && dx2 < 0) return -1;
        else if (dx2 >= 0 && dx1 < 0) return +1;
        else                          return  0;
      }
      else return -ccw(Point.this, q1, q2);     // both above or below

      // Note: ccw() recomputes dx1, dy1, dx2, and dy2
    };
  }

//  public Comparator<Point> reversePolarOrder() {
//    return new Comparator<Point>() {
//      Comparator<Point> comparator = polarOrder();
//      @Override
//      public int compare(Point q1, Point q2) {
//        return -comparator.compare(q1, q2);
//      }
//    };
//  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Point)) {
      return false;
    }

    Point point = (Point) o;

    if (x != point.x) {
      return false;
    }
    return y == point.y;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", x(), y());
  }


//  public static void main(String[] args) {
//    Random random = new Random();
//    int try_number = 1_000_000;
//    while (try_number-- > 0) {
//      Point a = new Point(random.nextInt(), random.nextInt());
//      Point b = new Point(random.nextInt(), random.nextInt());
//      Point c = new Point(random.nextInt(), random.nextInt());
////      int ccw1 = a.orientation(a, b, c);
//      int ccw2 = ccw(a, b, c);
////      assert ccw1 == ccw2;
//    }
//  }

}
