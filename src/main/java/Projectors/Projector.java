package Projectors;

import org.ojalgo.optimisation.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Projector {

//    public static void main(String[] args) throws ContradictionException {
//        Loader.loadNativeLibraries();
//        // Create the linear solver with the GLOP backend.
//        MPSolver solver = MPSolver.createSolver("GLOP");
//        if (solver == null) {
//            System.out.println("Could not create solver GLOP");
//            return;
//        }
//
//        // Create the variables x and y.
//        MPVariable x = solver.makeNumVar(0.0, 1.0, "x");
//        MPVariable y = solver.makeNumVar(0.0, 2.0, "y");
//
////        System.out.println();
//        System.out.println("Number of variables = " + solver.numVariables());
//    }

   private ExpressionsBasedModel model;
   private Expression objective;
   private Expression constraint;
   private Variable[] x;
   private int[][] A;
   private int[] b;
   private int m;
   private double[] x_P;
    // minimize ||x - x_0||^2
    // s.t.  side * ⟨a, x⟩ <= side * b
    //                   x >= 0
    public Projector(int m, int[][] A, int[] b) {
        Logger.getLogger("org.ojalgo").setLevel(Level.OFF);
        this.model = new ExpressionsBasedModel();
        this.x = new Variable[m];
        this.m = m;
        this.A = A;
        this.b = b;
        this.x_P = new double[m];
        this.objective = model.addExpression("objective");
        for (int i = 0; i < A.length; i++) {
            x[i] = model.addVariable("x["+i+"]").lower(0);
            objective.set(x[i], x[i], 1.0); //x_i ^2
//            objective.set(x[i], -2.0 * x0[i]); // -2 x0_i * x_i
        }
        this.constraint = model.addExpression("constraint");

    }

    public double[] P_C(int side, int idx, double[] x0) {
        int coeff = 1;
        if(side == 1) {
            coeff = -1;
        }
        for(int i = 0; i < m; i++) {
            objective.set(x[i], -2.0 * x0[i]); // -2 x0_i * x_i
        }

//        Expression constraint = model.addExpression("constraint").upper(coeff*b[idx]);
        constraint.upper(coeff*b[idx]);
        for(int i = 0; i < m; i++) {
            constraint.set(x[i], coeff * A[i][idx]);
        }


        objective.weight(1.0);

        Optimisation.Result result = model.minimise();
        for(int i = 0; i < m; i++) {
            x_P[i] = x[i].getValue().doubleValue();
        }
        return  x_P;
    }

//public static void main(String[] args) {
//    int[] A = new int[] {3, 2};
//    int b = 20;
//    int m = A.length;
//    double[] x0 = new double[] {6.5, -0.75};
//    int side = 0;
//
//    ExpressionsBasedModel model = new ExpressionsBasedModel();
//    // minimize ||x - x_0||^2
//    // s.t.  side * ⟨a, x⟩ <= side * b
//    //                   x >= 0
//
//    Variable[] x = new Variable[A.length];
//    Expression objective = model.addExpression("objective");
//    for (int i = 0; i < A.length; i++) {
//        x[i] = model.addVariable("x["+i+"]").lower(0);
//        objective.set(x[i], x[i], 1.0); //x_i ^2
//        objective.set(x[i], -2.0 * x0[i]); // -2 x0_i * x_i
//    }
//    double coeff = -1.0;
//    if(side == 1) {
//        coeff = 1.0;
//    }
//    Expression constraint = model.addExpression("constraint").upper(coeff*b);
//    for(int i = 0; i < m; i++) {
//        constraint.set(x[i], coeff * A[i]);
//    }
//
//
//    objective.weight(1.0);
//
//    Optimisation.Result result = model.minimise();
//
//    for (int i = 0; i < A.length; i++) {
//        System.out.println("x["+i+"] = "+x[i].getValue());
//    }
//}
//
//
////    public static void main(String[] args) throws ContradictionException {
////        int[] A = new int[] {1, 4};
////        int b = 10;
////        int m = A.length;
////        Projection p = new Projection(m);
////        double[] l_0 = new double[] {4.0, -0.75};
////        int side = 1;
////        double[] l_1 = p.P_C(side, A, b, l_0);
////        System.out.print("P_C(l_0) = [");
////        for(int i = 0; i < m; i++) {
////            System.out.print(l_1[i]+", ");
////        }
////        System.out.println("]");
////
////    }
//


}

