package MKP_extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class WLPUncapacited_parseur {
    public static WLPUncapacitedData loadWarehouse(String file){
        WLPUncapacitedData data = null;
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            br.readLine();
            Scanner scan = new Scanner(br.readLine());
            int number_of_warehouse = scan.nextInt();
            int number_of_stores = scan.nextInt();
            data = new WLPUncapacitedData(number_of_warehouse,number_of_stores);
            for(int i=0;i<number_of_stores;i++){
                for(int j=0;j<number_of_warehouse;j++){
                    data.setA_i_j(-1,i, j*number_of_stores +i);
                }
            }

            for(int i=0;i<number_of_stores*number_of_warehouse;i++){
                data.setA_i_j(1,i+number_of_stores, i);
                data.setA_i_j(-1, i+number_of_stores, number_of_stores*number_of_warehouse+Math.floorDiv(i, number_of_stores));
            }
            int k =0;
            for(int j=0;j<number_of_warehouse;j++){
                scan = new Scanner(br.readLine());
                scan.nextInt();
                data.setCVector( -1*scan.nextInt(),number_of_warehouse * number_of_stores +j);
                for(int i=0; i<number_of_stores;i++){
                    data.setCVector(-1*scan.nextInt(), k);
                    k++;
                }
            }
            for(int i=0;i<number_of_stores;i++){
                data.setB_vector(-1,i);
            }





        } catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }
}
