To run the experiments of the paper run 

 mvn exec:java -Dexec.args="PROBLEM INSTANCE METHOD MaxItX THRESHOLD MinItX TIMEOUT" > output.txt



Valid entries for PROBLEM

 beasley ChuAndBeasley Petersen UFLP

 
 
Valid entries for INSTANCE 

 if PROBLEM = beasley then mknap2_i.txt where i = 0 .. 47
 
 if PROBLEM = ChuAndBeasley then i_j_k.txt where i = 5 10 30, j = 100 250 500, k = 0 .. 29
 
 if PROBLEM = Petersen then mknap2_i.txt where i = 1 2 3 4 5 6 7
 
 if PROBLEM = UFLP then pb_i.txt where i = 0 .. 23 


 
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
 
