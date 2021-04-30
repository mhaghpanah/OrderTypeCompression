package OrderTypeCompression;

public class Pair<K, V> {

  public final K k;
  public final V v;

  public Pair(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public K getKey() {
    return k;
  }

  public V getValue() {
    return v;
  }

  public static <K, V> Pair<K, V> getInstance(K k, V v) {
    return new Pair<>(k, v);
  }
}