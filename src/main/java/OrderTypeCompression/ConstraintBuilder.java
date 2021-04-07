package OrderTypeCompression;

import gurobi.GRB;
import gurobi.GRB.Status;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class ConstraintBuilder {

  public static void orderConstraints(GRBModel model, GRBVar[] grbVars) throws GRBException {
    int n = grbVars.length;
    for (int i = 0; i < n - 1; i++) {
      GRBLinExpr expr = new GRBLinExpr();

      expr.addTerm(1.0, grbVars[i]);
      expr.addTerm(-1.0, grbVars[i + 1]);

      double threshold = 2.0;
      GRBConstr constr = model.addConstr(expr, GRB.GREATER_EQUAL, threshold, String.format("order(%d,%d)", i, i + 1));
    }
  }

  public static void orientationsConstraints(GRBModel model, GRBVar[] grbVars, long[] t,
      Orientations orientations, boolean XFixed, double EPSILON) throws GRBException {
    int n = grbVars.length;
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

//            GRBVar slack = model.addVar(0, INF, 1.0, GRB.CONTINUOUS,
//                String.format("slack[%d,%d,%d]", i, j, k));
//            int slackCoeff = orderType[i][j][k] ^ XFixed ? +1 : -1;
//            slackVars.add(slack);
//
//            expr.addTerm(slackCoeff, slack);

          char sense =
              orientations.getOrderType(i, j, k) ^ XFixed ? GRB.GREATER_EQUAL : GRB.LESS_EQUAL;
          double rhs = orientations.getOrderType(i, j, k) ^ XFixed ? EPSILON : -EPSILON;

          model.addConstr(expr, sense, rhs, String.format("ccw_points_(%d,%d,%d)", i, j, k));
        }
      }
    }

  }


  public static void debugSolution(int optimizationStatus, boolean OUTPUT_FLAG) {

    if (optimizationStatus == GRB.Status.OPTIMAL) {
      if (OUTPUT_FLAG) {
        System.out.println("Model was solved to optimality (subject to tolerances), "
            + "and an optimal solution is available.");
      }
    } else if (optimizationStatus == GRB.Status.INF_OR_UNBD) {
      if (OUTPUT_FLAG) {
        System.out.println("Model is infeasible or unbounded");
      }
    } else if (optimizationStatus == GRB.Status.INFEASIBLE) {
      if (OUTPUT_FLAG) {
        System.out.println("Model is infeasible");
      }
    } else if (optimizationStatus == GRB.Status.UNBOUNDED) {
      if (OUTPUT_FLAG) {
        System.out.println("Model is unbounded");
      }
    } else if (optimizationStatus == Status.SUBOPTIMAL) {
      if (OUTPUT_FLAG) {
        System.out.println("Unable to satisfy optimality tolerances; "
            + "a sub-optimal solution is available.");
      }
    } else {
      if (OUTPUT_FLAG) {
        System.out.println("Optimization was stopped with status = " + optimizationStatus);
      }
    }
  }

}
