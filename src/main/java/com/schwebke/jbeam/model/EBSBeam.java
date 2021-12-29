package com.schwebke.jbeam.model;
import com.schwebke.jbeam.math.*;
import com.schwebke.math.*;
import java.io.*;
import java.util.*;

/** Euler-Bernoulli-Shear-Balkenelement mit inneren Gelenken durch
  * statische Kondensation der Elementsteifigkeitsmatrix und
  * Berücksichtigung von Schub.
  */
public class EBSBeam extends EBBeam
{
    static final long serialVersionUID = -6616605126487015726L;

    /// Querschnittswert: GAs (Schubsteifigkeit)
    protected double GAs;

    /**
      * Schubparameter psi
      */
    protected double psi;

    /**
      * Konstruktor
      */
    public EBSBeam(Node n1, Node n2, double EI, double EA, double GAs, double m)
    {
	super(n1, n2, EI, EA, m);
	this.GAs = GAs;
	updatePsi();
    }

   /**
     * Durchbiegung
     */
   public double displace(double f)
   {
      double w0 = vl[1];
      double x = f*l;
      double b0 = vl[2];
      double Q0 = Vi;
      double M0 = Mi;

      return ((((((-GAs*vi+vk*GAs)*x +
	       5.*GAs*vi*l)*x +
	       -20.*vk*EI+20.*EI*vi-20.*Q0*GAs*l)*x +
	       -60.*vi*l*EI-60.*M0*GAs*l)*x +
	       -120.*b0*GAs*l*EI+120.*Q0*l*EI)*x +
	       120.*GAs*w0*EI*l)/(120.*GAs*l*EI);
    }

    /**
      * Ableitung der Durchbiegung
      * (zur Bestimmung lokaler Extremwerte)
      */
    protected double ddisplace(double f)
    {
       double x = f*l;
       double b0 = vl[2];

       return (((((-GAs*vi+vk*GAs)*x +
               4.*vi*GAs*l)*x +
	       -12.*Vi*GAs*l+12.*EI*vi-12.*vk*EI)*x +
	       -24.*vi*l*EI-24.*Mi*GAs*l)*x +
	       -24.*b0*GAs*l*EI+24.*Vi*l*EI)/(24.*GAs*l*EI);
    }

    /**
      * Ort der maximalen Durchbiegung
      */
   public double fDisplaceMax()
   {
      return fDisplaceExtr(true);
   }

    /**
      * Ort der minimalen Durchbiegung
      */
   public double fDisplaceMin()
   {
      return fDisplaceExtr(false);
   }

   protected double fDisplaceExtr(boolean maximum)
   {
      Vector f = new Vector();
      f.addElement(new Double(0.));
      f.addElement(new Double(1.));

      /*
       * Dieses Verfahren sollte durch ein besseres
       * zur Polynomnullstellenermittlung ersetzt
       * werden.
       *
       * Das Intervall [0,1] wird in Teilintervalle
       * zerlegt. Wenn im Teilintervall ein Vorzeichenwechsel
       * auftritt, wird mit dem Sekantenverfahren eine
       * Nullstelle gesucht.
       */

      // Anzahl der Untersuchten Intervalle
      int i = 10;
      int n;
      for (n = 0; n < i; ++n)
      {
	 double a = 1./i * n;
	 double b = a + 1./i;
	 double da = ddisplace(a);
	 double db = ddisplace(b);

	 if (da*db <= 0.)
	 {
	    double x1 = a;
	    double x2 = b;
	    double y1 = da;
	    double y2 = db;

	    int step = 0;

	    while (Math.abs(x1-x2) > 1e-6)
	    {
	       double xn = x1 + y1/((y1-y2)/(x2-x1));

	       if ( (xn<a) || (xn>b) || ((++step)>20))
	       {
		  System.out.println("EBSBeam::fDisplaceMax(): Sekantenverfahren konvergiert nicht!");
		  break;
	       }
	       
	       double yn = ddisplace(xn);

	       x1 = x2; y1 = y2;
	       x2 = xn; y2 = yn;
	    }

	    if (Math.abs(x1-x2) <= 1e-6)
	    {
	       f.addElement(new Double(x2));
	    }
	 }
      }

      f.addElement(new Double(0.5));


      Enumeration ef = f.elements();
      double fMax = ((Double)ef.nextElement()).doubleValue();

      while (ef.hasMoreElements())
      {
	 double fn = ((Double)ef.nextElement()).doubleValue();
	 if (maximum)
	 {
	    if (displace(fn) > displace(fMax))
	    {
	       fMax = fn;
	    }
	 } else {
	    if (displace(fn) < displace(fMax))
	    {
	       fMax = fn;
	    }
	 }
      }

      return fMax;
   }

    public double getGAs()
    {
	return GAs;
    }

    public void setGAs(double GAs)
    {
	this.GAs = GAs;
	updatePsi();
    }


    public void setN1(Node n1)
    {
       super.setN1(n1);
       updatePsi();
    }

    public void setN2(Node n2)
    {
       super.setN2(n2);
       updatePsi();
    }

    protected void updatePsi()
    {
	psi = 1. / ( 1.+12.* ((EI)/(l*l*GAs)) );
    }

    /**
      * Berechung der globalen Elementsteifigkeitsmatrix.
      */
    public void calSg()
    {
	updatePsi();
	Sl=new double[6][6];
	for (int i=0; i<6; i++)
	{
	    for (int j=0; j<6; j++)
	    {
		Sl[i][j]=0.;
	    }
	}

	// lokale Steifigkeitsmatrix des allgemeinen Euler-Bernoulli-Shear-Balken-Elementes
	Sl[0][0] = EA/l;
	Sl[0][3] = Sl[3][0] = -EA/l;
	Sl[1][1] = 12.*psi*EI/(l*l*l);
	Sl[1][2] = Sl[2][1] = -6.*psi*EI/(l*l);
	Sl[1][4] = Sl[4][1] = -12.*psi*EI/(l*l*l);
	Sl[1][5] = Sl[5][1] = -6.*psi*EI/(l*l);
	Sl[2][2] = (1.+3.*psi)*EI/l;
	Sl[2][4] = Sl[4][2] = 6.*psi*EI/(l*l);
	Sl[2][5] = Sl[5][2] = (-1.+3.*psi)*EI/l;
	Sl[3][3] = EA/l;
	Sl[4][4] = 12.*psi*EI/(l*l*l);
	Sl[4][5] = Sl[5][4] = 6.*psi*EI/(l*l);
	Sl[5][5] = (1.+3.*psi)*EI/l;

	// Zählen der Element-Gelenke
	int hN = 0;
	for (int n=0; n<6; n++)
	{
	    if (hinged[n])
	    {
		hN++;
	    }
	}

	// Auslösen der Gelenke durch statische Kondensation
	if (hN > 0)
	{
	    double Kaa[][] = new double[hN][hN];
	    Kab = new double[hN][6-hN];
	    Kba = new double[6-hN][hN];
	    double Kbb[][] = new double[6-hN][6-hN];

	    int aai = 0;
	    int aaj = 0;
	    int bbi = 0;
	    int bbj = 0;
	    int abi = 0;
	    int abj = 0;
	    int bai = 0;
	    int baj = 0;
	    

	    for (int i=0; i<6; i++)
	    {
		for (int j=0; j<6; j++)
		{
		    if (hinged[i])
		    {
			if (hinged[j])
			{
			    Kaa[aai][aaj++] = Sl[i][j];
			} else {
			    Kab[abi][abj++] = Sl[i][j];
			}
		    } else {
			if (hinged[j])
			{
			    Kba[bai][baj++] = Sl[i][j];
			} else {
			    Kbb[bbi][bbj++] = Sl[i][j];
			}
		    }
		}
		aaj = 0;
		bbj = 0;
		baj = 0;
		abj = 0;
		if (hinged[i])
		{
		    aai++;
		    abi++;
		} else {
		    bbi++;
		    bai++;
		}
	    }


	    double Kred[][] = 
		Matrix.sub(
		    Kbb, 
		    Matrix.multiply(
			Kba,
			Matrix.multiply(
			    invKaa = Matrix.invert(Kaa),
			    Kab
			)
		    )
		);


	    bbi = 0;
	    bbj = 0;
	    for (int i=0; i<6; i++)
	    {
		for (int j=0; j<6; j++)
		{
		    if (hinged[i])
		    {
			Sl[i][j] = 0.;
		    } else {
			if (hinged[j])
			{
			    Sl[i][j] = 0.;
			} else {
			    Sl[i][j] = Kred[bbi][bbj++];
			}
		    }
		}
		bbj = 0;
		if (!hinged[i])
		{
		    bbi++;
		}
	    }

	}

	calMl();
	transform();

	// load vector
	Ll = new double [6][1];
	//   Ni, Nk
	Ll[0][0] = -(2*ni+nk)*l/6.;
	Ll[3][0] = -(ni+2.*nk)*l/6.;

	/*
	// Vi, Vk
	Ll[1][0] = -(l/60.)*(21.*vi+9.*vk);
	Ll[4][0] = -(l/60.)*(9.*vi+21.*vk);

	// Mi, Mk
	Ll[2][0] = ((l*l)/60.)*(3.*vi+2.*vk);
	Ll[5][0] = -((l*l)/60.)*(2.*vi+3.*vk);
	*/

	{
	   double p2[][] = new double [2][1];
	   p2[0][0] = (vi+vk)/2.;
	   p2[1][0] = (vk-vi)/2.;

	   double p1[][] = new double [4][2];
	   p1[0][0] = 30.;   p1[0][1] = -10. - 2.*psi;
	   p1[1][0] = 30.;   p1[1][1] = 10. + 2.*psi;
	   p1[2][0] = -5.*l; p1[2][1] = l*psi;
	   p1[3][0] = 5.*l;  p1[3][1] = l*psi;

	   double p[][] = Matrix.multiply(
	                         -l/60.,
				 Matrix.multiply(p1, p2)
			      );

	   Ll[1][0] = p[0][0];
	   Ll[4][0] = p[1][0];
	   Ll[2][0] = p[2][0];
	   Ll[5][0] = p[3][0];
	}

	/*
	System.out.println("EBSBeam Ll:");
	Matrix.dump(Ll);
	*/

	if (hN > 0)
	{
	    Pa = new double[hN][1];
	    double Pb[][] = new double[6-hN][1];
	    int a = 0;
	    int b = 0;

	    for (int n=0; n<6; n++)
	    {
		if (hinged[n])
		{
		    Pa[a][0] = Ll[n][0];
		    a++;
		} else {
		    Pb[b][0] = Ll[n][0];
		    b++;
		}
	    }

	    double Pred[][] = 
		Matrix.sub(
		    Pb,
		    Matrix.multiply(
			Matrix.multiply(
			    Kba,
			    invKaa
			),
			Pa
		    )
		);

	    b = 0;
	    for (int n=0; n<6; n++)
	    {
		if (!hinged[n])
		{
		    Ll[n][0] = Pred[b][0];
		    b++;
		} else {
		    Ll[n][0] = 0.;
		}
	    }
	}

	double L[][] = Matrix.multiply(Matrix.transpose(a), Ll);

	for (int n=0; n<6; n++)
	{
	    Lg[n] = L[n][0];
	}
    }

}

