package MKP_extractor;

public class WLPUncapacitedData {
    protected int number_of_warehouses;
    protected int number_of_stores;
    protected int[][] A;
    protected int[]   b;
    protected int[]   c;


    public WLPUncapacitedData(int number_of_warehouses, int number_of_stores) {
        this.number_of_warehouses = number_of_warehouses; // n
        this.number_of_stores = number_of_stores; // m
        this.A = new int[number_of_warehouses * number_of_stores + number_of_stores][number_of_warehouses * number_of_stores + number_of_warehouses];
        this.b = new int[number_of_warehouses * number_of_stores + number_of_stores];
        this.c = new int[number_of_warehouses * number_of_stores + number_of_warehouses];
    }

//    public

    public void setCVector(int value, int ij) {
        assert ij < number_of_warehouses * number_of_stores + number_of_stores;
        c[ij] = value;
    }
    public void setA_i_j(int weight_of_item_j_in_kp_i, int kp_i, int item_j){
        A[kp_i][item_j] = weight_of_item_j_in_kp_i;
    }
    public void setB_vector(int capacity_of_kp_i, int kp_i){
        b[kp_i] = capacity_of_kp_i;
    }
    public int getNumber_of_stores(){return number_of_stores;}
    public int getNumber_of_warehouses(){return  number_of_warehouses;}
    public int[] getC_vector(){
        int[] c_copy = new int[c.length];
        System.arraycopy(c, 0, c_copy, 0, c.length);
        return c_copy;
    }
    //
    public int[] getCapacities(){
        int[] capacities_copy = new int[b.length];
        System.arraycopy(b,0, capacities_copy, 0, b.length);
        return capacities_copy;
    }

    //
    public int[][] getKnapsacks(){
        int[][] knapsacks_copy = new int[A.length][A[0].length];
        for(int i = 0; i < A.length; i++){
            System.arraycopy(A[i], 0, knapsacks_copy[i], 0, A[0].length);
        }
        return knapsacks_copy;
    }


}
