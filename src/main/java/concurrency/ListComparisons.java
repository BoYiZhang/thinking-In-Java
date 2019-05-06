//: concurrency/ListComparisons.java
package concurrency; /* Added by Eclipse.py */
// {Args: 1 10 10} (Fast verification check during build)
// Rough comparison of thread-safe List performance.
import java.util.concurrent.*;
import java.util.*;
import net.mindview.util.*;

abstract class ListTest extends Tester<List<Integer>> {
  ListTest(String testId, int nReaders, int nWriters) {
    super(testId, nReaders, nWriters);
  }
  class Reader extends TestTask {
    long result = 0;
    void test() {
      for(long i = 0; i < testCycles; i++)
        for(int index = 0; index < containerSize; index++)
          result += testContainer.get(index);
    }
    void putResults() {
      readResult += result;
      readTime += duration;
    }
  }
  class Writer extends TestTask {
    void test() {
      for(long i = 0; i < testCycles; i++)
        for(int index = 0; index < containerSize; index++)
          testContainer.set(index, writeData[index]);
    }
    void putResults() {
      writeTime += duration;
    }
  }
  void startReadersAndWriters() {
    for(int i = 0; i < nReaders; i++)
      exec.execute(new Reader());
    for(int i = 0; i < nWriters; i++)
      exec.execute(new Writer());
  }
}

class SynchronizedArrayListTest extends ListTest {
  List<Integer> containerInitializer() {
    return Collections.synchronizedList(
      new ArrayList<Integer>(
        new CountingIntegerList(containerSize)));
  }
  SynchronizedArrayListTest(int nReaders, int nWriters) {
    super("Synched ArrayList", nReaders, nWriters);
  }
}

class CopyOnWriteArrayListTest extends ListTest {
  List<Integer> containerInitializer() {
    return new CopyOnWriteArrayList<Integer>(
      new CountingIntegerList(containerSize));
  }
  CopyOnWriteArrayListTest(int nReaders, int nWriters) {
    super("CopyOnWriteArrayList", nReaders, nWriters);
  }
}

public class ListComparisons {
  public static void main(String[] args) {
    Tester.initMain(args);
    new SynchronizedArrayListTest(10, 0);
    new SynchronizedArrayListTest(9, 1);
    new SynchronizedArrayListTest(5, 5);
    new CopyOnWriteArrayListTest(10, 0);
    new CopyOnWriteArrayListTest(9, 1);
    new CopyOnWriteArrayListTest(5, 5);
    Tester.exec.shutdown();
  }
}

/* Output: (Sample)

Connected to the target VM, address: '127.0.0.1:65003', transport: 'socket'
Type                             Read time     Write time
Synched ArrayList 10r 0w        3512594541              0
Synched ArrayList 10r 0w        2937623559              0
Synched ArrayList 10r 0w        3340745894              0
Synched ArrayList 10r 0w        2840354133              0
Synched ArrayList 10r 0w        2368234981              0
Synched ArrayList 10r 0w        3086702691              0
Synched ArrayList 10r 0w        2887693635              0
Synched ArrayList 10r 0w        2245958759              0
Synched ArrayList 10r 0w        3317351626              0
Synched ArrayList 10r 0w        2834734228              0
Synched ArrayList 9r 1w         2523648333      308976013
readTime + writeTime =          2832624346
Synched ArrayList 9r 1w         2218258053      293072902
readTime + writeTime =          2511330955
Synched ArrayList 9r 1w         1826333852      253692122
readTime + writeTime =          2080025974
Synched ArrayList 9r 1w         2673196955      327132491
readTime + writeTime =          3000329446
Synched ArrayList 9r 1w         2993254058      358339756
readTime + writeTime =          3351593814
Synched ArrayList 9r 1w         2904877885      346877765
readTime + writeTime =          3251755650
Synched ArrayList 9r 1w         3150756484      389193946
readTime + writeTime =          3539950430
Synched ArrayList 9r 1w         4419373626      506665610
readTime + writeTime =          4926039236
Synched ArrayList 9r 1w         5181670451      601723130
readTime + writeTime =          5783393581
Synched ArrayList 9r 1w         4132687621      487124650
readTime + writeTime =          4619812271
Synched ArrayList 5r 5w         2311926521     3204084473
readTime + writeTime =          5516010994
Synched ArrayList 5r 5w         1581561790     3129466016
readTime + writeTime =          4711027806
Synched ArrayList 5r 5w         2075233239     3355995866
readTime + writeTime =          5431229105
Synched ArrayList 5r 5w         2007729649     2911895847
readTime + writeTime =          4919625496
Synched ArrayList 5r 5w         2553496319     3523451842
readTime + writeTime =          6076948161
Synched ArrayList 5r 5w         3308712661     4544369349
readTime + writeTime =          7853082010
Synched ArrayList 5r 5w         2087369605     2786030232
readTime + writeTime =          4873399837
Synched ArrayList 5r 5w         1530961236     2922042207
readTime + writeTime =          4453003443
Synched ArrayList 5r 5w         2030490402     2927471538
readTime + writeTime =          4957961940
Synched ArrayList 5r 5w         2028213925     3178100791
readTime + writeTime =          5206314716



CopyOnWriteArrayList 10r 0w      144119379              0
CopyOnWriteArrayList 10r 0w       70728066              0
CopyOnWriteArrayList 10r 0w       70346745              0
CopyOnWriteArrayList 10r 0w       64401796              0
CopyOnWriteArrayList 10r 0w       69977417              0
CopyOnWriteArrayList 10r 0w       67339217              0
CopyOnWriteArrayList 10r 0w       70666921              0
CopyOnWriteArrayList 10r 0w       62946067              0
CopyOnWriteArrayList 10r 0w       66416181              0
CopyOnWriteArrayList 10r 0w       62072360              0
CopyOnWriteArrayList 9r 1w       125581133       67633677
readTime + writeTime =           193214810
CopyOnWriteArrayList 9r 1w       119137405       59454923
readTime + writeTime =           178592328
CopyOnWriteArrayList 9r 1w       115258009       57474321
readTime + writeTime =           172732330
CopyOnWriteArrayList 9r 1w        66811970       45228843
readTime + writeTime =           112040813
CopyOnWriteArrayList 9r 1w        70288252       42073391
readTime + writeTime =           112361643
CopyOnWriteArrayList 9r 1w        99088487       53282724
readTime + writeTime =           152371211
CopyOnWriteArrayList 9r 1w        80244035       54728227
readTime + writeTime =           134972262
CopyOnWriteArrayList 9r 1w       112609154       64273192
readTime + writeTime =           176882346
CopyOnWriteArrayList 9r 1w        63831645       45628696
readTime + writeTime =           109460341
CopyOnWriteArrayList 9r 1w       106272710       61100882
readTime + writeTime =           167373592
CopyOnWriteArrayList 5r 5w       113031126     1369690872
readTime + writeTime =          1482721998
CopyOnWriteArrayList 5r 5w        32546918     1096845753
readTime + writeTime =          1129392671
CopyOnWriteArrayList 5r 5w        31583349     1115051013
readTime + writeTime =          1146634362
CopyOnWriteArrayList 5r 5w        38276392     1071529067
readTime + writeTime =          1109805459
CopyOnWriteArrayList 5r 5w        32935215     1084023496
readTime + writeTime =          1116958711
CopyOnWriteArrayList 5r 5w        33928712     1083800203
readTime + writeTime =          1117728915
CopyOnWriteArrayList 5r 5w        34174913     1093682913
readTime + writeTime =          1127857826
CopyOnWriteArrayList 5r 5w        35747555     1069857883
readTime + writeTime =          1105605438
CopyOnWriteArrayList 5r 5w        41074016     1076585950
readTime + writeTime =          1117659966
CopyOnWriteArrayList 5r 5w        32871855     1056785182
readTime + writeTime =          1089657037
Disconnected from the target VM, address: '127.0.0.1:65003', transport: 'socket'



*///:~
