package org.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import MKP_extractor.MKPData;
import MKP_extractor.MKP_parseur;
import MKP_extractor.WLPUncapacitedData;
import MKP_extractor.WLPUncapacited_parseur;
import Propagators.PropKP2Steps;
import Propagators.PropKP2StepsFilteringBeforeLocal;
import Propagators.PropKP2StepsProjectionOnQi1Qi0;
import Propagators.PropKP2StepsWLPU;
import org.chocosolver.solver.Model;


import Vector_Print.*;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.Constraint;

public class Main {
    public static void main(String[] args) throws ContradictionException {
        Model model = new Model("Model");
        String pb_filename = "Benchmark/"+args[0]+"/"+args[1];
        MKPData mkp = null;
        WLPUncapacitedData wlp = null;
        int [] c = null;
        int[][] A = null;
        int[] b = null;
        int n;
        int m;
        int NumberOfSteps = Integer.parseInt(args[3]);
        double threshold;
        BoolVar[] X;
        IntVar P;
        Constraint constraint_mkp;
        PropKP2StepsWLPU propWLPU;
        PropKP2Steps prop2Steps;
        PropKP2StepsFilteringBeforeLocal prop2StepsFilteringBeforeLocal;
        PropKP2StepsProjectionOnQi1Qi0 propProjection;
        int obj = 0;
        if (args[0].equals("beasley") || args[0].equals("ChuAndBeasley") || args[0].equals("Petersen")) {
//            Model model
            mkp = MKP_parseur.loadMKP(pb_filename);
            c = mkp.getValues();
            A = mkp.getKnapsacks();
            b = mkp.getCapacities();
            n = mkp.getNumber_of_items();
            m = mkp.getNumber_of_knapsacks();
            if(args[4].equals("avg")) {
                threshold = Arrays.stream(c).average().getAsDouble();
            }
            else {
                threshold = Double.parseDouble(args[4]);
            }
            X = new BoolVar[n];
            for(int i = 0; i < n; i++){
                X[i] = model.boolVar("X["+i+"]");
            }
            P = model.intVar("Price", 0, Arrays.stream(c).sum());
            if(args[2].equals("Projection")) {
                propProjection = new PropKP2StepsProjectionOnQi1Qi0(X, P, c, b, A, threshold, NumberOfSteps, Integer.parseInt(args[5]));
                constraint_mkp = new Constraint("mkp", propProjection);
            }
            else {
                if(args[2].equals("stepsFiltering")) {
                    prop2StepsFilteringBeforeLocal = new PropKP2StepsFilteringBeforeLocal(X, P, c, b, A, threshold, NumberOfSteps, Integer.parseInt(args[5]));
                    constraint_mkp = new Constraint("mkp", prop2StepsFilteringBeforeLocal);
                }
                else {
                    prop2Steps = new PropKP2Steps(X, P, c, b, A, threshold, NumberOfSteps, Integer.parseInt(args[5]));
                    constraint_mkp = new Constraint("mkp", prop2Steps);
                }
            }
            constraint_mkp.post();
            model.setObjective(Model.MAXIMIZE, P);
            model.getSolver().limitTime(args[6]);
            if(true){
                Solution solution = new Solution(model);

                while(model.getSolver().solve()){
                    solution.record();
                }
                try{
                    if(model.getSolver().getSearchState()== SearchState.TERMINATED)solution.restore();
                }
                finally {
                }

            }
            else{
                model.getSolver().solve();
            }
            for(int i = 0; i < X.length; i++){
                obj += X[i].getValue() * c[i];
            }
        }
        else {
            wlp = WLPUncapacited_parseur.loadWarehouse(pb_filename);
            c = wlp.getC_vector();
            A = wlp.getKnapsacks();
            b = wlp.getCapacities();
            n = wlp.getNumber_of_warehouses();
            m = wlp.getNumber_of_stores();
            if(args[4].equals("avg")) {
                threshold = -1*Arrays.stream(c).average().getAsDouble();
            }
            else {
                threshold = Double.parseDouble(args[4]);
            }
            X = new BoolVar[m*n + n];
            for(int i = 0; i < m*n + n; i++){
                X[i] = model.boolVar("X["+i+"]");
            }
            P = model.intVar("Price", Arrays.stream(c).sum(), 0);
            propWLPU = new PropKP2StepsWLPU(X, P, c, b, n, m, threshold, NumberOfSteps, Integer.parseInt(args[5]));
            constraint_mkp = new Constraint("mkp", propWLPU);
            constraint_mkp.post();
            model.setObjective(Model.MAXIMIZE, P);
            model.getSolver().limitTime(args[6]);
            if(true) {
                Solution solution = new Solution(model);

                while (model.getSolver().solve()) {
                    solution.record();
                }
                try {
                    if (model.getSolver().getSearchState() == SearchState.TERMINATED) solution.restore();
                } finally {
                }
            }
            for(int i = 0; i < X.length; i++){
                obj -= X[i].getValue() * c[i];
            }

        }

        System.out.printf("%10s & %10s & %10s & %5s & %5s & %5s & %5s & %8.5f & %8d & %8d & %8d & %8d & %8d & %8s & %8s", args[0], args[1], args[2], args[3], args[4], args[5], args[6], model.getSolver().getTimeCount(), model.getSolver().getNodeCount(), model.getSolver().getFailCount(), model.getSolver().getBackTrackCount(), P.getValue(), obj, model.getSolver().getSearchState(), model.getSolver().isFeasible());
        System.out.println();

//        PrintVector pv = new PrintVector();

//        System.out.println(pv.print_int_Vector(c));
//        System.out.println(pv.print_int_mat(A));
//        System.out.println(pv.print_int_Vector(b));
    }
}

// args : benchmark pb_name.extension model NumberOfSteps threshold additionnalSteps timeout
// threshold can be either 'avg' or a double


// mvn exec:java -Dexec.args="beasley mknap2_36.txt steps 60 avg 2 300s" > output_beasley_36.txt