To run the experiments of the paper run 

 mvn exec:java -Dexec.args="PROBLEM INSTANCE METHOD MaxItX THRESHOLD MinItX TIMEOUT" > output.txt



Valid entries for PROBLEM

 beasley ChuAndBeasley Petersen FanceUK KuehnAndHamburger

 
 
Valid entries for INSTANCE 

 if PROBLEM = beasley then mknap2_i.txt where i = 0 .. 47
 
 if PROBLEM = ChuAndBeasley then i_j_k.txt where i = 5 10 30, j = 100 250 500, k = 0 .. 29
 
 if PROBLEM = Petersen then mknap2_i.txt where i = 1 2 3 4 5 6 7
 
 if PROBLEM = FanceUK then pb_i.txt where i = 0 .. 23 

 if PROBLEM = KuehnAndHamburger then cap71.txt, cap72.txt, cap73.txt, cap74.txt, cap101.txt, cap102.txt, cap103.txt, cap104.txt, cap131.txt, cap132.txt, cap133.txt, cap134.txt


 
Valid entries for METHOD

 steps Projection
 
steps is for Lagrangian method LR, LR+{0,1,2,3}

Projection is for methods PROJ+{1, 2, 3}



Valid entries for THRESHOLD

 avg or any floating value
 
Valid entries for TIMEOUT

 300s or any positive integer x followed directly by the character s.

 
To get the result of the paper

Propagator entries   :      METHOD MaxItX THRESHOLD MinItX TIMEOUT

  LR entries   :        steps -1 -1 -1 300s

  LR+0 entries   :          steps 60 avg 0 300s
  
  LR+k entries   :          steps 60 avg k 300s


 
The instances files


beasley mkap2_i.txt, ChuAndBeasley i_j_k.txt, Petersen mknap1_i.txt

blank line or Text line

number_of_items number_of_constraints integer_optimal_value best_known_integer_value  LP_OPTIMAL(if no integer optimal values are known)

value[0] value[1] ... value[number_of_items-1]

weight[0][0] weight[0][1] ... weight[0][number_of_items-1]

weight[1][0] weight[1][1] ... weight[1][number_of_items-1]

   ...           ...                    ...
   
weight[number_of_constraints-1][0] weight[number_of_constraints-1][1] ... weight[number_of_constraints-1][number_of_items-1]

capacity[0] capacity[1] ... capacity[number_of_constraints-1] 

blank line


For FranceUK and KuehnAndHamburger

Country Instance_index or text

number_of_facility number_of_city 0

1 facility_building_cost[1] distanceFacilityCity[1][1] ... distanceFacilityCity[1][number_of_city]

2 facility_building_cost[2] distanceFacilityCity[2][1] ... distanceFacilityCity[2][number_of_city]

... ... ...

number_of_facility facility_building_cost[number_of_facility] distanceFacilityCity[number_of_facility][1] ... distanceFacilityCity[number_of_facility][number_of_city]











