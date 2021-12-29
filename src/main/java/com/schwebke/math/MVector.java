package com.schwebke.math;

import java.io.*;

/**
 * Klasse von Funktionen zur Vektorrechnung mit double[3]-Feldern
 */
public class MVector implements Serializable
{
    /** x-Komponente des Vektors */
    public double x;
    /** y-Komponente des Vektors */
    public double y;
    /** z-Komponente des Vektors */
    public double z;

    /**
     * Erzeugt einen Null-Vektor
     */
    public MVector()
    {
	x=y=z=0.;
    }

    /**
     * Erzeugt einen Vektor (x,y,z)
     */
    public MVector(double x, double y, double z)
    {
	this.x=x;
	this.y=y;
	this.z=z;
    }

    /** lesen der x-Komponente des Vektors */
    public double getX()
    {
	return x;
    }

    /** lesen der y-Komponente des Vektors */
    public double getY()
    {
	return y;
    }

    /** lesen der z-Komponente des Vektors */
    public double getZ()
    {
	return z;
    }

    /** setzen der x-Komponente des Vektors */
    public void setX(double x)
    {
	this.x=x;
    }

    /** setzen der y-Komponente des Vektors */
    public void setY(double y)
    {
	this.y=y;
    }

    /** setzen der z-Komponente des Vektors */
    public void setZ(double z)
    {
	this.z=z;
    }



    /**
     * liefert a+b
     */
    public static MVector add(MVector a, MVector b)
    {
	return new MVector(a.x+b.x, a.y+b.y, a.z+b.z);
    }

    /**
     * liefert a-b
     */
    public static MVector sub(MVector a, MVector b)
    {
	return new MVector(a.x-b.x, a.y-b.y, a.z-b.z);
    }

    /**
     * liefert aXb (äußeres Produkt)
     */
    public static MVector cross(MVector a, MVector b)
    {
	return new MVector(
		a.y*b.z-a.z*b.y,
		a.z*b.x-a.x*b.z,
		a.x*b.y-a.y*b.x
	    );
    }

    /**
     * liefert a*s
     */
    public static MVector multiply(MVector a, double s)
    {
	return new MVector(a.x*s, a.y*s, a.z*s);
    }

    /**
     * liefert s*a
     */
    public static MVector multiply(double s, MVector a)
    {
	return new MVector(a.x*s, a.y*s, a.z*s);
    }

    /**
     * liefert <a,b> (inneres Produkt)
     */
    public static double dot(MVector a, MVector b)
    {
	return a.x*b.x+a.y*b.y+a.z*b.z;
    }

    /**
     * liefert |a| (Länge)
     */
    public static double abs(MVector a)
    {
	return Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
    }
}
