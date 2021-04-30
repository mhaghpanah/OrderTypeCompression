package OrderTypeCompression.Exp;

import OrderTypeCompression.Database;
import OrderTypeCompression.Orientations;
import OrderTypeCompression.Points;
import OrderTypeCompression.PointsRealization;
import OrderTypeCompression.RealizeQuadraticAlternative;
import OrderTypeCompression.RealizeRandomILP;
import OrderTypeCompression.RealizeWithHint2;
import OrderTypeCompression.Timer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Iterative {

  int rate = 100;
  Database database;


  PointsRealization slowSolver;
  PointsRealization randomSolver;
  PointsRealization fastSolver;

  Random random;
  List<Points> pointsList;
  Set<Integer> remainedSet;
  List<int[]> hintList;
  List<Integer> solvedCount;
  int size;


  public Iterative(Database database) {
    this.database = database;

//    slowSolver = new RealizeQuadraticAlternativeStretch(100);
    slowSolver = new RealizeQuadraticAlternative(200);
    randomSolver = new RealizeRandomILP(200);

    random = new Random();
    pointsList = new ArrayList<>();
    remainedSet = new HashSet<>();
    hintList = new ArrayList<>();
    solvedCount = new ArrayList<>();
    initialize();
  }

  public void initialize() {
    for (Iterator<Points> it = database.sampleIterator(rate); it.hasNext(); ) {
      Points points = it.next();
      pointsList.add(points);
    }
    size = pointsList.size();
    for (int i = 0; i < size; i++) remainedSet.add(i);
  }


  public Points chooseUnsolvedPoints() {
    int remained = remainedSet.size();
    int index = random.nextInt(remained);
    for (Integer id : remainedSet) {
      if (index == 0) return pointsList.get(id);
      index--;
    }
    return null;
  }

  public Set<Integer> solveAndCount(int[] hint) {
    int n = hint.length;
    fastSolver = new RealizeWithHint2(n, hint);
    Set<Integer> solved = new HashSet<>();
    for (Integer id : remainedSet) {
      Points points1 = pointsList.get(id);
      Orientations orientations = new Orientations(points1);
      Points ans = fastSolver.solve(orientations);
      if (ans != null) {
        solved.add(id);
      }
    }
    return solved;
  }

  public int[] solveAndHint(Points points) {
    int n = points.size();
    int[] hint = new int[n];
    Orientations orientations = new Orientations(points);
    Points ans = slowSolver.solve(orientations);
    if (ans == null) ans = randomSolver.solve(orientations);
    if (ans == null) ans = points;
    for (int i = 0; i < n; i++) hint[i] = (int)ans.get(i).x();
    return hint;
  }

  public void applySolved(Set<Integer> solved) {
    for (Integer id : solved) {
      remainedSet.remove(id);
    }
  }

  public void solve() {
    int threshold = 10;
    int TryNum = 10;
    while (remainedSet.size() > 0 && TryNum-- > 0) {
      System.err.printf("%d -------------------\n", TryNum);
      Points points = chooseUnsolvedPoints();
      int[] hint = solveAndHint(points);
      Set<Integer> solved = solveAndCount(hint);
      if (solved.size() > threshold) {
        applySolved(solved);
        hintList.add(hint);
        solvedCount.add(solved.size());
      }
    }

    for (int i = 0; i < hintList.size(); i++) {
      System.out.printf("index = %d solved = %d\n", i, solvedCount.get(i));
    }
    System.out.printf("Remained %d\n", remainedSet.size());

  }

  public static void main(String[] args) {
    Timer timer = new Timer();
    int n = 10;
    Database database = Database.read(n);
    Iterative iterative = new Iterative(database);
    iterative.solve();
    System.out.println(timer);
  }
}
