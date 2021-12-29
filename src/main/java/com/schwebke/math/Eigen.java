package com.schwebke.math;

import static java.lang.Math.signum;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
  * Eigenvalue-Solver
  */
public class Eigen
{
   /**
     * delta:
     *    smallest positive number, for which
     *    holds 1.0 + delta != 1.0
     */
   public static double delta = 2.220446049250313E-16;

   /**
     * epsilon:
     *    desired absolute tolerance for eigenvalues
     */
   public static double eps = 1E-8;


   /**
     * Calculate the eigenvalues and eigenvectors of
     * the symetric matrix A with the cyclic Jacobian method.
     *
     * After the call the diagonal of A holds the eigenvalues
     * and the columns of the returned matrix V the corresponding
     * eigenvectors.
     */
   public static double[][] cyclJac(double A[][])
   {
      int n = A.length;
      if (n != A[0].length)
      {
	 throw new IllegalArgumentException("cannot solve for non-square matrix");
      }

      double V[][] = new double [n][n];
      for (int i = 0; i < n; ++i)
      {
	 for (int j = 0; j < n; ++j)
	 {
	    V[i][j] = 0.;
	 }
	 V[i][i] = 1.;
      }

      double sum = 1.;
      double prevSum;
      while (true)
      {
	 prevSum = sum;
	 sum = 0.;
	 for (int i = 0; i < n; ++i)
	 {
	    for (int j = 0; j <= i-1; ++j)
	    {
	       sum += A[i][j]*A[i][j];
	    }
	 }

	 if ( (2.*sum < eps*eps) || (abs(prevSum-sum) < 0.1*eps) )
	 {
	    return V;
	 }

	 for (int p = 0; p < n-1; ++p)
	 {
	    for (int q = p+1; q < n; ++q)
	    {
	       if (abs(A[q][p]) >= eps)
	       {
		  double Theta = (A[q][q] - A[p][p])/(2.*A[q][p]);
		  double t = 1.;

		  if (abs(Theta) > delta)
		  {
		     t = 1./(Theta + signum(Theta)*sqrt(Theta*Theta+1.));
		  }

		  double c = 1./sqrt(1+t*t);
		  double s = c*t;
		  double r = s/(1.+c);

		  double tAqp = t*A[q][p];
		  A[p][p] -= tAqp;
		  A[q][q] += tAqp;
		  A[q][p] = 0.;

		  for (int j = 0; j <= p-1; ++j)
		  {
		     double g = A[q][j] + r*A[p][j];
		     double h = A[p][j] - r*A[q][j];
		     A[p][j] -= s*g;
		     A[q][j] += s*h;
		  }

		  for (int i = p+1; i <= q-1; ++i)
		  {
		     double g = A[q][i] + r*A[i][p];
		     double h = A[i][p] - r*A[q][i];
		     A[i][p] -= s*g;
		     A[q][i] += s*h;
		  }

		  for (int i = q+1; i < n; ++i)
		  {
		     double g = A[i][q] + r*A[i][p];
		     double h = A[i][p] - r*A[i][q];
		     A[i][p] -= s*g;
		     A[i][q] += s*h;
		  }

		  for (int i = 0; i < n; ++i)
		  {
		     double g = V[i][q] + r*V[i][p];
		     double h = V[i][p] - r*V[i][q];
		     V[i][p] -= s*g;
		     V[i][q] += s*h;
		  }
	       }
	    }
	 }
      }
   }

   /**
     * Calculate the eigenvalues and eigenvectors of
     * the symmetric matrix A with the cyclic Jacobian method.
     *
     * After the call the diagonal of A holds the eigenvalues
     * and the columns of the returned matrix V the corresponding
     * eigenvectors.
     */
   public static double[][] cyclJac(PMatrix A)
   {
      int n = A.getRows();
      if (n != A.getCols())
      {
	 throw new IllegalArgumentException("cannot solve for non-square matrix");
      }

      double V[][] = new double [n][n];
      for (int i = 0; i < n; ++i)
      {
	 for (int j = 0; j < n; ++j)
	 {
	    V[i][j] = 0.;
	 }
	 V[i][i] = 1.;
      }

      double sum = 1.;
      double prevSum;
      while (true)
      {
	 prevSum = sum;
	 sum = 0.;
	 for (int i = 0; i < n; ++i)
	 {
	    int endIndex = min(i, A.getMaxCol(i));
	    for (int j = A.getMinCol(i); j < endIndex; ++j)
	    {
	       double Aij = A.get(i, j);
	       sum += Aij*Aij;
	    }
	 }

	 if ( (2.*sum < eps*eps) || (abs(prevSum-sum) < 0.1*eps) )
	 {
	    return V;
	 }

	 for (int p = 0; p < n-1; ++p)
	 {
	    int startIndex = max(p+1, A.getMinCol(p));
	    int endIndex = A.getMaxCol(p);
	    for (int q = startIndex; q < endIndex; ++q)
	    {
	       if (abs(A.get(q,p)) >= eps)
	       {
		  double Theta = (A.get(q,q) - A.get(p,p))/(2.*A.get(q,p));
		  double t = 1.;

		  if (abs(Theta) > delta)
		  {
		     t = 1./(Theta + signum(Theta)*sqrt(Theta*Theta+1.));
		  }

		  double c = 1./sqrt(1+t*t);
		  double s = c*t;
		  double r = s/(1.+c);

		  double tAqp = t*A.get(q,p);
		  A.add(p, p, -tAqp);
		  A.add(q, q, tAqp);
		  A.set(q, p, 0.);

		  for (int j = 0; j <= p-1; ++j)
		  {
		     double g = A.get(q,j) + r*A.get(p,j);
		     double h = A.get(p,j) - r*A.get(q,j);
		     A.add(p, j, -s*g);
		     A.add(q, j, s*h);
		  }

		  for (int i = p+1; i <= q-1; ++i)
		  {
		     double g = A.get(q,i) + r*A.get(i,p);
		     double h = A.get(i,p) - r*A.get(q,i);
		     A.add(i,p, -s*g);
		     A.add(q,i, s*h);
		  }

		  for (int i = q+1; i < n; ++i)
		  {
		     double g = A.get(i,q) + r*A.get(i,p);
		     double h = A.get(i,p) - r*A.get(i,q);
		     A.add(i,p, -s*g);
		     A.add(i,q, s*h);
		  }

		  for (int i = 0; i < n; ++i)
		  {
		     double g = V[i][q] + r*V[i][p];
		     double h = V[i][p] - r*V[i][q];
		     V[i][p] -= s*g;
		     V[i][q] += s*h;
		  }
	       }
	    }
	 }
      }
   }
}
