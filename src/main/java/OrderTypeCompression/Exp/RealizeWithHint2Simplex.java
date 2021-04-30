package OrderTypeCompression.Exp;

import OrderTypeCompression.DoubleEpsilonCompare;
import OrderTypeCompression.Orientations;
import OrderTypeCompression.Points;
import OrderTypeCompression.PointsRealization;
import edu.princeton.cs.algs4.LinearProgramming;
import java.util.Arrays;

public class RealizeWithHint2Simplex implements PointsRealization {

  public static final String name = "RealizeWithHint2Simplex";
  private static final double INF = (1 << 24) - 1;
  private static final double EPSILON = 1.0 - 1e-1;
//  private static final double EPSILON = 0.1;
  private static final boolean OUTPUT_FLAG = false;
  private final int n;
  private Orientations orientations;
  private final long[] x;
  private final long[] y;

  private final double[][] A;
  private final double[] b;
  private final double[] c;

  private static final int[][] hints = new int[][] {{},{},{},{65535,32768,0},{65535,41158,24377,0},{65535,45125,32767,20410,0},{65535,47512,37298,28237,18023,0},{65535,49143,40229,32767,25306,16392,0},{65535,50344,42317,35835,29700,23218,15191,0},{65535,51265,43887,38082,32768,27453,21648,14270,0},{65535,51998,45119,39811,35064,30471,25724,20416,13537,0},{65535,52598,46118,41190,36860,32768,28675,24345,19417,12937,0},{65535,53111,46958,42330,38318,34589,30946,27217,23205,18577,12424,0}};

  public RealizeWithHint2Simplex(int n) {
    this.n = n;
    x = new long[n];
    y = new long[n];

    for (int i = 0; i < n; i++) {
      x[i] = hints[n][i];
    }

    int m = (n * (n - 1) * (n - 2)) / 6;
    A = new double[m][n];
    b = new double[m];
    Arrays.fill(b, 0.0);
    c = new double[n];
//    Arrays.fill(c, 1.0);

    initialize();

  }

  public RealizeWithHint2Simplex(int n, int[] hint) {
    assert n == hint.length;

    this.n = n;
    x = new long[n];
    y = new long[n];

    for (int i = 0; i < n; i++) {
      x[i] = hint[i];
    }

    int m = (n * (n - 1) * (n - 2)) / 6;
    A = new double[m][n];
    b = new double[m];
    Arrays.fill(b, -1.0);
    c = new double[n];
    Arrays.fill(c, 1.0);

    initialize();
  }


  public void initialize() {
    int index = 0;
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        for (int k = j + 1; k < n; k++) {

          A[index][i] += x[j];
          A[index][j] += -x[i];

//          expr.addTerm(x[j], grbVars[i]);
//          expr.addTerm(-x[i], grbVars[j]);

          A[index][k] += x[i];
          A[index][i] += -x[k];

//          expr.addTerm(x[i], grbVars[k]);
//          expr.addTerm(-x[k], grbVars[i]);

          A[index][j] += x[k];
          A[index][k] += -x[j];

//          expr.addTerm(x[k], grbVars[j]);
//          expr.addTerm(-x[j], grbVars[k]);

          index++;
        }
      }
    }
  }

  public Points solve(Orientations orientations) {
    assert n == orientations.getN();
    this.orientations = orientations;

    Points ans = null;

    boolean XFixed = true;
    boolean solved = solve(XFixed);
    if (solved) {
      System.err.printf("%s: Solved!\n", name);
      ans = new Points(x, y);
      return ans;
    } else {
      System.err.printf("%s: Not found Solutions!\n", name);
      return ans;
    }
  }

  private boolean solve(boolean XFixed) {

    boolean XVar = !XFixed;
    try {

      int index = 0;
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          for (int k = j + 1; k < n; k++) {
            double sense = !orientations.getOrderType(i, j, k) ^ XFixed ? 1.0 : -1.0;

            A[index][i] += sense * x[j];
            A[index][j] += sense * -x[i];

            A[index][k] += sense * x[i];
            A[index][i] += sense * -x[k];

            A[index][j] += sense * x[k];
            A[index][k] += sense * -x[j];

            index++;
          }
        }
      }

      LinearProgramming lp = new LinearProgramming(A, b, c);

      System.out.printf("A : %s\n", Arrays.deepToString(A));
      System.out.printf("b : %s\n", Arrays.toString(b));
      System.out.printf("c : %s\n", Arrays.toString(c));

      double[] primal = lp.primal();
      double[] dual = lp.dual();

      double value = lp.value();
      System.out.println(value);
      System.out.printf("primal %s\n", Arrays.toString(primal));
      System.out.printf("dual %s\n", Arrays.toString(dual));

      long[] t = XVar ? x : y;

      for (int i = 0; i < n; i++) {
        //todo fix it
        t[i] = DoubleEpsilonCompare.integrality(primal[i] * INF);
//          System.out.println(t[i]);
      }

      boolean ans = orientations.isSameOrderType(new Points(x, y));
      return ans;

    } catch (ArithmeticException e) {
      System.err.println(e);
      return false;
    }

  }

}
