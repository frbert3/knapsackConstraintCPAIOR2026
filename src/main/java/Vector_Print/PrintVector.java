package Vector_Print;

public class PrintVector {
    public String print_int_Vector(int[] v) {
        String res = "";
        for (int i = 0; i < v.length; i++) {
            res += v[i] + " ";
        }
        return res;
    }

    public String print_int_mat(int[][] v) {
        String res = "";
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                res += v[i][j] + " ";
            }
            if (i != v.length - 1) {
                res += "\n";
            }

        }
        return res;
    }
}
