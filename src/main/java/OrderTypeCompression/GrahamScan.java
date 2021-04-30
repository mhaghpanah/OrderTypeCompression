package OrderTypeCompression;

import java.util.Arrays;
import java.util.Stack;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public class GrahamScan {

  Stack<Point> hull;
  public GrahamScan(Points points) {
    hull = new Stack<>();
    int n = points.size();
    Point[] a = new Point[n];
    for (int i = 0; i < n; i++) {
      a[i] = points.get(i);
    }
//    System.out.println("----------------");
//    System.out.printf("xsorted points: %s \n", Arrays.toString(a));
    Arrays.sort(a, Point.YOrder());
//    System.out.printf("-sorted points: %s \n", Arrays.toString(a));
    Arrays.sort(a, 1, n, a[0].polarOrder());
//    System.out.printf("sorted around first point: %s \n", Arrays.toString(a));


    hull.push(a[0]);

    int k1;
    for (k1 = 1; k1 < n; k1++) {
      if (!a[0].equals(points.get(k1))) {
        break;
      }
    }
    if (k1 == n) return;
//    System.out.printf("k1 %d\n", k1);

    int k2 = k1 + 1;
    for (; k2 < n; k2++) {
      if (Point.ccw(a[0], points.get(k1), points.get(k2)) != 0) {
        break;
      }
    }
//    System.out.printf("k2 %d\n", k2);
    hull.push(a[k2 - 1]);


    for (int i = k2; i < n; i++) {
      Point top = hull.pop();
      while (Point.ccw(hull.peek(), top, a[i]) <= 0) {
        top = hull.pop();
      }
      hull.push(top);
      hull.push(a[i]);
    }

    assert isConvex();

  }

  public Points hull() {
    Points ans = new Points();
    for (Point point : hull) ans.add(point);
    return ans;
  }

  private boolean isConvex() {
    int h = hull.size();
    if (h <= 2) return true;
    Points points = hull();
//    System.out.println(points);
    for (int i = 0; i < h; i++) {
      Point p = points.get(i);
      Point q = points.get((i + 1) % h);
      Point r = points.get((i + 2) % h);
      if (Point.ccw(p, q, r) <= 0) {
        return false;
      }
    }
    return true;
  }

  public static Points convexHull(Points points) {
    PrecisionModel precisionModel = new PrecisionModel();
    GeometryFactory geometryFactory = new GeometryFactory(precisionModel);

    Coordinate[] coordinates = new Coordinate[points.size()];
    for (int i = 0; i < points.size(); i++) {
      Point point = points.get(i);
      coordinates[i] = new Coordinate(point.x(), point.y());
    }

    Points convexHull = new Points();
    Geometry geometry = (new ConvexHull(coordinates, geometryFactory)).getConvexHull();
    Coordinate[] convexHullCoordinates = geometry.getCoordinates();
    for (int i = 0; i < convexHullCoordinates.length - 1; i++) {
      Coordinate coordinate = convexHullCoordinates[i];
      Point point = new Point((long)coordinate.getX(), (long)coordinate.getY());
      convexHull.add(point);
    }
    int minId = 0;
    for (int i = 0; i < convexHull.size(); i++) {
      if (convexHull.get(i).compareTo(convexHull.get(minId)) < 0) {
        minId = i;
      }
    }
    return convexHull;

    //    Points orderedConvexHull = new Points();
//    for (int i = 0; i < convexHull.size(); i++) {
//      orderedConvexHull.add(convexHull.get((i + minId) % convexHull.size()));
//    }
////    convexHull.sort(Point.XOrder());
//    return orderedConvexHull;
  }

  public static void main(String[] args) {
    int n = 10;
    Database database = Database.read(n);
    int id = 0;
    for (Points points : database) {
//      if (id == 6 || id == 8) continue;
//      id++;
      System.out.println("--------------------------");
      System.out.printf("Points: %s\n", points);

      Points convexHull0 = (new GrahamScan(points)).hull();
      convexHull0.sort(Point.XOrder());
      convexHull0.sortAroundFirstPoint();
      System.out.printf("CH0: %s\n", convexHull0);


      Points convexHull1 = convexHull(points);
      convexHull1.sort(Point.XOrder());
      convexHull1.sortAroundFirstPoint();
      System.out.printf("CH1: %s\n", convexHull1);


      assert convexHull0.equals(convexHull1);
    }



    Points points = new Points();
    n = 3;
    points.add(new Point(0, 0));
    for (int i = 1; i <= n; i++) {
      points.add(new Point(i, i * i));
//      points.add(new Point(-i, i * i));
      points.add(new Point(i, -i * i));
//      points.add(new Point(-i, -i * i));
    }
    System.out.println(points);
//    points.shuffle();
//    System.out.println(points);
    System.out.printf("Convex hull: %s\n", convexHull(points));

    GrahamScan grahamScan2 = new GrahamScan(points);
    System.out.println(grahamScan2.hull());
    System.out.println(grahamScan2.hull().size());
  }

}
