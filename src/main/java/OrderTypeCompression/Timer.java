package OrderTypeCompression;

public class Timer {

  private long startTime;
  private long endTime;
  private long elapsedTime;
  private long memAvailable;
  private long memUsed;
  private boolean ready;

  public Timer() {
    start();
  }

  private long now() {
//    Instant instant = Instant.now();
//    Duration.between(start, finish).getNano()
//    long t = TimeUnit.NANOSECONDS.toMicros(instant.getNano());
//    return t;
    return System.nanoTime() / 1000;
//    return System.currentTimeMillis();
  }

  public void start() {
    startTime = now();
    ready = false;
  }

  public Timer end() {
    endTime = now();
    elapsedTime = endTime - startTime;
    memAvailable = Runtime.getRuntime().totalMemory();
    memUsed = memAvailable - Runtime.getRuntime().freeMemory();
    ready = true;
    return this;
  }

  public long duration() {
    if (!ready) {
      end();
    }
    return elapsedTime;
  }

  public long memory() {
    if (!ready) {
      end();
    }
    return memUsed;
  }

  public String toString() {
    if (!ready) {
      end();
    }
    return "Time: " + elapsedTime + " micro sec.\n" + "Memory: " + (memUsed/1048576) + " MB / " + (memAvailable/1048576) + " MB.";
  }

}
