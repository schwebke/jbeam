package com.schwebke.math;

/**
 * Klasse von Funktionen zur Matrix-Rechnung mit zweidimensionalen Feldern.
 */
public class Matrix
{
    /**
     * liefert die transponierte Matrix
     */
    public static double[][] transpose(double m[][])
    {
	int I = m.length;
	int J = m[0].length;

	double ret[][]=new double[I][J];
	for (int i=0; i<I; i++)
	{
	    for (int j=0; j<J; j++)
	    {
		ret[i][j]=m[j][i];
	    }
	}
	return ret;
    }

    /**
     * liefert Matrix * (-1.)
     */
    public static double[][] neg(double m[][])
    {
	int I = m.length;
	int J = m[0].length;

	double ret[][]=new double[I][J];
	for (int i=0; i<I; i++)
	{
	    for (int j=0; j<J; j++)
	    {
		ret[i][j]= -m[i][j];
	    }
	}
	return ret;
    }

    /**
     * liefert a-b
     */
    public static double[][] sub(double a[][], double b[][])
    {
	int I = a.length;
	int J = a[0].length;

	double ret[][]=new double[I][J];
	for (int i=0; i<I; i++)
	{
	    for (int j=0; j<J; j++)
	    {
		ret[i][j] = a[i][j]-b[i][j];
	    }
	}
	return ret;
    }

    /**
     * liefert a+b
     */
    public static double[][] add(double a[][], double b[][])
    {
	int I = a.length;
	int J = a[0].length;

	double ret[][]=new double[I][J];
	for (int i=0; i<I; i++)
	{
	    for (int j=0; j<J; j++)
	    {
		ret[i][j] = a[i][j]+b[i][j];
	    }
	}
	return ret;
    }

    /**
     * liefert a*b
     */
    public static double[][] multiply(double a[][], double b[][])
    {
	int I = a.length;
	int J = b.length;
	int K = b[0].length;

	double m[][]=new double[I][K];
	for (int i=0; i<I; ++i)
	{
	    for (int k=0; k<K; ++k)
	    {
		double t=0.;
		for (int j=0; j<J; ++j)
		{
		    t+=a[i][j]*b[j][k];
		}
		m[i][k]=t;
	    }
	}
	return m;
    }

    /**
     * liefert a*b
     */
    public static double[] multiply(double a[][], double b[])
    {
	int I = a.length;
	int J = b.length;

	double r[]=new double[I];
	for (int i=0; i<I; i++)
	{
	     double t=0.;
	     for (int j=0; j<J; j++)
	     {
		 t+=a[i][j]*b[j];
	     }
	     r[i]=t;
	}
	return r;
    }

    /**
     * liefert a*b
     */
    public static double[][] multiply(double a, double b[][])
    {
	int I = b.length;
	int J = b[0].length;

	double m[][]=new double[I][J];
	for (int i=0; i<I; i++)
	{
	    for (int j=0; j<J; j++)
	    {
		m[i][j]=a*b[i][j];
	    }
	}
	return m;
    }
    public static double[][] multiply(double b[][], double a)
    {
       return multiply(a, b);
    }

    /**
     * erzeugt eine tiefe Kopie einer Matix
     */
    public static double[][] duplicate(double a[][])
    {
       int I = a.length;
       int J = a[0].length;

       double r[][] = new double[I][J];

       int i;
       for (i = 0; i < I; ++i)
       {
	  int j;
	  for (j = 0; j < J; ++j)
	  {
	     r[i][j] = a[i][j];
	  }
       }

       return r;
    }

    /**
     * liefert die inverse Matrix
     */
    public static double[][] invert(double a[][])
    {
	int I = a.length;

	if (I != a[0].length)
	{
	    throw new IllegalArgumentException("cannot invert non-square matrix");
	}

	double m[][]=new double[I][I];
	double e[] = new double [I];
	int[] t = new int [I];
	for (int k = 0; k < I; ++k)
	{
	   t[k] = k;
	}
	Solver.gaussDecomp(a, t);

	for (int i=0; i<I; i++)
	{
	    for (int n=0; n<I; n++)
	    {
		e[n] = 0.;
	    }
	    e[i] = 1.;

	    Solver.permuteGauss(e, t);
	    Solver.gaussSubst(a, e, t);

	    for (int n=0; n<I; n++)
	    {
		m[n][i] = e[n];
	    }
	}

	return m;
    }
    
    /**
      * Matrix auf Konsole ausgeben
      */
    public static void dump(double a[][])
    {
       int iN = a.length;
       int i;
       for (i = 0; i < iN; ++i)
       {
	  int jN = a[i].length;
	  int j;
	  for (j = 0; j < jN; ++j)
	  {
	     System.out.print(a[i][j]+"\t");
	  }
	  System.out.println("");
       }
    }
}
 
