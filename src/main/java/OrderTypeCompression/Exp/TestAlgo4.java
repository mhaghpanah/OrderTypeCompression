package OrderTypeCompression.Exp;
import edu.princeton.cs.algs4.*;

public class TestAlgo4 {


  public static void test(double[][] A, double[] b, double[] c) {

    LinearProgramming lp;
    try {
      lp = new LinearProgramming(A, b, c);
    }
    catch (ArithmeticException e) {
      System.out.println(e);
      return;
    }

    StdOut.println("value = " + lp.value());
    double[] x = lp.primal();
    for (int i = 0; i < x.length; i++)
      StdOut.println("x[" + i + "] = " + x[i]);
    double[] y = lp.dual();
    for (int j = 0; j < y.length; j++)
      StdOut.println("y[" + j + "] = " + y[j]);

  }

  public static void main(String[] args) {
    double[][] A = {
        { -1,  1,  0 },
        {  1,  4,  0 },
        {  2,  1,  0 },
        {  3, -4,  0 },
        {  0,  0,  1 },
    };
    double[] c = { 0, 0, 0 };
    double[] b = { 5, 45, 27, 24, 4 };

    test(A, b, c);
  }
}
