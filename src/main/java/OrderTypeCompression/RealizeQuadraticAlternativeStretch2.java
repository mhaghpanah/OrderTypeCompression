package OrderTypeCompression;

import gurobi.GRB;
import gurobi.GRB.IntParam;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.Arrays;
import java.util.Random;

public class RealizeQuadraticAlternativeStretch2 implements PointsRealization {

  public static final String name = "RealizeQuadraticAlternativeStretch2";
  private static final double INF = (1 << 16) - 1;
  private static final double EPSILON = 0.1;
//  private static final double EPSILON = 1.0 - 1e-1;
  private static final boolean OUTPUT_FLAG = false;
  private int n;
  private Orientations orientations;
  private long[] x;
  private long[] y;
  private int TRY_NUM = 20;
  private int TRY_NUM2 = 200;

  Random random = new Random();

  public RealizeQuadraticAlternativeStretch2() {}

  public RealizeQuadraticAlternativeStretch2(int TRY_NUM) {
    this.TRY_NUM = TRY_NUM;
  }

  public Points solve(Orientations orientations) {
    this.orientations = orientations;
    n = orientations.getN();
    x = new long[n];
    y = new long[n];
    Points ans = null;

    boolean XFixed = true;
    for (int i = 0; i < TRY_NUM2; i++) {
      if (i > 0) {
        for (int j = 0; j < n; j++) {
          y[j] = random.nextInt();
          x[j] = random.nextInt();
        }
        Arrays.sort(x);
      }
      if (i == 0) TRY_NUM = 20;
      else TRY_NUM = 6;
      boolean solved = alternativeRealization(XFixed);
      if (solved) {
        ans = new Points(x, y);
        System.err.printf("%s: Solved after %d trying!\n", name, i + 1);
        return ans;
//        break;
      }
    }
    System.err.printf("%s: Not found Solutions!\n", name);

    return ans;
  }

  public boolean alternativeRealization(boolean XFixed) {
//    Arrays.fill(x, 0);
//    Arrays.fill(y, 0);

    for (int i = 0; i < TRY_NUM; i++) {
      if (solve(XFixed)) {
//        System.err.printf("%s: Solved after %d trying!\n", name, i + 1);
        return true;
      }
      XFixed = !XFixed;
    }
//    System.err.printf("%s: Not found Solutions!\n", name);
    return false;
  }

  private void stretch(double[] t) {
    double min = t[0];
    double max = t[0];
    for (int i = 1; i < t.length; i++) {
      min = Math.min(min, t[i]);
      max = Math.max(max, t[i]);
    }

    assert max > min;

    double threshold = INF/10;
    double rangeStart = 1.0 / 3.0 * INF;
    double rangeEnd = 2.0 / 3.0 * INF;
    if (max - min <= threshold) {
      for (int i = 0; i < t.length; i++) {
        t[i] = rangeStart +  ((t[i] - min) / (max - min)) * (rangeEnd - rangeStart);
      }
    }

  }

  private boolean solve(boolean XFixed) {

    boolean XVar = !XFixed;
    try {

      // Create empty environment, set options, and start
      GRBEnv env = new GRBEnv(true);
      if (OUTPUT_FLAG) {
        env.set("logFile", String.format("%s.log", name));
      } else {
        env.set(IntParam.OutputFlag, 0);
      }
      // Set the integrality focus
//      env.set(IntParam.IntegralityFocus, 1);
//      env.set(DoubleParam.TimeLimit, 5);
      env.set(IntParam.BarHomogeneous, 1);
      env.start();

      // Create empty model
      GRBModel model = new GRBModel(env);

      // Create variables
      GRBVar[] grbVars = new GRBVar[n];
      String str = XVar ? "x" : "y";
      for (int i = 0; i < n; i++) {
        grbVars[i] = model.addVar(0, INF, 0.0, GRB.CONTINUOUS, String.format("%s[%d]", str, i));
      }

      // Add constraint: x + 2 y + 3 z <= 4
      if (XVar) {
//        ConstraintBuilder.reverseOrderConstraints(model, grbVars);
        ConstraintBuilder.orderConstraints(model, grbVars);
      }

      long[] t = XFixed ? x : y;
      ConstraintBuilder.orientationsConstraints(model, grbVars, t, orientations, XFixed, EPSILON);
      model.feasRelax(GRB.FEASRELAX_QUADRATIC, false, false, true);

      if (OUTPUT_FLAG) {
        model.write(String.format("%s.mps", name));
        model.write(String.format("%s.lp", name));
      }

      // Optimize model
      model.optimize();

      int optimizationStatus = model.get(GRB.IntAttr.Status);
      double objVal;
      boolean ans = false;

      if (optimizationStatus == GRB.Status.OPTIMAL || optimizationStatus == GRB.Status.SUBOPTIMAL) {
        if (OUTPUT_FLAG) {
          model.write(String.format("%s.sol", name));
          objVal = model.get(GRB.DoubleAttr.ObjVal);
          System.out.println("Optimal objective: " + objVal);
        }

        t = XVar ? x : y;

        double[] tmpAns = new double[n];
        for (int i = 0; i < n; i++) {
          tmpAns[i] = grbVars[i].get(GRB.DoubleAttr.X);
        }

        stretch(tmpAns);

        for (int i = 0; i < n; i++) {
          //todo fix it
//            t[i] = (long) grbVars[i].get(GRB.DoubleAttr.X);
//          t[i] = DoubleEpsilonCompare.integrality(grbVars[i].get(GRB.DoubleAttr.X));
//          if (optimizationStatus == Status.SUBOPTIMAL)
          t[i] = DoubleEpsilonCompare.integrality(tmpAns[i]);

          if (OUTPUT_FLAG)
            System.out.printf("x[%d] = %d y[%d] = %d %f\n", i, x[i], i, y[i],
                grbVars[i].get(GRB.DoubleAttr.X));
        }
        ans = orientations.isSameOrderType(new Points(x, y));
      }

      ConstraintBuilder.debugSolution(optimizationStatus, OUTPUT_FLAG);

      // Dispose of model and environment
      model.dispose();
      env.dispose();
      return ans;

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
      return false;
    }

  }

}
