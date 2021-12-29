package com.schwebke.math;

/**
  * Linear Equation System Solvers (Ax=b)
  *
  * direct solvers: Gauss, Cholesky
  *                 after the call A contains the decomposed matix
  *                 and b contains the solution vector
  *
  * iterative solver: Gauss-Seidel
  *                   after the call x contains the improved
  *                   solution vector
  *
  * The solver code contains no further comments - see
  * the literature (e.g. H.R. Schwarz, "Numerische Mathematik",
  * Teubner) for details.
  */
public class Solver
{
   /**
     * epsilon:
     *    Gauss: minimal pivot size for non-singular matrix
     *    Gauss-Seidel: desired residual
     */
   public static double eps = 1E-10;


   /**
     * maximum number of iterations for iterative solvers
     */
   public static int itmax = 500;

   /**
     * generic Gauss decomposition solver
     */
   public static void gauss(double[][] A, double[] b)
   {
      int n = A.length;
      if (n != A[0].length)
      {
	 throw new IllegalArgumentException("cannot solve for non-square matrix");
      }

      int k;

      int[] t = new int [n];
      for (k = 0; k < n; ++k)
      {
	 t[k] = k;
      }

      gaussDecomp(A, t);
      permuteGauss(b, t);
      gaussSubst(A, b, t);
   }

   public static void gaussDecomp(double[][] A, int[] t)
   {
      int n = A.length;
      if (n != A[0].length)
      {
	 throw new IllegalArgumentException("cannot solve for non-square matrix");
      }

      int k;

      for (k = 0; k < (n-1); ++k)
      {
	 int i;
	 int pk = 0;
	 double max = 0.;
	 for (i = k; i < n; ++i)
	 {
	    int ti = t[i];
	    double s = 0.;
	    int j;
	    for (j = k; j < n; ++j)
	    {
	       s += Math.abs(A[ti][j]);
	    }
	    double q = Math.abs(A[ti][k])/s;
	    if (q > max)
	    {
	       max = q;
	       pk = i;
	    }
	 }
	 if (max < eps)
	 {
	    throw new IllegalArgumentException("singular matrix");
	 }

	 i = t[k]; t[k] = t[pk]; t[pk] = i;

	 int tk = t[k];

	 for (i = k+1; i < n; ++i)
	 {
	    int ti = t[i];
	    A[ti][k] /= A[tk][k];
	    int j;
	    for (j = k+1; j < n; ++j)
	    {
	       A[ti][j] -= A[ti][k]*A[tk][j];
	    }
	 }
      }
   }

   public static void permuteGauss(double[] b, int[] t)
   {
      int n = b.length;

      double[] tmpB = new double[n];
      for (int k = 0; k < n; ++k)
      {
	 tmpB[k] = b[k];
      }
      for (int k = 0; k < n; ++k)
      {
	 b[k] = tmpB[t[k]];
      }
   }


   public static void gaussSubst(double[][] A, double[] b, int[] t)
   {
      int n = A.length;
      int k;

      for (k = 1; k < n; ++k)
      {
	 int tk = t[k];
	 int i;
	 for (i = 0; i <= (k-1); ++i)
	 {
	    b[k] -= A[tk][i]*b[i];
	 }
      }
      for (k = (n-1); k >= 0; --k)
      {
	 int tk = t[k];
	 int i;
	 for (i = k+1; i < n; ++i)
	 {
	    b[k] -= A[tk][i]*b[i];
	 }
	 b[k] /= A[tk][k];
      }
   }

   /**
     * Cholesky decomposition for symetric positive definite (SPD) matrices
     */
   public static void cholesky(double[][] A, double[] b)
   {
      int n = A.length;
      if (n != A[0].length)
      {
	 throw new IllegalArgumentException("cannot solve for non-square matrix");
      }

      int k;
      for (k = 0; k < n; ++k)
      {
	 int j;
	 for (j = 0; j < k; ++j)
	 {
	    A[k][k] -= A[k][j]*A[k][j];
	 }
	 A[k][k] = Math.sqrt(A[k][k]);

	 int i;
	 for (i = k+1; i < n; ++i)
	 {
	    for (j = 0; j < k; ++j)
	    {
	       A[i][k] -= A[i][j]*A[k][j];
	    }
	    A[i][k] /= A[k][k];
	 }
      }
      for (k = 0; k < n; ++k)
      {
	 int i;
	 for (i = 0; i < k; ++i)
	 {
	    b[k] -= b[i]*A[k][i];
	 }
	 b[k] /= A[k][k];
      }
      for (k = n-1; k >= 0; --k)
      {
	 int i;
	 for (i = k+1; i < n; ++i)
	 {
	    b[k] -= A[i][k]*b[i];
	 }
	 b[k] /= A[k][k];
      }
   }

   /**
     * generic iterative Gauss-Seidel solver
     */
   public static void gaussSeidel(double[][] A, double[] b, double[] x)
   {
      int n = A.length;
      if (n != A[0].length)
      {
	 throw new IllegalArgumentException("cannot solve for non-square matrix");
      }

      int iteration;
      for (iteration = 1; iteration <= itmax; ++iteration)
      {
	 double r = 0.;
	 int i;
	 for (i = 0; i < n; ++i)
	 {
	    double sum = 0.;
	    int j;
	    for (j = 0; j < n; ++j)
	    {
	       if (i != j)
	       {
		  sum += A[i][j]*x[j];
	       }
	    }
	    sum -= b[i];
	    double xn = -1./A[i][i] * sum;
	    r += (x[i]-xn)*(x[i]-xn);
	    x[i] = xn;
	 }
	 if (r < eps*eps)
	 {
	    return;
	 }
      }
   }

   public static void factorizeCholesky(PMatrix A)
   {
      int n = A.getRows();
      if (A.getCols() != n)
      {
	 throw new IllegalArgumentException("factorizeCholesky: attempt to decomp. non-square matrix");
      }

      for (int k = 0; k < n; ++k)
      {
	 double Akk = A.getUnchecked(k, k);
	 for (int j = A.getMinCol(k); j < k; ++j)
	 {
	    double Akj = A.getUnchecked(k, j);
	    Akk -= Akj*Akj;
	 }
	 if (Akk < 1e-15)
	 {
	    System.out.println("warning: singular matrix");
	 }
	 Akk = Math.sqrt(Akk);
	 A.setUnchecked(k, k, Akk);

	 for (int i = k+1; i < n; ++i)
	 {
	    int startIdx = Math.max(A.getMinCol(i), A.getMinCol(k));
	    int endIdx = Math.min(Math.min(A.getMaxCol(i), A.getMaxCol(k)), k);

	    for (int j = startIdx; j < endIdx; ++j)
	    {
	       double Aik = A.getUnchecked(i, k);
	       Aik -= A.getUnchecked(i, j)*A.getUnchecked(k, j);
	       A.setUnchecked(i, k, Aik);
	    }
	    if (A.exist(i, k))
	    {
	       double Aik = A.getUnchecked(i, k);
	       Aik /= Akk;
	       A.setUnchecked(i, k, Aik);
	    }
	 }
      }
      A.updateRowBounds();
   }

   public static void substCholesky(PMatrix A, double[] b)
   {
      int n = A.getRows();

      for (int k = 0; k < n; ++k)
      {
	 for (int i = A.getMinCol(k); i < k; ++i)
	 {
	    b[k] -= b[i]*A.getUnchecked(k, i);
	 }
	 b[k] /= A.getUnchecked(k, k);
      }

      for (int k = n-1; k >= 0; --k)
      {
	 for (int i = k+1; i < A.getMaxRow(k); ++i)
	 {
	    b[k] -= A.get(i, k)*b[i];
	 }
	 b[k] /= A.getUnchecked(k, k);
      }
   }

   public static void linsolveCholesky(PMatrix A, double[] b)
   {
      factorizeCholesky(A);
      substCholesky(A, b);
   }
}
