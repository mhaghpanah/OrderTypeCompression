package OrderTypeCompression;

import gurobi.GRB;
import gurobi.GRB.IntParam;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RealizeLearnOrderQuadraticAlternative implements PointsRealization {

  public static final String name = "RealizeLearnOrderQuadraticAlternative";
  private static final double INF = (1 << 16) - 1;
  private static final double EPSILON = 0.5;
  private static final boolean OUTPUT_FLAG = false;
  private int n;
  private Orientations orientations;
  private long[] x;
  private long[] y;

  private List<Integer> perm;
  
  public Points solve(Orientations orientations) {
    this.orientations = orientations;
    n = orientations.getN();
    x = new long[n];
    y = new long[n];
//    slacks = new long[n][n][n];
    Points ans = null;

    boolean XFixed = false;
    boolean solved = alternativeRealization(XFixed);
    if (solved) {
      ans = new Points(x, y);
    }
    return ans;
  }

  public boolean alternativeRealization(boolean XFixed) {
    int TRY_NUM = 200;

    Arrays.fill(x, 0);
    Arrays.fill(y, 0);

    perm = new ArrayList<>();
    for (int i = 0; i < n; i++) perm.add(i);

    for (int i = 0; i < TRY_NUM; i++) {
      if (solve(XFixed, i)) {
          System.err.printf("%s: Solved after %d trying!\n", name, i + 1);
          return true;
        }
        XFixed = !XFixed;
      }
    System.err.printf("%s: Not found Solutions!\n", name);
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

    double threshold = INF/6;
    double rangeStart = 1.0 / 3.0 * INF;
    double rangeEnd = 2.0 / 3.0 * INF;
    if (max - min <= threshold) {
      for (int i = 0; i < t.length; i++) {
        t[i] = rangeStart +  ((t[i] - min) / (max - min)) * (rangeEnd - rangeStart);
      }
    }

  }

  private boolean solve(boolean XFixed, int round) {

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

      List<GRBConstr> constrs = new ArrayList<>();
      List<Double> rhspens = new ArrayList<>();
      if (XVar) {
        for (int i = 0; i < n - 1; i++) {
          GRBLinExpr expr = new GRBLinExpr();

          int r1 = perm.get(i);
          int r2 = perm.get(i + 1);

          expr.addTerm(1.0, grbVars[r2]);
          expr.addTerm(-1.0, grbVars[r1]);

          int thereshold = 2;
          GRBConstr constr = model.addConstr(expr, GRB.GREATER_EQUAL, thereshold,
              String.format("th(%d,%d)", r1, r2));
          double rhspen = 0.0;

//          constrs.add(constr);
//          rhspens.add(rhspen);
        }
      } else {
//        for (int i = 0; i < n; i++) {
//          for (int j = i + 1; j < n; j++) {
//            int r1 = perm.get(i);
//            int r2 = perm.get(j);
//            GRBVar diff = model.addVar(0.0, (int) INF, 0.0, GRB.CONTINUOUS,
//                String.format("diff_y(%d,%d)", r1, r2));
//
//            GRBLinExpr expr = new GRBLinExpr();
//            expr.addTerm(1.0, grbVars[r2]);
//            expr.addTerm(-1.0, grbVars[r1]);
//            model.addConstr(expr, GRB.EQUAL, diff, String.format("diff(%d,%d)", r1, r2));
//
//            GRBVar abs = model.addVar(0.0, (int) INF, 0.0, GRB.CONTINUOUS, String.format("abs_y(%d,%d)", r1, r2));
//            model.addGenConstrAbs(abs, diff, String.format("abs(%d,%d)", r1, r2));
//
//
//            int thereshold = 100;
//            model.addConstr(abs, GRB.GREATER_EQUAL, thereshold, String.format("th_abs(%d,%d)", r1, r2));
//
////          constrs.add(constr);
////          rhspens.add(rhspen);
//          }
//        }

      }

      // Add constraint: x + 2 y + 3 z <= 4
      long[] t = XFixed ? x : y;
      for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
          for (int k = j + 1; k < n; k++) {

            GRBLinExpr expr = new GRBLinExpr();

            expr.addTerm(t[j], grbVars[i]);
            expr.addTerm(-t[i], grbVars[j]);

            expr.addTerm(t[i], grbVars[k]);
            expr.addTerm(-t[k], grbVars[i]);

            expr.addTerm(t[k], grbVars[j]);
            expr.addTerm(-t[j], grbVars[k]);

            char sense =
                orientations.getOrderType(i, j, k) ^ XFixed ? GRB.GREATER_EQUAL : GRB.LESS_EQUAL;
            double rhs = orientations.getOrderType(i, j, k) ^ XFixed ? EPSILON : -EPSILON;

            GRBConstr constr = model.addConstr(expr, sense, rhs, String.format("ccw_points_(%d,%d,%d)", i, j, k));
            double value = x[i] * y[j] - x[j] * y[i]
                - x[i] * y[k] + x[k] * y[i]
                + x[j] * y[k] - x[k] * y[j];

            double rhspen = DoubleEpsilonCompare.sign(value) == orientations.getOrientations(i, j, k) ? 100.0 : 1.0;

            constrs.add(constr);
            rhspens.add(rhspen);
          }
        }
      }

//      model.feasRelax(GRB.FEASRELAX_CARDINALITY, false, false, true);
      double[] rhspensArr = new double[rhspens.size()];
      for (int i = 0; i < rhspens.size(); i++) rhspensArr[i] = rhspens.get(i);
      model.feasRelax(GRB.FEASRELAX_CARDINALITY, false, null, null, null, constrs.toArray(new GRBConstr[0]), rhspensArr);


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
