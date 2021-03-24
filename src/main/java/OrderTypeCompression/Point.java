package OrderTypeCompression;

public class Point {

  private long x;
  private long y;

  public Point(long x, long y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point p) {
    x = p.getX();
    y = p.getY();
  }

  public long getX() {
    return x;
  }

  public void setX(long x) {
    this.x = x;
  }

  public long getY() {
    return y;
  }

  public void setY(long y) {
    this.y = y;
  }

  public String toFileFormatString() {
    return String.format("%d %d\n", getX(), getY());
  }

  public Point plus(Point b) {
    Point a = this;
    long _x = a.x + b.x;
    long _y = a.y + b.y;
    return new Point(_x, _y);
  }

  public Point minus(Point b) {
    Point a = this;
    long _x = a.x - b.x;
    long _y = a.y - b.y;
    return new Point(_x, _y);
  }

  // Change method name
  public long cross(Point v, Point w) {
    return v.minus(this).cross(w.minus(this));
  }

  public long cross(Point p) {
    return this.getX() * p.getY() - this.getY() * p.getX();
  }

  public int compareTo(Point that) {
    if (this.getX() == that.getX()) {
      return Long.compare(this.getY(), that.getY());
    }
    return Long.compare(this.getX(), that.getX());
  }

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
    return String.format("(%d, %d)", getX(), getY());
  }
}
