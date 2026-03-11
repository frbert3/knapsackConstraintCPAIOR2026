package MKP_extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class MKP_parseur {
    public static MKPData loadMKP(String file){
        MKPData data = null;
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            br.readLine();
            Scanner scan = new Scanner(br.readLine());
            int number_of_items = scan.nextInt();
            int number_of_knapsacks = scan.nextInt();
            data = new MKPData(number_of_items,number_of_knapsacks);
            data.setOptimal_sol(scan.nextInt());
            data.setFeasible((data.getOptimal_sol() !=0));
            data.setBest_known_sol(scan.nextInt());

            scan = new Scanner(br.readLine());

            for(int i = 0; i < number_of_items; i++){
                data.setValue(scan.nextInt(),i);
            }

            for(int i = 0; i < number_of_knapsacks;i++){
                scan = new Scanner(br.readLine());
                for(int j = 0; j < number_of_items; j++){
                    data.setKnapsack_i_j(scan.nextInt(), i, j);
                }
            }

            scan = new Scanner(br.readLine());

            for(int i = 0; i < number_of_knapsacks; i++){
                data.setCapacity(scan.nextInt(),i);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }
}
