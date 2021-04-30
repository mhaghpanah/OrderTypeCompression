package OrderTypeCompression;

import gurobi.GRB;
import gurobi.GRB.IntParam;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.Arrays;

public class RealizeWithHint implements PointsRealization {

  public static final String name = "RealizeWithHint";
  private static final double INF = (1 << 16) - 1;
  private static final double EPSILON = 1.0 - 1e-1;
//  private static final double EPSILON = 0.5;
  private static final boolean OUTPUT_FLAG = false;
  private int n;
  private Orientations orientations;
  private long[] x;
  private long[] y;

  private static final int[][] hints = new int[][] {{},{},{},{65535,32768,0},{65535,41158,24377,0},{65535,45125,32767,20410,0},{65535,47512,37298,28237,18023,0},{65535,49143,40229,32767,25306,16392,0},{65535,50344,42317,35835,29700,23218,15191,0},{65535,51265,43887,38082,32768,27453,21648,14270,0},{65535,51998,45119,39811,35064,30471,25724,20416,13537,0},{65535,52598,46118,41190,36860,32768,28675,24345,19417,12937,0},{65535,53111,46958,42330,38318,34589,30946,27217,23205,18577,12424,0}};

  GRBEnv env;
  public RealizeWithHint() {
    try {
      // Create empty environment, set options, and start
      env = new GRBEnv(true);
      if (OUTPUT_FLAG) {
        env.set("logFile", String.format("%s.log", name));
      } else {
        env.set(IntParam.OutputFlag, 0);
      }
      // Set the integrality focus
//      env.set(IntParam.IntegralityFocus, 1);
      env.start();
    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
    }
  }

  public Points solve(Orientations orientations) {
    this.orientations = orientations;
    n = orientations.getN();
    x = new long[n];
    y = new long[n];
    Points ans = null;

    boolean XFixed = true;
    boolean solved = alternativeRealization(XFixed);
    if (solved) {
      ans = new Points(x, y);
    }
    return ans;
  }

  public boolean alternativeRealization(boolean XFixed) {
    Arrays.fill(x, 0);
    Arrays.fill(y, 0);

    long[] t = XFixed ? x : y;
    for (int i = 0; i < n; i++) {
      t[i] = hints[n][i];
    }

    if (solve(XFixed)) {
      System.err.printf("%s: Solved!\n", name);
      return true;
    }

    System.err.printf("%s: Not found Solutions!\n", name);
    return false;
  }

  private boolean solve(boolean XFixed) {

    boolean XVar = !XFixed;
    try {

//      // Create empty environment, set options, and start
//      GRBEnv env = new GRBEnv(true);
//      if (OUTPUT_FLAG) {
//        env.set("logFile", String.format("%s.log", name));
//      } else {
//        env.set(IntParam.OutputFlag, 0);
//      }
//      // Set the integrality focus
////      env.set(IntParam.IntegralityFocus, 1);
//      env.start();

      // Create empty model
      GRBModel model = new GRBModel(env);

      // Create variables
      GRBVar[] grbVars = new GRBVar[n];
      String str = XVar ? "x" : "y";
      for (int i = 0; i < n; i++) {
        grbVars[i] = model.addVar(0, INF, 0.0, GRB.CONTINUOUS, String.format("%s[%d]", str, i));
      }

      // Add constraint: x + 2 y + 3 z <= 4
//      if (XVar) {
//        ConstraintBuilder.orderConstraints(model, grbVars);
//      }

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

      System.out.println(optimizationStatus);
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
//          System.out.println(grbVars[i].get(GRB.DoubleAttr.X));
          t[i] = DoubleEpsilonCompare.integrality(grbVars[i].get(GRB.DoubleAttr.X));
        }
//        System.out.println(Arrays.toString(t));
        ans = orientations.isSameOrderType(new Points(x, y));
      }

      ConstraintBuilder.debugSolution(optimizationStatus, OUTPUT_FLAG);

      // Dispose of model and environment
      model.dispose();
//      env.dispose();
      return ans;

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
      return false;
    }

  }


  public static void main(String[] args) {
//    int MAX = 12;
//    int[][] expectedAns = new int[][] {{},{},{},{65535,32768,0},{65535,41158,24377,0},{65535,45125,32767,20410,0},{65535,47512,37298,28237,18023,0},{65535,49143,40229,32767,25306,16392,0},{65535,50344,42317,35835,29700,23218,15191,0},{65535,51265,43887,38082,32768,27453,21648,14270,0},{65535,51998,45119,39811,35064,30471,25724,20416,13537,0},{65535,52598,46118,41190,36860,32768,28675,24345,19417,12937,0},{65535,53111,46958,42330,38318,34589,30946,27217,23205,18577,12424,0}};
//    for (int r = 0; r < 1000; r++) {
//      Points[] ans = new Points[MAX + 1];
//      for (int n = 3; n <= MAX; n++) {
////      Database database = Database.read(n);
////      Points points = database.get(0);
//        while (ans[n] == null) {
//          Points points = Points.randomPointGenerator(n);
//          points.sort();
//          Orientations orientations = new Orientations(points);
//
//          ans[n] = realizeWithHint.solve(orientations);
//          System.out.println(n);
//          System.out.println(ans[n]);
//        }
//      }
//      int[][] cs = new int[MAX + 1][];
//      for (int n = 0; n <= MAX; n++) {
//        int[] t;
//        if (3 <= n && n <= 12) {
//          t = new int[n];
//          for (int i = 0; i < n; i++)
//            t[i] = (int) ans[n].get(i).getX();
//        } else {
//          t = new int[0];
//        }
//        cs[n] = t;
//        assert Arrays.equals(cs[n], expectedAns[n]);
//      }
//
//      StringBuilder sb = new StringBuilder();
//      sb.append("= new int[][] {");
//      for (int n = 0; n <= MAX; n++) {
//        if (n > 0)
//          sb.append(",");
//        sb.append("{");
//        for (int i = 0; i < cs[n].length; i++) {
//          if (i > 0)
//            sb.append(",");
//          sb.append(cs[n][i]);
//        }
//        sb.append("}");
//      }
//      sb.append("};");
//      System.out.println(sb.toString());
//
//      assert Arrays.deepEquals(cs, expectedAns);
//    }
  }

}
