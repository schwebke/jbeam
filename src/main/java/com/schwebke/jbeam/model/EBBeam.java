package com.schwebke.jbeam.model;
import com.schwebke.jbeam.math.*;
import com.schwebke.math.*;
import java.io.*;
import java.util.*;

/** Euler-Bernoulli-Balkenelement mit inneren Gelenken durch
  * statische Kondensation der Elementsteifigkeitsmatrix.
  */
public class EBBeam extends Beam
{
    static final long serialVersionUID = -1393044615864267287L;

    /// Querschnittswert: EI (Biegesteifigkeit)
    protected double EI;
    /// Querschnittswert: EA (Dehnsteifigkeit)
    protected double EA;

    /** Verteilte Elementbelastung vi */
    protected double vi;
    /** Verteilte Elementbelastung vk */
    protected double vk;
    /** Verteilte Elementbelastung ni */
    protected double ni;
    /** Verteilte Elementbelastung nk */
    protected double nk;

    /** Gelenkbedingung */
    protected boolean hinged[];
    public static final int hNi = 0;
    public static final int hVi = 1;
    public static final int hMi = 2;
    public static final int hNk = 3;
    public static final int hVk = 4;
    public static final int hMk = 5;

    /** Hilfsmatrix für statische Kondensation bei Elementgelenken. */
    protected double invKaa[][];
    /** Hilfsmatrix für statische Kondensation bei Elementgelenken. */
    protected double Kab[][];
    /** Hilfsmatrix für statische Kondensation bei Elementgelenken. */
    protected double Kba[][];
    /** Hilfsmatrix für statische Kondensation bei Elementgelenken. */
    protected double Pa[][];

    /** Schnittgrößen an den Enden */
    protected double Ll[][];

    /** Verschiebung an den Enden. (Weicht bei inneren Gelenken von den
      * Knotenverschiebungen ab. (globales KOS)
      */
    protected double v[];

    /** Verschiebung an den Enden. (Weicht bei inneren Gelenken von den
      * Knotenverschiebungen ab. (globales KOS)
      */
    protected double vl[];

    /** Knotenverschiebung an den Enden.
      * (lokales KOS)
      */
    protected double vkl[];

    /**
      * Konstruktor
      */
    public EBBeam(Node n1, Node n2, double EI, double EA, double m)
    {
	super(n1, n2, m);
	this.EI = EI;
	this.EA = EA;
	vi = vk = 0.;
	ni = nk = 0.;
	hinged = new boolean[6];
	for (int i=0; i<6; i++)
	{
	    hinged[i] = false;
	}
	invKaa = null;
	Kab = Kba = Pa = null;
	v = null;
	Ll = null;
    }

   /**
     * Durchbiegung im verschobenen Starrkörperkoordinatensystem
     */
   public double w(double f)
   {
      double w1 = vkl[1];
      double w2 = vkl[4];
      double wsk = w1 + f*(w2-w1);

      return displace(f)-wsk;
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

      return ((((((vk-vi)*x +
	       5.*vi*l)*x +
	       -20.*Q0*l)*x +
	       -60.*M0*l)*x +
	       -120.*b0*l*EI)*x +
	       120.*w0*EI*l)/(120.*l*EI);
    }

    /**
      * Momentenverlauf
      */
    public double M(double f)
    {
	double x = f*l;
	return Mi + Vi*x - vi*x*x*0.5 - ((vk-vi)/l)*((x*x*x)/6.);
    }

      /**
      * Ort des maximalen Momentes
      */
   public double fMaxM()
   {
      return fExtrM(true);
   }

    /**
      * Ort des minimalen Momentes
      */
   public double fMinM()
   {
      return fExtrM(false);
   }

   protected double fExtrM(boolean maximum)
   {
      Vector f = new Vector();
      f.addElement(new Double(0.));
      f.addElement(new Double(1.));

      if (Math.abs(vi-vk) > 1e-10)
      {
	 double D = vi*vi*l*l+2.*Vi*l*vk-2.*Vi*l*vi;
	 if (D > 1e-10)
	 {
	    double f1 = ((vi*l+Math.sqrt(D))/(vi-vk))/l;
	    double f2 = ((vi*l-Math.sqrt(D))/(vi-vk))/l;
	    if ( (f1 > 0.) && (f1 < 1.) )
	    {
	       f.addElement(new Double(f1));
	    }
	    if ( (f2 > 0.) && (f2 < 1.) )
	    {
	       f.addElement(new Double(f2));
	    }
	 } else if (Math.abs(D) <= 1e-10) {
	    double f1 = (vi)/(vi-vk);
	    if ( (f1 > 0.) && (f1 < 1.) )
	    {
	       f.addElement(new Double(f1));
	    }
	 }
      } else if (Math.abs(vi) > 1e-10) {
	 double f1 = (Vi/vi)/l;
	 if ( (f1 > 0.) && (f1 < 1.) )
	 {
	    f.addElement(new Double(f1));
	 }
      }

      Enumeration ef = f.elements();
      double fMax = ((Double)ef.nextElement()).doubleValue();

      while (ef.hasMoreElements())
      {
	 double fn = ((Double)ef.nextElement()).doubleValue();
	 if (maximum)
	 {
	    if (M(fn) > M(fMax))
	    {
	       fMax = fn;
	    }
	 } else {
	    if (M(fn) < M(fMax))
	    {
	       fMax = fn;
	    }
	 }
      }

      return fMax;
   }


    /**
      * Querkraftverlauf
      */
    public double V(double f)
    {
	double x = f*l;
	return Vi - vi*x - ((vk-vi)/l)*((x*x)/2.);
    }

    /**
      * Ort der maximalen Querkraft
      */
   public double fMaxV()
   {
      return fExtrV(true);
   }

    /**
      * Ort der minimalen Querkraft
      */
   public double fMinV()
   {
      return fExtrV(false);
   }

   protected double fExtrV(boolean maximum)
   {
      Vector f = new Vector();
      f.addElement(new Double(0.));
      f.addElement(new Double(1.));

      if ( Math.abs(vi-vk) > 1e-10 )
      {
	 double x = (vi*l)/(vi-vk);

	 if ( (x > 0.) && (x < l) )
	 {
	    f.addElement(new Double(x/l));
	 }
      }

      Enumeration ef = f.elements();
      double fMax = ((Double)ef.nextElement()).doubleValue();

      while (ef.hasMoreElements())
      {
	 double fn = ((Double)ef.nextElement()).doubleValue();
	 if (maximum)
	 {
	    if (V(fn) > V(fMax))
	    {
	       fMax = fn;
	    }
	 } else {
	    if (V(fn) < V(fMax))
	    {
	       fMax = fn;
	    }
	 }
      }

      return fMax;
   }

    /**
      * Normalkraftverlauf
      */
    public double N(double f)
    {
	return f*Nk+(1.-f)*Ni;
    }

    public double getV(int n)
    {
	return v[n];
    }

    public double getEI()
    {
	return EI;
    }

    public void setEI(double EI)
    {
	this.EI = EI;
    }

    public double getEA()
    {
	return EA;
    }

    public void setEA(double EA)
    {
	this.EA = EA;
    }

    public double getVi()
    {
	return vi;
    }

    public void setVi(double vi)
    {
	this.vi = vi;
    }

    public double getVk()
    {
	return vk;
    }

    public void setVk(double vk)
    {
	this.vk = vk;
    }

    public double getNi()
    {
	return ni;
    }

    public void setNi(double ni)
    {
	this.ni = ni;
    }

    public double getNk()
    {
	return nk;
    }

    public void setNk(double nk)
    {
	this.nk = nk;
    }

    public boolean getHinge(int dof)
    {
	return hinged[dof];
    }

    public void setHinge(int dof, boolean isHinge)
    {
	hinged[dof] = isHinge;
    }

    /**
      * Berechung der globalen Elementsteifigkeitsmatrix.
      */
    public void calSg()
    {
	Sl=new double[6][6];
	for (int i=0; i<6; i++)
	{
	    for (int j=0; j<6; j++)
	    {
		Sl[i][j]=0.;
	    }
	}

	// lokale Steifigkeitsmatrix des allgemeinen Euler-Bernoulli-Balken-Elementes
	Sl[0][0] = EA/l;
	Sl[0][3] = Sl[3][0] = -EA/l;
	Sl[1][1] = 12.*EI/(l*l*l);
	Sl[1][2] = Sl[2][1] = -6.*EI/(l*l);
	Sl[1][4] = Sl[4][1] = -12.*EI/(l*l*l);
	Sl[1][5] = Sl[5][1] = -6.*EI/(l*l);
	Sl[2][2] = 4.*EI/l;
	Sl[2][4] = Sl[4][2] = 6.*EI/(l*l);
	Sl[2][5] = Sl[5][2] = 2.*EI/l;
	Sl[3][3] = EA/l;
	Sl[4][4] = 12.*EI/(l*l*l);
	Sl[4][5] = Sl[5][4] = 6.*EI/(l*l);
	Sl[5][5] = 4.*EI/l;

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

	// Vi, Vk
	Ll[1][0] = -(l/60.)*(21.*vi+9.*vk);
	Ll[4][0] = -(l/60.)*(9.*vi+21.*vk);

	// Mi, Mk
	Ll[2][0] = ((l*l)/60.)*(3.*vi+2.*vk);
	Ll[5][0] = -((l*l)/60.)*(2.*vi+3.*vk);

	/*
	System.out.println("EBBeam Ll:");
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


    /**
      * Berechnung der Schnittgrößen nach der Lösung des Systems.
      */
    public void postCalculate()
    {

	// Zählen der Element-Gelenke
	int hN = 0;
	for (int n=0; n<6; n++)
	{
	    if (hinged[n])
	    {
		hN++;
	    }
	}

	// Knotenverschiebungen einlesen
	v = new double [6];
	this.vl = new double [6];
	vkl = new double [6];
	v[0] = n1.getDX();
	v[1] = n1.getDZ();
	v[2] = n1.getDR();
	v[3] = n2.getDX();
	v[4] = n2.getDZ();
	v[5] = n2.getDR();

	// lokale Knotenverschiebungen
	double vg[][] = new double [6][1];
	for (int n=0; n<6; n++)
	{
	   vg[n][0] = v[n];
	}
	double vl[][] = Matrix.multiply(a, vg);
	for (int n=0; n<6; n++)
	{
	   this.vl[n] = vl[n][0];
	   vkl[n] = vl[n][0];
	}


	// tatsächliche Stabendverschiebungen bei Gelenken
	if (hN > 0)
	{
	    double rb[][] = new double [6-hN][1];
	    double ra[][];

	    // Rückrechnen der wahren Element-End-Verschiebungen für ausgelöste Größen
	    int b=0;
	    for (int n=0; n<6; n++)
	    {
		if (!hinged[n])
		{
		    rb[b][0] = vl[n][0];
		    b++;
		}
	    }

	    ra = 
		Matrix.multiply(
		    invKaa,
		    Matrix.sub(
			Matrix.neg(Pa),
			Matrix.multiply(
			    Kab,
			    rb
			)
		    )
		);

	    // Zuordnen der wahren Verschiebungsgrößen
	    b = 0;
	    for (int n=0; n<6; n++)
	    {
		if (hinged[n])
		{
		    vl[n][0] = ra[b][0];
		    b++;
		}
	    }

	    // Zurückschreiben der wahren V-Größen im globalen KOS
	    vg = Matrix.multiply(Matrix.transpose(a), vl);

	    for (int n=0; n<6; n++)
	    {
	       v[n] = vg[n][0];
	       this.vl[n] = vl[n][0];
	    }
	}

	// Rückrechnung der Stabendschnittgrößen aus den Stabendverschiebungen
	double V[][] = new double [6][1];
	for (int n=0; n<6; n++)
	{
	    V[n][0] = v[n];
	}

	/*
	double Rl[][] =
	    Matrix.multiply(
		Matrix.multiply(
		    Sl,
		    a
		),
		V
	    );
	    */
	double Rl[][] =
	    Matrix.multiply(
	        Sl,
		vl
	    );

	for (int n=0; n<6; n++)
	{
	    Rl[n][0]+=Ll[n][0];
	}

	Ni = -Rl[hNi][0];
	Vi = -Rl[hVi][0];
	Mi = -Rl[hMi][0];
	Nk = Rl[hNk][0];
	Vk = Rl[hVk][0];
	Mk = Rl[hMk][0];

    }
}

