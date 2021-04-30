package OrderTypeCompression;

import java.io.IOException;
import java.io.InputStream;

public class ReadKBytes {

  int k;
  InputStream in;

  byte[] tmp;
  final int kiloByteSize = 1024;
  final int megaByteSize = 1024 * kiloByteSize;
  final int bufSize = 128*megaByteSize;

  int idx;
  int sz;

  public ReadKBytes(int k, InputStream in) {
    this.k = k;
    this.in = in;

    tmp = new byte[bufSize];

    idx = 0;
    sz = 0;
  }

  public int read(byte[] buf, int n) throws IOException {
    int len = 0;
    while (len < n) {
      if (idx < sz) buf[len++]  = tmp[idx++];
      else {
        sz = in.read(tmp);
        idx = 0;
        if (sz <= 0) break;
      }
    }
    return len;
  }

  public int read(byte[] buf) throws IOException {
    return read(buf, k);
  }



}
