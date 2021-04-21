\* Problem: myProblem *\

Maximize
 z: + 0.28 x12 + 1.69 x21

Subject To
 c1: + x12 = 1

Bounds
 0 <= x12 <= 1
 0 <= x21 <= 1

Generals
 x12
 x21

End

17 lines were written
GLPK Simplex Optimizer, v4.65
2 rows, 2 columns, 1 non-zero
      0: obj =  -0.000000000e+00 inf =   1.000e+00 (1)
      1: obj =   2.800000000e-01 inf =   0.000e+00 (0)
*     2: obj =   1.970000000e+00 inf =   0.000e+00 (0)
OPTIMAL LP SOLUTION FOUND
z = 1.97
x12 = 1.0
x21 = 1.0
