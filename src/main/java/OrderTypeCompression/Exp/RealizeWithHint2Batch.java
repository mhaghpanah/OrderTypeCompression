package OrderTypeCompression.Exp;

import OrderTypeCompression.ConstraintBuilder;
import OrderTypeCompression.DoubleEpsilonCompare;
import OrderTypeCompression.Orientations;
import OrderTypeCompression.Points;
import gurobi.GRB;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntParam;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.Arrays;

public class RealizeWithHint2Batch {

  public static final String name = "RealizeWithHint2Batch";
  private static final double INF = (1 << 16) - 1;
  private static final double EPSILON = 1.0 - 1e-1;
//  private static final double EPSILON = 0.1;
  private static final boolean OUTPUT_FLAG = false;
  private final int n;
  private final int b;
  private Orientations[] orientations;
  private final long[] x;
  private final long[][] y;

  private static final int[][] hints = new int[][] {{},{},{},{65535,32768,0},{65535,41158,24377,0},{65535,45125,32767,20410,0},{65535,47512,37298,28237,18023,0},{65535,49143,40229,32767,25306,16392,0},{65535,50344,42317,35835,29700,23218,15191,0},{65535,51265,43887,38082,32768,27453,21648,14270,0},{65535,51998,45119,39811,35064,30471,25724,20416,13537,0},{65535,52598,46118,41190,36860,32768,28675,24345,19417,12937,0},{65535,53111,46958,42330,38318,34589,30946,27217,23205,18577,12424,0}};

  GRBEnv env;
  GRBModel model;
  GRBVar[][] grbVars;
  GRBLinExpr[] exprs;
  char[] senses;
  double[] rhss;
  String[] names;

  public RealizeWithHint2Batch(int n, int b) {
    this.n = n;
    this.b = b;
    x = new long[n];
    y = new long[b][n];

    for (int i = 0; i < n; i++) {
      x[i] = hints[n][i];
    }

    initialize();

  }

  public RealizeWithHint2Batch(int n, int[] hint, int b) {
    assert n == hint.length;

    this.n = n;
    this.b = b;
    x = new long[n];
    y = new long[b][n];

    for (int i = 0; i < n; i++) {
      x[i] = hint[i];
    }

    initialize();
  }


  public void initialize() {
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

      model = new GRBModel(env);
      grbVars = new GRBVar[b][];

      for (int l = 0; l < b; l++) {
//              grbVars[l] = model.addVars(n, GRB.CONTINUOUS);
        double[] lb = new double[n];
        double[] up = new double[n];
        Arrays.fill(up, 1.0);
        double[] obj = new double[n];
        char[] type = new char[n];
        Arrays.fill(type, GRB.CONTINUOUS);
        String[] nameVars = new String[n];
        grbVars[l] = model.addVars(lb, up, obj, type, nameVars);
      }


      exprs = new GRBLinExpr[b * (n * (n - 1) * (n - 2)) / 6];
      senses = new char[b * (n * (n - 1) * (n - 2)) / 6];
      rhss = new double[b * (n * (n - 1) * (n - 2)) / 6];
      names = new String[b * (n * (n - 1) * (n - 2)) / 6];

      int index = 0;
      for (int l = 0; l < b; l++) {
        for (int i = 0; i < n; i++) {
          for (int j = i + 1; j < n; j++) {
            for (int k = j + 1; k < n; k++) {

              GRBLinExpr expr = new GRBLinExpr();

              expr.addTerm(x[j], grbVars[l][i]);
              expr.addTerm(-x[i], grbVars[l][j]);

              expr.addTerm(x[i], grbVars[l][k]);
              expr.addTerm(-x[k], grbVars[l][i]);

              expr.addTerm(x[k], grbVars[l][j]);
              expr.addTerm(-x[j], grbVars[l][k]);

              char sense = GRB.GREATER_EQUAL;
              double rhs = EPSILON;

              exprs[index] = expr;
              senses[index] = sense;
              rhss[index] = rhs;
              names[index] = "";

              index++;
//          model.addConstr(expr, sense, rhs, "");
//          model.addConstr(expr[i][j][k], sense, rhs, String.format("ccw_points_(%d,%d,%d)", i, j, k));
            }
          }
        }
      }
      model.addConstrs(exprs, senses, rhss, names);
//      System.out.println(exprs.length);
//      System.out.println(senses.length);
//      System.out.println(rhss.length);
//      System.out.println(names.length);
//      System.out.println("--------");
      model.presolve();
//      System.out.println(model.getVars().length);
//      System.out.println(model.getConstrs().length);

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
    }
  }

  public Points[] solve(Orientations[] orientations) {
    assert n == orientations[0].getN();
    assert b == orientations.length;
    this.orientations = orientations;
//    n = orientations.getN();
//    x = new long[n];
//    y = new long[n];
    Points[] ans = new Points[b];

    boolean XFixed = true;
    boolean solved = solve(XFixed);
    if (solved) {
      System.err.printf("%s: Solved!\n", name);
      for (int l = 0; l < b; l++) {
        ans[l] = new Points(x, y[l]);
      }
      return ans;
    } else {
      System.err.printf("%s: Not found Solutions!\n", name);
      return ans;
    }
  }

//  public boolean alternativeRealization(boolean XFixed) {
////    Arrays.fill(x, 0);
////    Arrays.fill(y, 0);
////
////    long[] t = XFixed ? x : y;
////    for (int i = 0; i < n; i++) {
////      t[i] = hints[n][i];
////    }
////
////    if (solve(XFixed)) {
////      System.err.printf("%s: Solved!\n", name);
////      return true;
////    }
////
////    return false;
//  }

  private boolean solve(boolean XFixed) {

    boolean XVar = !XFixed;
    try {

      GRBConstr[] grbConstrs = model.getConstrs();
//      System.out.println(grbConstrs.length);
      int index = 0;
      for (int l = 0; l < b; l++) {
        for (int i = 0; i < n; i++) {
          for (int j = i + 1; j < n; j++) {
            for (int k = j + 1; k < n; k++) {
              char sense =
                  orientations[l].getOrderType(i, j, k) ^ XFixed ? GRB.GREATER_EQUAL : GRB.LESS_EQUAL;
              double rhs = orientations[l].getOrderType(i, j, k) ^ XFixed ? EPSILON : -EPSILON;

              grbConstrs[index].set(CharAttr.Sense, sense);
              grbConstrs[index].set(DoubleAttr.RHS, rhs);
//            System.out.println(index);
//            System.out.println(n);
              index++;
            }
          }
        }
      }
//      for (GRBConstr grbConstr : grbConstrs) model.remove(grbConstr);


//      Instant start;
//      Instant finish;
//
//      start = Instant.now();


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
//      GRBModel model = new GRBModel(env);

      // Create variables
//      GRBVar[] grbVars = new GRBVar[n];
////      String str = XVar ? "x" : "y";
//      for (int i = 0; i < n; i++) {
////        grbVars[i] = model.addVar(0, INF, 0.0, GRB.CONTINUOUS, String.format("%s[%d]", str, i));
//        grbVars[i] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "");
//      }


//      GRBVar[] grbVars = model.addVars(n, GRB.CONTINUOUS);

//      double[] lb = new double[n];
//      double[] up = new double[n];
//      Arrays.fill(up, 1.0);
//      double[] objs = new double[n];
//      char[] type = new char[n];
//      Arrays.fill(type, GRB.CONTINUOUS);
//      String[] names = new String[n];
//      GRBVar[] grbVars = model.addVars(lb, up, objs, type, names);


      // Add constraint: x + 2 y + 3 z <= 4
//      if (XVar) {
//        ConstraintBuilder.orderConstraints(model, grbVars);
//      }

//      long[] t = XFixed ? x : y;
//      ConstraintBuilder.quickOrientationsConstraints(model, n, exprs, orientations, XFixed, EPSILON);

//      ConstraintBuilder.orientationsConstraints(model, grbVars, t, orientations, XFixed, EPSILON);
//      model.feasRelax(GRB.FEASRELAX_QUADRATIC, false, false, true);

      if (OUTPUT_FLAG) {
        model.write(String.format("%s.mps", name));
        model.write(String.format("%s.lp", name));
      }

      // Optimize model
      model.optimize();

//      finish = Instant.now();
//      System.out
//          .printf("+++++++++++++++ Solved in %d microseconds time!\n", Duration.between(start, finish).toMillis());
//
//      System.out.println(Duration.between(start, finish));

      int optimizationStatus = model.get(GRB.IntAttr.Status);
      double objVal;
      boolean ans = true;

      System.out.println(optimizationStatus);
      if (optimizationStatus == GRB.Status.OPTIMAL) {
        if (OUTPUT_FLAG) {
          model.write(String.format("%s.sol", name));
          objVal = model.get(DoubleAttr.ObjVal);
          System.out.println("Optimal objective: " + objVal);
        }

        for (int l = 0; l < b; l++) {
          long[] t = XVar ? x : y[l];
          for (int i = 0; i < n; i++) {
            //todo fix it
            t[i] = DoubleEpsilonCompare.integrality(grbVars[l][i].get(DoubleAttr.X) * INF);
//          System.out.println(t[i]);
//
//          System.out.println(grbVars[i].get(GRB.DoubleAttr.X));
//          t[i] = DoubleEpsilonCompare.integrality(grbVars[i].get(GRB.DoubleAttr.X));
          }
//        System.out.println(Arrays.toString(t));
          ans &= orientations[l].isSameOrderType(new Points(x, y[l]));
        }
      }

      ConstraintBuilder.debugSolution(optimizationStatus, OUTPUT_FLAG);

      // Dispose of model and environment
//      model.dispose();
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
