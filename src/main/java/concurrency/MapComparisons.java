//: concurrency/MapComparisons.java
package concurrency; /* Added by Eclipse.py */
// {Args: 1 10 10} (Fast verification check during build)
// Rough comparison of thread-safe Map performance.
import java.util.concurrent.*;
import java.util.*;
import net.mindview.util.*;

abstract class MapTest
extends Tester<Map<Integer,Integer>> {
  MapTest(String testId, int nReaders, int nWriters) {
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
          testContainer.put(index, writeData[index]);
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

class SynchronizedHashMapTest extends MapTest {
  Map<Integer,Integer> containerInitializer() {
    return Collections.synchronizedMap(
      new HashMap<Integer,Integer>(
        MapData.map(
          new CountingGenerator.Integer(),
          new CountingGenerator.Integer(),
          containerSize)));
  }
  SynchronizedHashMapTest(int nReaders, int nWriters) {
    super("Synched HashMap", nReaders, nWriters);
  }
}

class ConcurrentHashMapTest extends MapTest {
  Map<Integer,Integer> containerInitializer() {
    return new ConcurrentHashMap<Integer,Integer>(
      MapData.map(
        new CountingGenerator.Integer(),
        new CountingGenerator.Integer(), containerSize));
  }
  ConcurrentHashMapTest(int nReaders, int nWriters) {
    super("ConcurrentHashMap", nReaders, nWriters);
  }
}

public class MapComparisons {
  public static void main(String[] args) {
    Tester.initMain(args);
    new SynchronizedHashMapTest(10, 0);
    new SynchronizedHashMapTest(9, 1);
    new SynchronizedHashMapTest(5, 5);
    new ConcurrentHashMapTest(10, 0);
    new ConcurrentHashMapTest(9, 1);
    new ConcurrentHashMapTest(5, 5);
    Tester.exec.shutdown();
  }
} /* Output: (Sample)

Type                             Read time     Write time
Synched HashMap 10r 0w          6242418056              0
Synched HashMap 10r 0w          4146402296              0
Synched HashMap 10r 0w          5575418578              0
Synched HashMap 10r 0w          3834073584              0
Synched HashMap 10r 0w          5194782005              0
Synched HashMap 10r 0w          5161536963              0
Synched HashMap 10r 0w          4444520893              0
Synched HashMap 10r 0w          3871868695              0
Synched HashMap 10r 0w          4408989207              0
Synched HashMap 10r 0w          8023118598              0

ConcurrentHashMap 10r 0w         344685546              0
ConcurrentHashMap 10r 0w         346848415              0
ConcurrentHashMap 10r 0w         318704071              0
ConcurrentHashMap 10r 0w         405478178              0
ConcurrentHashMap 10r 0w         207930162              0
ConcurrentHashMap 10r 0w         366067455              0
ConcurrentHashMap 10r 0w         226500276              0
ConcurrentHashMap 10r 0w         296423990              0
ConcurrentHashMap 10r 0w         320249183              0
ConcurrentHashMap 10r 0w         508154405              0


读取性能差 10 倍以上





Synched HashMap 9r 1w           6513091741      753915689
readTime + writeTime =          7267007430
Synched HashMap 9r 1w           6028976406      641421250
readTime + writeTime =          6670397656
Synched HashMap 9r 1w           5676651670      653679779
readTime + writeTime =          6330331449
Synched HashMap 9r 1w           5908498114      494457577
readTime + writeTime =          6402955691
Synched HashMap 9r 1w           7834818531      844974551
readTime + writeTime =          8679793082
Synched HashMap 9r 1w           5914246757      645557937
readTime + writeTime =          6559804694
Synched HashMap 9r 1w           7138638960      744661626
readTime + writeTime =          7883300586
Synched HashMap 9r 1w           6789718783      710857815
readTime + writeTime =          7500576598
Synched HashMap 9r 1w           5310600463      599121405
readTime + writeTime =          5909721868
Synched HashMap 9r 1w           5655007305      586903118
readTime + writeTime =          6241910423


ConcurrentHashMap 9r 1w          216104674       54419560
readTime + writeTime =           270524234
ConcurrentHashMap 9r 1w          294262252       40927173
readTime + writeTime =           335189425
ConcurrentHashMap 9r 1w          264142918       43273241
readTime + writeTime =           307416159
ConcurrentHashMap 9r 1w          273924527       48728635
readTime + writeTime =           322653162
ConcurrentHashMap 9r 1w          369514606       50730634
readTime + writeTime =           420245240
ConcurrentHashMap 9r 1w          219419103       38947284
readTime + writeTime =           258366387
ConcurrentHashMap 9r 1w          252190432       30225329
readTime + writeTime =           282415761
ConcurrentHashMap 9r 1w          228296180       25051090
readTime + writeTime =           253347270
ConcurrentHashMap 9r 1w          303395962       49612912
readTime + writeTime =           353008874
ConcurrentHashMap 9r 1w          171637818       35856054
readTime + writeTime =           207493872


预计相差 25 倍以上.




Synched HashMap 5r 5w           3787200263     3601518353
readTime + writeTime =          7388718616
Synched HashMap 5r 5w           3089274985     2733346333
readTime + writeTime =          5822621318
Synched HashMap 5r 5w           3408395059     3113962082
readTime + writeTime =          6522357141
Synched HashMap 5r 5w           3981206639     3679965007
readTime + writeTime =          7661171646
Synched HashMap 5r 5w           3265915194     3142623898
readTime + writeTime =          6408539092
Synched HashMap 5r 5w           3164752807     3000101498
readTime + writeTime =          6164854305
Synched HashMap 5r 5w           3336219924     3014104874
readTime + writeTime =          6350324798
Synched HashMap 5r 5w           3124944592     2991122381
readTime + writeTime =          6116066973
Synched HashMap 5r 5w           3977208930     3839928054
readTime + writeTime =          7817136984
Synched HashMap 5r 5w           3074772608     2874037633
readTime + writeTime =          5948810241



ConcurrentHashMap 5r 5w           99540221      462089771
readTime + writeTime =           561629992
ConcurrentHashMap 5r 5w          240567089      518136870
readTime + writeTime =           758703959
ConcurrentHashMap 5r 5w          155490877      432835809
readTime + writeTime =           588326686
ConcurrentHashMap 5r 5w          220516994      483831256
readTime + writeTime =           704348250
ConcurrentHashMap 5r 5w          216032229      572084619
readTime + writeTime =           788116848
ConcurrentHashMap 5r 5w          197179986      620970499
readTime + writeTime =           818150485
ConcurrentHashMap 5r 5w          254407618      544484450
readTime + writeTime =           798892068
ConcurrentHashMap 5r 5w          208138769      501331795
readTime + writeTime =           709470564
ConcurrentHashMap 5r 5w          180873142      492012085
readTime + writeTime =           672885227
ConcurrentHashMap 5r 5w          164806795      559711679
readTime + writeTime =           724518474

预计相差 10 倍以上




*///:~
