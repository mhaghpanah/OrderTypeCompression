package OrderTypeCompression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BytesOrder {

  List<byte[]> bytesList;

  public BytesOrder() {
    this.bytesList = new ArrayList<>();
  }

  public BytesOrder(List<byte[]> bytesList) {
    this.bytesList = bytesList;
  }

  public void add(byte[] bytes) {
    bytesList.add(bytes);
  }

  public void sort() {
    Collections.sort(bytesList, lexicographicalOrder());
  }

  public String byteToString(byte b) {
    int ui = Byte.toUnsignedInt(b);
    String str = Integer.toBinaryString(ui);

    StringBuilder sb = new StringBuilder();
    for (int i = str.length(); i < 8; i++) {
      sb.append('0');
    }
    sb.append(str);

    return sb.toString();
  }

  public String bytesToString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(byteToString(b));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (byte[] bytes : bytesList) {
      if (sb.length() > 0) sb.append("#");
      sb.append(bytesToString(bytes));
    }
    return sb.toString();
  }

  public Comparator<byte[]> lexicographicalOrder() {
    return (b1, b2) -> {
      int len = Math.min(b1.length, b2.length);
      for (int i = 0; i < len; i++) {
        int c = Integer.compareUnsigned(b1[i], b2[i]);
        if (c != 0) return c;
      }
      if (b1.length < b2.length) return -1;
      if (b1.length == b2.length) return 0;
      return +1;
    };
  }

  public byte[] compress() {
    int size = bytesList.size() * bytesList.get(0).length;
    byte[] ans = new byte[size];
    int index = 0;
    for (byte[] bytes : bytesList) {
      for (int i = 0; i < bytes.length; i++) {
        ans[index++] = bytes[i];
      }
    }

    return ans;
  }

  public static void main(String[] args) {
    int n = 6;
    Database database = Database.read(n);
    BytesOrder bytesOrder = new BytesOrder();
    for (Points points : database) {
      byte[] bytes = Orientations.bytesOrientations(points);
      bytesOrder.add(bytes);
    }

    System.out.println(bytesOrder);
    bytesOrder.sort();
    System.out.println(bytesOrder);
  }
}
