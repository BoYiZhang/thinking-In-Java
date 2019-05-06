//: concurrency/SynchronizationComparisons.java
package concurrency; /* Added by Eclipse.py */
// Comparing the performance of explicit Locks
// and Atomics versus the synchronized keyword.
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.*;
import static net.mindview.util.Print.*;

abstract class Accumulator {
  public static long cycles = 50000L;
  // Number of Modifiers and Readers during each test:
  private static final int N = 4;
  public static ExecutorService exec =
    Executors.newFixedThreadPool(N*2);
  private static CyclicBarrier barrier =
    new CyclicBarrier(N*2 + 1);
  protected volatile int index = 0;
  protected volatile long value = 0;
  protected long duration = 0;
  protected String id = "error";
  protected final static int SIZE = 100000;
  protected static int[] preLoaded = new int[SIZE];
  static {
    // Load the array of random numbers:
    Random rand = new Random(47);
    for(int i = 0; i < SIZE; i++)
      preLoaded[i] = rand.nextInt();
  }
  public abstract void accumulate();
  public abstract long read();
  private class Modifier implements Runnable {
    public void run() {
      for(long i = 0; i < cycles; i++)
        accumulate();
      try {
        barrier.await();
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  private class Reader implements Runnable {
    private volatile long value;
    public void run() {
      for(long i = 0; i < cycles; i++)
        value = read();
      try {
        barrier.await();
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  public void timedTest() {
    long start = System.nanoTime();
    for(int i = 0; i < N; i++) {
      exec.execute(new Modifier());
      exec.execute(new Reader());
    }
    try {
      barrier.await();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
    duration = System.nanoTime() - start;
    printf("%-13s: %13d\n", id, duration);
  }

  public static void report(Accumulator acc1, Accumulator acc2) {
    printf("%-22s: %.2f\n", acc1.id + "/" + acc2.id,
      (double)acc1.duration/(double)acc2.duration);
  }
}

class BaseLine extends Accumulator {
  { id = "BaseLine"; }
  public void accumulate() {
    if( index < SIZE-1){
      value += preLoaded[index++];
      if(index >= SIZE) index = 0;
    }

  }
  public long read() { return value; }
}

class SynchronizedTest extends Accumulator {
  { id = "synchronized"; }
  public synchronized void accumulate() {
    value += preLoaded[index++];
    if(index >= SIZE) index = 0;
  }
  public synchronized long read() {
    return value;
  }
}

class LockTest extends Accumulator {
  { id = "Lock"; }
  private Lock lock = new ReentrantLock();
  public void accumulate() {
    lock.lock();
    try {
      value += preLoaded[index++];
      if(index >= SIZE) index = 0;
    } finally {
      lock.unlock();
    }
  }
  public long read() {
    lock.lock();
    try {
      return value;
    } finally {
      lock.unlock();
    }
  }
}

class AtomicTest extends Accumulator {
  { id = "Atomic"; }
  private AtomicInteger index = new AtomicInteger(0);
  private AtomicLong value = new AtomicLong(0);
  public void accumulate() {
    // Oops! Relying on more than one Atomic at
    // a time doesn't work. But it still gives us
    // a performance indicator:
    int i = index.getAndIncrement();

    if( index.get() < SIZE){

      value.getAndAdd(preLoaded[i]);
      if(++i >= SIZE)
        index.set(0);
    }

  }
  public long read() { return value.get(); }
}

public class SynchronizationComparisons {
  static BaseLine baseLine = new BaseLine();
  static SynchronizedTest synch = new SynchronizedTest();
  static LockTest lock = new LockTest();
  static AtomicTest atomic = new AtomicTest();
  static void test() {
    print("============================");
    printf("%-12s : %13d\n", "Cycles", Accumulator.cycles);
    baseLine.timedTest();
    synch.timedTest();
    lock.timedTest();
    atomic.timedTest();
    Accumulator.report(synch, baseLine);
    Accumulator.report(lock, baseLine);
    Accumulator.report(atomic, baseLine);
    Accumulator.report(synch, lock);
    Accumulator.report(synch, atomic);
    Accumulator.report(lock, atomic);
  }
  public static void main(String[] args) {
    int iterations = 8; // Default
    if(args.length > 0) // Optionally change iterations
      iterations = new Integer(args[0]);
    // The first time fills the thread pool:
    print("Warmup");
    baseLine.timedTest();
    // Now the initial test doesn't include the cost
    // of starting the threads for the first time.
    // Produce multiple data points:
    for(int i = 0; i < iterations; i++) {
      test();
      Accumulator.cycles *= 2;
    }
    Accumulator.exec.shutdown();
  }
}

/*
Connected to the target VM, address: '127.0.0.1:62371', transport: 'socket'
Warmup
BaseLine     :      21694209
============================
Cycles       :         50000
BaseLine     :       2180204
synchronized :      42437398
Lock         :      30372376
Atomic       :      26275467
synchronized/BaseLine : 19.46
Lock/BaseLine         : 13.93
Atomic/BaseLine       : 12.05
synchronized/Lock     : 1.40
synchronized/Atomic   : 1.62
Lock/Atomic           : 1.16
============================
Cycles       :        100000
BaseLine     :       5193179
synchronized :      82977464
Lock         :      31370928
Atomic       :      11210371
synchronized/BaseLine : 15.98
Lock/BaseLine         : 6.04
Atomic/BaseLine       : 2.16
synchronized/Lock     : 2.65
synchronized/Atomic   : 7.40
Lock/Atomic           : 2.80
============================
Cycles       :        200000
BaseLine     :       8938838
synchronized :     165124963
Lock         :      78268801
Atomic       :      13180858
synchronized/BaseLine : 18.47
Lock/BaseLine         : 8.76
Atomic/BaseLine       : 1.47
synchronized/Lock     : 2.11
synchronized/Atomic   : 12.53
Lock/Atomic           : 5.94
============================
Cycles       :        400000
BaseLine     :      24426392
synchronized :     331777960
Lock         :     115546684
Atomic       :      29905100
synchronized/BaseLine : 13.58
Lock/BaseLine         : 4.73
Atomic/BaseLine       : 1.22
synchronized/Lock     : 2.87
synchronized/Atomic   : 11.09
Lock/Atomic           : 3.86
============================
Cycles       :        800000
BaseLine     :      34345855
synchronized :     633854425
Lock         :     245733266
Atomic       :      87583065
synchronized/BaseLine : 18.46
Lock/BaseLine         : 7.15
Atomic/BaseLine       : 2.55
synchronized/Lock     : 2.58
synchronized/Atomic   : 7.24
Lock/Atomic           : 2.81
============================
Cycles       :       1600000
BaseLine     :      62631708
synchronized :    1594457825
Lock         :     654792500
Atomic       :     138332107
synchronized/BaseLine : 25.46
Lock/BaseLine         : 10.45
Atomic/BaseLine       : 2.21
synchronized/Lock     : 2.44
synchronized/Atomic   : 11.53
Lock/Atomic           : 4.73
============================
Cycles       :       3200000
BaseLine     :     128165186
synchronized :    3528972948
Lock         :    1278760479
Atomic       :     285986269
synchronized/BaseLine : 27.53
Lock/BaseLine         : 9.98
Atomic/BaseLine       : 2.23
synchronized/Lock     : 2.76
synchronized/Atomic   : 12.34
Lock/Atomic           : 4.47
============================
Cycles       :       6400000
BaseLine     :     220485251
synchronized :    6981991134
Lock         :    2650398226
Atomic       :     500422566
synchronized/BaseLine : 31.67
Lock/BaseLine         : 12.02
Atomic/BaseLine       : 2.27
synchronized/Lock     : 2.63
synchronized/Atomic   : 13.95
Lock/Atomic           : 5.30
 */