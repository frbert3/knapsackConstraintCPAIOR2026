package Propagators;

import DataStructure.BipartiteSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.Locale;

public class PropKP2StepsWLPU extends Propagator<IntVar> {

    private BoolVar[] X; // Items.

    private IntVar P; // Profit variable.

    private int[] v; // Value vector, v[i] is the value of item i.

    private int[] c; // Capacity of KP, c[j] capacity in ressource j.

//    private int[][] W; // Weight, W[j][i] is the weight of item i in ressource j.

    private int n; // number of items.

    private int m; // number of ressources.

    private double V; // Relaxed upper bound on P.

    private double best_V; // Current lowest value of V.

    private double[] lag; // Lagrange multipliers

    private double[] prev_lag; // Lagrange multipliers of a previous iteration.

    private double[] best_lag; // Lagrange multipliers that generates the lowest value of V

    private double[] rc; // Reduced cost vector |v⟩-W^T|λ⟩

    private int[] x; // LP variables representation of X.

    private double[] grad;

    private boolean derivable;
    private boolean derivable_best;

    private BipartiteSet not_derivable;
    private BipartiteSet not_derivable_best;

    private BipartiteSet[] lagrangian_forbidden_value;

    private int iter;
    private int number_of_no_derivable;

    private int max_len_val_in_LF;

    private int number_of_variables_with_forbiddem_val;

    private double grad_norm;

    private boolean is_grad_ok;
    private boolean is_grad_ok_at_best;

    private final double[] saved_normed;
    private double[] saved_lag;
    private double[] saved_rc;
    private double[] best_rc;
    private double[] saved_grad;
    private int[] saved_x;
    private int[] best_x;

    private int initial_LB;


    private int best_int_v_grad;

    private int[] x_fixed;

    private BipartiteSet not_fixed_variables_idx;

    private int max_v;

    private double threshold;

    private double ZERO;

    private int NOS;

    private double[] sg_q;

    public int nsteps;

    public int nmb_of_algo_throw;

    public int nmb_of_algo_filter;


    private double coef;

    private int N;
    private int M;

    private int Idx;
    private int Jdx;






    public PropKP2StepsWLPU(BoolVar[] X, IntVar P, int[] v, int[] c, int N, int M, double threshold, int NOS, int nsteps){
        super(ArrayUtils.append(X, new IntVar[]{P}), PropagatorPriority.VERY_SLOW, false);
        this.X = X;
        this.P = P;
        this.v = v;
        this.c = c;
        this.N = N;
        this.M = M;
        this.n = N * M + N;
        this.m = N * M + M;
        this.V = P.getUB();
        this.best_V = Arrays.stream(v).sum();
        this.lag = new double[m];
        this.prev_lag = new double[m];
        this.best_lag = new double[m];
        this.saved_lag = new double[m];
        Arrays.fill(best_lag,0.0);
        this.rc = new double[n];
        this.best_rc = new double[n];
        this.saved_rc = new double[n];
        this.x = new int[n];
        this.best_x = new int[n];
        this.saved_x = new int[n];
        this.x_fixed = new int[n];
        this.grad = new double[m];
        this.saved_grad = new double[m];
        this.sg_q = new double[m];
        this.derivable = true;
        this.not_derivable = new BipartiteSet(n);
        this.not_derivable_best = new BipartiteSet(n);
        this.derivable_best =true;
        this.not_derivable.clear();
        this.not_derivable_best.clear();
        this.lagrangian_forbidden_value = new BipartiteSet[n];
        for(int i = 0; i < n; i++){
            lagrangian_forbidden_value[i] = new BipartiteSet(2);
            lagrangian_forbidden_value[i].clear();
        }
        this.iter =-1;
        this.number_of_no_derivable = 0;
        this.max_len_val_in_LF = 0;
        this.number_of_variables_with_forbiddem_val=0;
        this.grad_norm=1e-5;
        this.is_grad_ok = false;
        this.is_grad_ok_at_best=false;
        this.saved_normed = new double[n];
        int w_loc;
        this.max_v=v[0];
        for(int i=0; i<n; i++){
            if(max_v<v[i]) {
                max_v = v[i];
            }
        }
        this.initial_LB = P.getLB();
        this.best_int_v_grad=0;

        this.not_fixed_variables_idx = new BipartiteSet(n);
        not_fixed_variables_idx.clear();

        this.threshold = threshold;
        this.NOS = NOS;
        this.ZERO=1e-4;

        this.nsteps = nsteps;
        this.nmb_of_algo_throw=0;
        this.coef = 1.0;

        this.Idx =0;
        this.Jdx=0;
    }

    @Override
    public void propagate(int q) throws ContradictionException {

        try {
//            if(iter == 4360) {
//                for(int j=0; j<n; j++){
//                    System.out.println(X[j]);
//                }
//                System.out.println(P);
//            }

//            Arrays.fill(lag, 0.0);
            System.arraycopy(best_lag,0,lag,0,m);
            not_fixed_variables_idx.clear();
            number_of_variables_with_forbiddem_val = 0;
            coef=1.0;
            for (int i = 0; i < n; i++) {
                lagrangian_forbidden_value[i].clear();
                if (X[i].isInstantiated()) {
                    x[i] = X[i].getValue();
                    number_of_variables_with_forbiddem_val++; // because it is fixed, it is like the other val is forb.
                }
                else {
                    not_fixed_variables_idx.add(i);
                }
            }
            max_len_val_in_LF = 0;
            double prev_V;
            boolean go_on = true;
            int pre_val;
            int nmb_same_sol = 0;
            int k = 0;
            int big_K = 10;
            int ix;
            boolean is_it_the_same_x;
            while (k < 1000 && go_on) {
                iter++;
                compute_RC();
                solve_sub_problem(ZERO);
                compute_V();
                gather_filtering();
                compute_gradient();
                saved_for_best();
                go_on = update_lag(k, big_K, 5.0, 0.60);
//                if((38<=iter) &&(iter<=38)) {
////                    System.out.println(iter+", "+k+", "+go_on);
//                    System.out.println(iter+", "+k+", "+V+", "+go_on);
////                    for(int ij =0; ij<n; ij++){
////                        System.out.println(X[ij]);
////                    }
//                    System.out.print("λ = ");
//                    print_double_vector(lag);
//                    System.out.print("q = ");
//                    print_double_vector(rc);
//                    System.out.print("x = ");
//                    print_int_vector(x);
//                    System.out.print("g = ");
//                    print_double_vector(grad);
//                    System.out.println(iter+", "+k+", "+V);
//
//                }
                k++;
            }
//            if(( 4360-2 <iter)&& (iter< 4360+2)) {
//                System.out.println("Iteration "+iter);
//                System.out.println("V "+V);
//            }
//            int nmb_of_variables_to_fix = 0;
//            for (int i = 0; i < n; i++) {
//                if (lagrangian_forbidden_value[i].size() > 0) {
//                    nmb_of_variables_to_fix++;
//                }
//            }
//            if(iter == 7106) {
//                System.out.println(iter+", "+nmb_of_variables_to_fix);
//            }
            if (NOS > 0) {
                local_alteration();

            }
            int nmb_of_variables_to_fix = 0;
            for (int i = 0; i < n; i++) {
                if (lagrangian_forbidden_value[i].size() > 0) {
                    nmb_of_variables_to_fix++;
                }
            }
//            if(iter == 7106) {
//                System.out.println(iter+", "+nmb_of_variables_to_fix);
//            }
//            System.out.println(iter+", "+nmb_of_variables_to_fix);
//            System.out.println(iter);
//            for (int i = 0; i < n; i++) {
//                System.out.println(lagrangian_forbidden_value[i].pretty());
//            }
            apply_filtering();
//            System.out.println(iter+", "+V);
//            if(( 4360-3 <iter)&& (iter< 4360+3)) {
//                System.out.println("Hello "+iter);
//            }
            if (trigger_fail()) {
//                System.out.println("Hello "+iter);
                fails();
            }
//            if(( 4360-3 <iter)&& (iter< 4360+3)) {
//                System.out.println("Bye "+iter);
//            }
        }
        finally {
        }

    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }


    private void apply_filtering() throws ContradictionException {
        for(int i = 0; i < n; i++) {
            for(int b = 0; b <= lagrangian_forbidden_value[i].last; b++){
                X[i].removeValue(lagrangian_forbidden_value[i].list[b],this);
            }
        }

        P.updateUpperBound((int) Math.min(Math.floor(V+ZERO), P.getUB()), this);
//        System.out.println(iter+", "+P);
    }

    private boolean trigger_fail() {
        boolean failure = false;
        int number_of_fix_var = 0;
        int fix_value_of_KP = 0;
        for (int i = 0; i < n; i++) {
            if (X[i].isInstantiated()) {
                number_of_fix_var++;
                if (X[i].isInstantiatedTo(1)) {
                    fix_value_of_KP += v[i];
                }
            }
        }

        boolean violation_of_constraint = false;
        int j = 0;
//        int i;
        int weights0;
        int weights1;
        while (j < m && !violation_of_constraint) {
//            i=0;
            weights0=0;
            weights1=0;
            if (j<M) {
                for (int s = 0; s < N; s++) {
                    weights0 -= X[s*M +j].getUB();
                    weights1 -= X[s*M +j].getLB();
                }
            }
            else {
                Jdx = (j-M)%M;
                Idx = ((j-M) - Jdx) / M;
                weights0 = -X[(N*M)+Idx].getUB()+X[(Idx*M)+Jdx].getLB();
                weights1 = -X[(N*M)+Idx].getLB()+X[(Idx*M)+Jdx].getUB();
//                if(iter==3606&& j==22) {
//                    System.out.println("-X["+((Idx*M)+Jdx)+"].getUB()+X["+(N*M+Idx)+"].getLB() = -"+X[((Idx*M)+Jdx)].getUB()+" + "+X[((N*M)+Idx)].getLB());
//                    System.out.println("-X["+((Idx*M)+Jdx)+"].getLB()+X["+(N*M+Idx)+"].getUB() = -"+X[((Idx*M)+Jdx)].getLB()+" + "+X[((N*M)+Idx)].getUB());
//                }
            }
//            while (i < n ) {
//
//                weights0 += Math.min(W[j][i] * X[i].getLB(), W[j][i] * X[i].getUB());
//                weights1 += Math.max(W[j][i] * X[i].getLB(), W[j][i] * X[i].getUB());
//                i++;
//            }
            violation_of_constraint = (weights0 > c[j]) ;//&& (weights1 > c[j]) ;//;
//            if(iter==3606) {
//                System.out.println("weights0 = "+weights0+", c["+j+"] = "+c[j]);
//                System.out.println("weights1 = "+weights1+", c["+j+"] = "+c[j]);
//                System.out.println("violation_of_constraint  "+j+"   : "+ (violation_of_constraint));
//            }
//            violation_of_constraint = (weights0 > c[j]) && (weights1 > c[j]) ;//;
            // if the weights are all negative and c[j] is negative and the var are not fixed,
            // then it could trigger a faillure for no reason.
            j++;
        }
        failure = ((((fix_value_of_KP < P.getLB()) || (fix_value_of_KP > P.getUB())) && (number_of_fix_var == n)) || (violation_of_constraint) );
//        if(( 3606-2 <iter)&& (iter< 3606+2)) {
//            System.out.println("fix_value_of_KP < P.getLB() : "+ (fix_value_of_KP < P.getLB()));
//            System.out.println("fix_value_of_KP > P.getUB() : "+ (fix_value_of_KP > P.getUB()));
//            System.out.println("number_of_fix_var == n      : "+ (number_of_fix_var == n));
//            System.out.println("violation_of_constraint     : "+ (violation_of_constraint));
//        }
        return failure;
    }
    private void local_alteration() {
        int k_prime;
        int K_prime = NOS;
        double saved_V = V;
        saved_sate();
        boolean goOn_local;
        double ratio = threshold;
        double dotprod=0.0;
        double new_norm=0.0;
        double coef;
        double rci;
        boolean i_smaller_than_nm;
        for(int i=0; i < n; i++) {
            if((!X[i].isInstantiated() && lagrangian_forbidden_value[i].size()==0)) {
                coef = 30.0;
                int l=0;
                i_smaller_than_nm = i < N*M;
                while (l<=nsteps && ((V - Math.abs(rc[i]))>=P.getLB())) {
//                    System.out.println("Hello");
                    rci=0.0;
                    compute_gradient();
                    coef *= 0.5;
                    if(i_smaller_than_nm) {
                        grad[i%M] -= Math.signum(rc[i]);
                        grad[i+M] += Math.signum(rc[i]);
                    }
                    else{
                        for(int s=0; s<M; s++) {
                            grad[((i - (N * M) + 1)* M )+ s] -= Math.signum(rc[i]);
                        }
                    }

                    for(int j = 0; j < m; j++) {
                        lag[j] =  Math.max(0.0,lag[j] - coef * grad[j]); // here it is a '-' bc we search a minimum.
//                        rci += W[j][i] * lag[j];
                    }
                    if (i < N*M) {
                        Jdx = i % M;
                        Idx = (int) (i - Jdx) / M;
                        rci = v[i] + lag[Jdx] - lag[i + M];
                    }
                    else{
                        for(int s = 0; s < M+0; s++) {
                            rci  += lag[((i - (N * M) + 1)* M )+ s];
                        }
                        rci += v[i];
                    }
//                    rci = v[i] - rci;
                    if(Math.signum(rci)!= Math.signum(rc[i])) {
                        if(i_smaller_than_nm) {
                            lag[i%M] = Math.max(0.0,lag[i%M] - ((rci + 0.05 * Math.signum(rci)) /2.0));
                            lag[i+M] = Math.max(0.0,lag[i+M] + ((rci + 0.05 * Math.signum(rci)) /2.0));
//                            grad[i%M] -= Math.signum(rc[i]);
//                            grad[i+M] += Math.signum(rc[i]);
                        }
                        else{
                            for(int s=0; s<M; s++) {
                                lag[((i - (N * M) + 1)* M )+ s] = Math.max(0.0,lag[((i - (N * M) + 1)* M )+ s] + ((rci + 0.05 * Math.signum(rci)) /M));
//                                grad[M+i-N*M+s] -= Math.signum(rc[i]);
                            }
                        }
//                        for(int j = 0; j < m; j++) {
//                            lag[j] =  Math.max(0.0,lag[j] + ((rci + 0.05 * Math.signum(rci)) * W[j][i]/column_normed_sqared[i]));
//                        }
                    }
                    compute_RC();
                    solve_sub_problem(1e-3);
                    compute_V();
                    l++;
                }


                k_prime = 0;
                if ((V - P.getLB()) - Math.abs(rc[i]) <= ratio) {
//                    if(iter == 7106) {
//                        System.out.println(i);
//                    }
                    nmb_of_algo_throw++;

                    goOn_local = true;

                    while (k_prime < K_prime && goOn_local) {
                        compute_RC();
                        solve_sub_problem(1e-3);
                        compute_V();
                        do_filtering(i);
                        compute_gradient();
                        if(i_smaller_than_nm) {
                            grad[i%M] -= Math.signum(rc[i]);
                            grad[i+M] += Math.signum(rc[i]);
                        }
                        else{
                            for(int s=0; s<M; s++) {
                                grad[((i - (N * M) + 1)* M )+ s] -= Math.signum(rc[i]);
//                                if(k_prime==1&& iter==7106&&(i==20 || i==22)) {
//                                    System.out.println("grad["+(M+i-N*M+s+(i-N*M)*N)+"] -= Math.signum(rc["+i+"]) := "+Math.signum(rc[i]));
//                                }
                            }
                        }
//                        if(k_prime==1&& iter==7106&&(i==20 || i==22)) {
//                            System.out.print(k_prime + " g = ");
//                            print_double_vector(grad);
//                        }
//                        for (int j = 0; j < m; j++) {
//                            grad[j] += Math.signum(rc[i]) * W[j][i];
//                        }
                        goOn_local = (lagrangian_forbidden_value[i].size() == 0) && update_lag(k_prime, 5, 15.0, 0.60);
                        k_prime++;
                    }
                    if(lagrangian_forbidden_value[i].size() ==1) {
                        nmb_of_algo_filter++;
//                            System.out.println("Detected Missed filtering for X["+i+"] at iteration " + iter);
//                            System.out.print("filtering_lag = ");
//                            print_double_vector(lag);
//                            System.out.println("V = "+V+" rc["+i+"] = "+rc[i]+" varLB = "+P.getLB());
                    }
//                        else {
//                            System.out.println("Wrong throw for X["+i+"] at iteration " + iter);
//                        }
                }
                V = saved_V;
                restore_sate();
            }
        }
    }


    private void compute_RC(){
        Arrays.fill(rc, 0.0);
        for(int i = 0; i < n; i++) {
            if (i < N*M) {
                Jdx = i % M;
                Idx = (int) (i - Jdx) / M;
                rc[i] = v[i] + lag[Jdx] - lag[i + M];
//                if(iter==38) {
//                    System.out.println("rc["+i+"]  = v["+i+"] + λ["+Jdx+"] - λ["+(i+M)+"]");
//                }
            }
            else{
//                if(iter==38) {
//                    System.out.print("rc["+i+"]  += ");
//                }
                for(int s = 0; s < M+0; s++) {
//                    if(iter==38) {
////                        System.out.print(" λ [" + (i - (N * M) + M + s + ((i - (N * M)) * N)) + "] +");
//                        System.out.print(" λ [" + ((i - (N * M) + 1)* M + s) + "] +");
//                    }
                    rc[i]  += lag[((i - (N * M) + 1)* M )+ s];
                }
//                if(iter==38) {
//                    System.out.println("+v[" + i + "]");
//                }
                rc[i] += v[i];
            }
        }
    }


    private void solve_sub_problem(double precision){
        not_derivable.clear();
        derivable = true;
        boolean test_derivable;
        for(int i = 0; i < n; i++){
            x[i] = 0;
            test_derivable =  rc[i] * rc[i] > precision;
            if(!test_derivable) {
                derivable = false;
                not_derivable.add(i);
            }
            if(X[i].isInstantiated()) {
                x[i] = X[i].getValue();
            }
            else {
                if(rc[i] > 0) {
                    x[i] = 1;
                }
                else {
                    x[i] = 0;
                }
            }
        }
        if(!derivable) number_of_no_derivable++;
    }

    private void compute_gradient(){
        grad_norm =0.0;
        is_grad_ok = true;
        Arrays.fill(grad,0.0);
        for(int k = 0; k < m; k++) {
            if(k < M) {
                for(int s = 0; s < N; s++) {
                    grad[k] += x[(s * M) + k];
                }
                grad[k]+= -1;
            }
            else {
                Jdx = (k-M) % M;
                Idx = (int) (k - M - Jdx) / M;
                grad[k] = -x[(Idx * M) + Jdx] + x[(N * M) + Idx];
            }
            grad_norm += grad[k] * grad[k];
            is_grad_ok &= grad[k] >=0.0;
        }
//        for(int j = 0; j < m; j++){
//            for(int i = 0; i < n; i++){
//                grad[j] += W[j][i] * x[i];
//            }
//            grad[j] = c[j] - grad[j];
//            is_grad_ok &= grad[j] >=0.0;
//            grad_norm += grad[j] * grad[j];
//        }

    }

    private boolean update_lag(int k, int K, double mu0, double r){
        int val_to_compare = P.getLB();
        if(is_grad_ok_at_best) {
            val_to_compare = Math.max(val_to_compare, best_int_v_grad);
        }

        if((k > 0) &&  (k % K == 0)) {
            coef = 2.0 * coef;
        }
        double mu = ((mu0 / coef) * (V-P.getLB())) / (Math.max(grad_norm, 1e-5));
//        if(( 38-3 <iter)&& (iter< 38+3)) {
////                    System.out.println(iter+", "+k+", "+go_on);
//            System.out.println(iter+", "+k+", "+V);
//            System.out.println("μ = "+mu);
//
//        }
        double max_diff = 0.0;
        double L2diff = 0.0;
        System.arraycopy(lag,0,prev_lag,0,m);
        for(int j = 0; j < m; j++) {
            lag[j] =  lag[j] - mu * grad[j]; // here it is a '-' bc we search a minimum.
        }
        for(int j = 0; j < m; j++) {
            lag[j] =  Math.max(0.0,lag[j]); // valid relaxation
            L2diff += Math.pow(prev_lag[j]-lag[j],2.0);
            if(max_diff < Math.abs(prev_lag[j]-lag[j])){
                max_diff = Math.abs(prev_lag[j]-lag[j]);
            }
        }
        return (max_diff > 1e-5)  ;
    }



    private void compute_V(){
        double cste1 = 0.0;
        for(int i = 0; i < n; i++) {
            cste1 += rc[i] * x[i];
        }
        double cste2 = 0.0;
        for(int j = 0; j < M; j++) {
            cste2 -= lag[j];
        }
//        if(( 4360-2 <iter)&& (iter< 4360+2)) {
//            System.out.println(iter);
//            System.out.println("cste1 = "+cste1+" cste2 = "+cste2);
//            System.out.print("q = ");
//            print_double_vector(rc);
//            System.out.print("x = ");
//            print_int_vector(x);
//            System.out.println(X[6]);
//
//        }
        V = cste1 + cste2+ZERO;
    }

    private int compute_V_from_solution(){
        int Val = 0;
        for(int i = 0; i < n; i++){
            Val += v[i] * x[i];
        }
        return Val;
    }

    private void gather_filtering(){
        for(int i = 0; i < n; i++) {
            do_filtering(i);
        }
    }

    private void do_filtering(int i) {
        if((V - Math.abs(rc[i]) < P.getLB()) &&!X[i].isInstantiated()) {
            if(!lagrangian_forbidden_value[i].contain(1-x[i])) {
                lagrangian_forbidden_value[i].add(1-x[i]);
                if(lagrangian_forbidden_value[i].size()>max_len_val_in_LF){
                    max_len_val_in_LF = lagrangian_forbidden_value[i].size();
                }
            }
        }
    }

    private void saved_for_best(){
        if(V < best_V && V >= P.getLB()) {
            best_V = V;
            System.arraycopy(lag, 0, best_lag, 0, m);
            if(!is_grad_ok_at_best) {
                is_grad_ok_at_best=is_grad_ok;
            }
        }

        int val = compute_V_from_solution();

        if(is_grad_ok && val > best_int_v_grad) {
            best_int_v_grad = val;
            System.arraycopy(x,0,best_x,0,n);
        }

    }

    private void saved_sate(){
        System.arraycopy(rc,0, saved_rc,0,n);
        System.arraycopy(grad,0, saved_grad,0,m);
        System.arraycopy(x,0, saved_x,0,n);
        System.arraycopy(lag,0, saved_lag,0,m);

    }
    private void restore_sate(){
        System.arraycopy(saved_rc,0, rc,0,n);
        System.arraycopy(saved_grad,0, grad,0,m);
        System.arraycopy(saved_lag,0, lag,0,m);
        System.arraycopy(saved_x,0, x,0,n);
        System.arraycopy(saved_lag,0, lag,0,m);

    }


    private void print_double_vector(double[] vector) {
        Locale.setDefault(Locale.US);
        for(int i = 0; i < vector.length; i++) {
            if(vector[i]>=0) {
                System.out.printf("%6.5f, ", vector[i]);
            }
            else{
                System.out.printf("%5.5f, ", vector[i]);
            }

        }
        System.out.println();
    }

    private void print_int_vector(int[] vector) {
        for(int i = 0; i < vector.length; i++) {
            System.out.printf("%1d ", vector[i]);
        }
        System.out.println();
    }

    public void printForbidden(){
        for(int i = 0; i < n; i++){
            System.out.printf("%s  ", lagrangian_forbidden_value[i].pretty());
        }
        System.out.println(" ");
    }

    public String print_sol(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < n; i++){
            str.append(String.format("%d , ", x[i]));
        }
        return str.toString();
    }

    public void print_best_int_sol() {

        print_int_vector(best_x);
    }

    public int solution(int i){
        return x[i];
    }
    public int get_iter() {
        return iter;
    }
    public double getV(){
        return V;
    }
    public double getBest_V(){
        return best_V;
    }

    public int getBest_int_v_grad(){
        return best_int_v_grad;
    }

    public int getNumber_of_no_derivable() {
        return number_of_no_derivable;
    }

    public void print_X_var() {
        for(int i=0; i<n; i++){
//            System.out.println(X[i]+", " +minizinc_solution[i]);
            System.out.println(X[i]);
        }
    }
}
