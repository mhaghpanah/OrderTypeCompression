package OrderTypeCompression;

import java.util.Arrays;

public class Orientations {

  private final int n;
  private final Points points;
  private final boolean[][][] orderType;
  private final int[][][] orientations;
  private byte[] bytes;

  public Orientations(Points points) {
    n = points.size();
    this.points = points;
    orientations = new int[n][n][n];
    orderType = new boolean[n][n][n];
    initial();
  }

  public Orientations(int n, byte[] bytes) {
    this.n = n;
    assert bytes.length == getByteSize(n);
    this.points = null;
    orientations = new int[n][n][n];
    orderType = new boolean[n][n][n];

    toOrderType(n, bytes);
    assert bytes.length == toByte().length;
    assert Arrays.equals(bytes, toByte());
  }

  public static int getByteSize(int n) {
    int size = (n * (n - 1) * (n - 2)) / 6;
    return (size + 7) / 8;
  }

  public static byte[] bytesOrientations(Points points) {
    Orientations orientations = new Orientations(points);
    return orientations.toByte();
  }

  public static boolean[][][] getOrderType(Points points) {
    Orientations orientations = new Orientations(points);
    return orientations.getOrderType();
  }

  public static int[][][] getOrientations(Points points) {
    Orientations orientations = new Orientations(points);
    return orientations.getOrientations();
  }

  public int getN() {
    return n;
  }

  public boolean[][][] getOrderType() {
    return orderType;
  }

  public boolean getOrderType(int i, int j, int k) {
    assert i < j && j < k;
    return orderType[i][j][k];
  }

  public int[][][] getOrientations() {
    return orientations;
  }

  public int getOrientations(int i, int j, int k) {
    assert i < j && j < k;
    return orientations[i][j][k];
  }

  public boolean isSameOrderType(Points other) {
    assert n == other.size();

    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        for (int k = j + 1; k < n; k++) {
          int orientation = Points.orientation(other.get(i), other.get(j), other.get(k));
          if (orientation == 0 || orientations[i][j][k] != orientation) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private void initial() {
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        for (int k = j + 1; k < n; k++) {
          int orientation = Points.orientation(points.get(i), points.get(j), points.get(k));

          orientations[i][j][k] = orientation;

          if (orientation == 0) {
            System.out.println(points.get(i));
            System.out.println(points.get(j));
            System.out.println(points.get(k));
            System.out.println(Points.orientation(points.get(i), points.get(j), points.get(k)));
          }
          assert orientation == -1 || orientation == 1;
          orderType[i][j][k] = orientation == 1;
        }
      }
    }
//    System.out.println(Arrays.deepToString(orientations));
//    System.out.println(Arrays.deepToString(orderType));
  }

  private void toOrderType(int n, byte[] bytes) {
    int index = 0;
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        for (int k = j + 1; k < n; k++) {
          if ((bytes[index / 8] & (1 << (index % 8))) > 0) {
            orderType[i][j][k] = true;
            orientations[i][j][k] = 1;
          } else {
            orderType[i][j][k] = false;
            orientations[i][j][k] = -1;
          }
          index++;
        }
      }
    }
  }

  public byte[] toByte() {
    if (bytes == null) {
      bytes = new byte[getByteSize(n)];
      int index = 0;
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          for (int k = j + 1; k < n; k++) {
            if (orderType[i][j][k]) {
//              System.out.printf("i: %d j: %d k: %d\n", i, j, k);
              bytes[index / 8] |= (1 << (index % 8));
            }
            index++;
          }
        }
      }
      assert (index + 7) / 8 == getByteSize(n);
    }
    return bytes;
  }

//  public static void main(String[] args) {
//    int n = 9;
//    Database database = Database.read(n);
//    int id = 6;
//    Points points = database.get(id);
//    Orientations orientations = new Orientations(points);
//    byte[] bytes = orientations.toByte();
//    Orientations orientations1 = new Orientations(n, bytes);
//    assert Arrays.equals(bytes, orientations1.toByte());
//  }

}
