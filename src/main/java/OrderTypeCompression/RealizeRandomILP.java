package OrderTypeCompression;

import gurobi.GRB;
import gurobi.GRB.IntParam;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.Random;

public class RealizeRandomILP implements PointsRealization {

  public static final String name = "RandomILP";
  private static double INF = 1 << 16;
  private static final double EPSILON = 1.0 - 1e-1;
  private static final boolean OUTPUT_FLAG = false;
  private final Random random;
  private int n;
  private Orientations orientations;
  private long[] x;
  private long[] y;

  public RealizeRandomILP() {
    random = new Random();
  }

  public Points solve(Orientations orientations) {
    this.orientations = orientations;
    n = orientations.getN();
    x = new long[n];
    y = new long[n];
    Points ans = null;

    boolean XFixed = false;
    boolean solved = randomRealization(XFixed);
//    boolean solved = randomRealization2(XFixed);
    if (solved) {
      ans = new Points(x, y);
    }
    return ans;
  }

  private void randomAssignment(long[] t, int bound) {
    for (int i = 0; i < t.length; i++) {
      t[i] = random.nextInt(bound);
    }
  }

  public boolean randomRealization(boolean XFixed) {
    int TRY_NUM = 10_000;
    for (int i = 0; i < TRY_NUM; i++) {
      randomAssignment(x, (int) INF);
      randomAssignment(y, (int) INF);
      if (solve(XFixed)) {
        System.err.printf("%s: Solved after %d trying!\n", name, i + 1);
        return true;
      }
    }
    System.err.printf("%s: Not found Solutions!\n", name);
    return false;
  }

  public boolean randomRealization2(boolean XFixed) {
    int TRY_NUM = 100;
    int count = 0;
    for (int b = 8; b <= 16; b++) {
      INF = (1 << b);
      for (int i = 0; i < TRY_NUM; i++) {
        randomAssignment(x, (int) INF);
        randomAssignment(y, (int) INF);
        if (solve(XFixed)) {
          System.err.printf("%s: Solved after %d trying!\n", name, count + 1);
          return true;
        }
        count++;
      }
      TRY_NUM *= 1.2;
    }
    System.err.printf("%s: Not found Solutions!\n", name);
    return false;
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
      env.start();

      // Create empty model
      GRBModel model = new GRBModel(env);

      // Create variables
      GRBVar[] grbVars = new GRBVar[n];
      String str = XVar ? "x" : "y";
      for (int i = 0; i < n; i++) {
        grbVars[i] = model.addVar(0, INF, 0.0, GRB.INTEGER, String.format("%s[%d]", str, i));
      }

      // Add constraint: x + 2 y + 3 z <= 4
      long[] t = XFixed ? x : y;
      ConstraintBuilder.orientationsConstraints(model, grbVars, t, orientations, XFixed, EPSILON);

      if (OUTPUT_FLAG) {
        model.write(String.format("%s.mps", name));
        model.write(String.format("%s.lp", name));
      }

      // Optimize model
      model.optimize();

      int optimizationStatus = model.get(GRB.IntAttr.Status);
      double objVal;
      boolean ans = false;

      if (optimizationStatus == GRB.Status.OPTIMAL) {
        if (OUTPUT_FLAG) {
          model.write(String.format("%s.sol", name));
          objVal = model.get(GRB.DoubleAttr.ObjVal);
          System.out.println("Optimal objective: " + objVal);
        }

        t = XVar ? x : y;
        for (int i = 0; i < n; i++) {
          //todo fix it
//            t[i] = (long) grbVars[i].get(GRB.DoubleAttr.X);
          t[i] = DoubleEpsilonCompare.integrality(grbVars[i].get(GRB.DoubleAttr.X));

          System.out.printf("x[%d] = %d y[%d] = %d %f\n", i, x[i], i, y[i],
              grbVars[i].get(GRB.DoubleAttr.X));
        }
        assert orientations.isSameOrderType(new Points(x, y));
        ans = true;
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
