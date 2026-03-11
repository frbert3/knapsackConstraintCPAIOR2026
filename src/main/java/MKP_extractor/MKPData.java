package MKP_extractor;

public class MKPData {
    protected int number_of_items;
    protected int number_of_knapsacks;

    protected int[] values;

    protected int[][] knapsacks;

    protected int[] capacities;

    protected boolean feasible;

    protected int optimal_sol;

    protected int best_known_sol;

    protected double lp_best_known;

    public MKPData(int number_of_items, int number_of_knapsacks) {
        this.number_of_items = number_of_items;
        this.number_of_knapsacks = number_of_knapsacks;
        this.values = new int[number_of_items];
        this.knapsacks = new int[number_of_knapsacks][number_of_items];
        this.capacities = new int[number_of_knapsacks];

    }

    public void setValue(int value, int item_index){
        assert item_index < number_of_items;
        values[item_index] = value;
    }
    public void setKnapsack_i_j(int weight_of_item_j_in_kp_i, int kp_i, int item_j){
        knapsacks[kp_i][item_j] = weight_of_item_j_in_kp_i;
    }

    public void setCapacity(int capacity_of_kp_i, int kp_i){
        capacities[kp_i] = capacity_of_kp_i;
    }

    public void setFeasible(boolean isFeasible){
        this.feasible=isFeasible;
    }

    public void setOptimal_sol(int optimal_val){
        this.optimal_sol = optimal_val;
    }

    public void setBest_known_sol(int best_known_val) {
        this.best_known_sol=best_known_val;
    }

    public void setLp_best_known(double lp_best_known_val){
        this.lp_best_known = lp_best_known_val;
    }

    public int getNumber_of_items(){return number_of_items;}
    public int getNumber_of_knapsacks(){return  number_of_knapsacks;}
    public int[] getValues(){
        int[] values_copy = new int[number_of_items];
        System.arraycopy(values,0, values_copy, 0, number_of_items);
        return values_copy;
    }

    public int[] getCapacities(){
        int[] capacities_copy = new int[number_of_knapsacks];
        System.arraycopy(capacities,0, capacities_copy, 0, number_of_knapsacks);
        return capacities_copy;
    }

    public int[][] getKnapsacks(){
        int[][] knapsacks_copy = new int[number_of_knapsacks][number_of_items];
        for(int i = 0; i < number_of_knapsacks; i++){
            System.arraycopy(knapsacks[i], 0, knapsacks_copy[i], 0, number_of_items);
        }
        return knapsacks_copy;
    }

    public boolean getFeasible(){return feasible;}
    public int getOptimal_sol(){return optimal_sol;}

    public int getBest_known_sol(){return best_known_sol;}
    public double getLp_best_known(){return lp_best_known;}

}
