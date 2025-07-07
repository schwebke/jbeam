package com.schwebke.jbeam.model;
import com.schwebke.jbeam.math.*;
import com.schwebke.math.*;

import java.io.*;

/**
  * Abstakte Klasse für die gemeinsamen Eigenschaften
  * der Truss- und EBBeam-Elemente.
  */
public abstract class Beam implements Distance, Serializable
{

    /**
      * Startknoten
      */
    protected Node n1;
    /**
      * Endknoten
      */
    protected Node n2;

    /**
      * Label
      */
    protected String label;

    /** lokale Elementsteifigkeitsmatrix */
    protected double Sl[][];
    /** globale Elementsteifigkeitsmatrix */
    protected double Sg[][];
    /** Transformationsmatrix zwischen lokalen und globalen Koordinaten */
    protected double a[][];
    /** globaler Elementlastvektor */
    protected double Lg[];

    /** lokale konzentrierte Massenmatrix */
    protected double Ml[][];
    /** globale konzentrierte Massenmatrix */
    protected double Mg[][];
    /** Massenbelegung */
    protected double m;

    /** Stabendschnittgröße Ni */
    protected double Ni;
    /** Stabendschnittgröße Nk */
    protected double Nk;
    /** Stabendschnittgröße Vi */
    protected double Vi;
    /** Stabendschnittgröße Vk */
    protected double Vk;
    /** Stabendschnittgröße Mi */
    protected double Mi;
    /** Stabendschnittgröße Mk */
    protected double Mk;

    /** Normalenvektor des Elements */
    protected MVector n;
    /** Richtungsvektor des Elements */
    protected MVector r;

    /** Länge des Elements */
    protected double l;

    /** Erzeugen eines Beamelementes */
    public Beam(Node n1, Node n2, double m)
    {
	this.n1=n1;
	this.n2=n2;
	this.m = m;
	label = new String("");
	Ni = Nk = 0.;
	Vi = Vk = 0.;
	Mi = Mk = 0.;
	Sl = Sg = a = null;
	n = r = null;
	l = 0.;
	calculateVector();
	Lg = new double [6];
	for (int n=0; n<6; n++)
	{
	    Lg[n] = 0.;
	}
    }

    /** Berechnen der Elementvektorinformationen aus den tatsächlichen
      * Knotenorten
      */
    public void calculateVector()
    {
	r = new MVector(n2.getX()-n1.getX(), 0., n2.getZ()-n1.getZ());
	l = MVector.abs(r);
	n = MVector.multiply(MVector.cross(r, new MVector(0., 1., 0.)), 1./l);
    }

    public MVector getN()
    {
	return n;
    }

    public MVector getR()
    {
	return r;
    }

    public double getL()
    {
	return l;
    }

    public double[][] getSg()
    {
	return Sg;
    }

    public double[][] getMg()
    {
       return Mg;
    }
    
    public double[][] getSl()
    {
       return Sl;
    }
    
    public double[] getLg()
    {
       return Lg;
    }
    
    public double[][] getMl()
    {
       return Ml;
    }
    
    public double[][] getTransformationMatrix()
    {
       return a;
    }

    public Node getN1()
    {
	return n1;
    }

    public void setN1(Node n1)
    {
	this.n1 = n1;
    }

    public Node getN2()
    {
	return n2;
    }

    public void setN2(Node n2)
    {
	this.n2 = n2;
    }

    public void setM(double m)
    {
       this.m = m;
    }

    public double getM()
    {
       return m;
    }

    public String getLabel()
    {
	return label;
    }

    public void setLabel(String label)
    {
	this.label = label;
    }

    /** Berechnen der globalen Elementsteifigkeitsmatritzen.
      */
    abstract public void calSg();

    /** Berechnen der lokalen konzentrierten Massenmatrix.
      * Diese Funktion sollten von calSg aufgerufen werden,
      * bevor transform aufgerufen wird.
      * Dann berechnet calSg sowol Sg als auch Mg
      */
    protected void calMl()
    {
       Ml = new double[6][6];

       for (int i = 0; i < 6; ++i)
       {
	  for (int j = 0; j < 6; ++j)
	  {
	     Ml[i][j] = 0.;
	  }
       }

       Ml[0][0] = .5*m*l;
       Ml[1][1] = .5*m*l;
       Ml[3][3] = .5*m*l;
       Ml[4][4] = .5*m*l;
    }

    /** Berechnen von Schnittgrößen und anderen Parametern
      * nach Lösung der Systems.
      */
    abstract public void postCalculate();

    /** Durchbiegung an der Stelle x im
      * verschobenen Starrkörperkoordinatensystem
      */
    abstract public double w(double x);

    /** Abstand eines Punktes zum Element.
      */
    public double getDistance(double x, double z)
    {
	MVector a=new MVector(n1.x, 0., n1.z);
	MVector b=new MVector(n2.x-n1.x, 0., n2.z-n1.z);
	MVector c=new MVector(x-n1.x, 0., z-n1.z);
	MVector p=new MVector(x, 0., z);
	double t=MVector.dot(MVector.sub(p, a), b)/
		    MVector.dot(b, b);

	if (t<0.)
	{
	    return MVector.abs(MVector.sub(a, p));
	}

 	if (t>1.)
	{
	    return MVector.abs(MVector.sub(
		new MVector(n2.x, 0., n2.z), p));
	}


	return MVector.abs(MVector.cross(b, c))
	      /MVector.abs(b);
    }

    /** Berechnung der Transformationsmatrix a und der globalen
      * Steifigkeitsmatritzen Sg und Mg aus Sl und Ml.
      */
    protected final void transform()
    {
	a=new double[6][6];

	for (int i=0; i<6; i++)
	{
	    for (int j=0; j<6; j++)
	    {
		a[i][j]=0.;
	    }
	}

	double alpha = Math.atan2(n2.z-n1.z, n2.x-n1.x);
	a[0][0]=a[1][1]=a[3][3]=a[4][4]=Math.cos(alpha);
	a[0][1]=a[3][4]=Math.sin(alpha);
	a[1][0]=a[4][3]=-Math.sin(alpha);
	a[2][2]=a[5][5]=1.;

	Sg=Matrix.multiply(Matrix.multiply(Matrix.transpose(a), Sl), a);
	Mg=Matrix.multiply(Matrix.multiply(Matrix.transpose(a), Ml), a);
    }
}
 
