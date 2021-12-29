package com.schwebke.jbeam.model;
import com.schwebke.jbeam.math.*;

import java.io.*;

/**
  * Klasse für Systemknoten
  */
public class Node implements Distance, Serializable
{
    static final long serialVersionUID = -4315467087879300491L;
    
    /** Lagerbedingung cX */
    protected boolean cX;
    /** Lagerbedingung cZ */
    protected boolean cZ;
    /** Lagerbedingung cR */
    protected boolean cR;

    /** globale Freiheitsgradnummer nX (nur für Systemberechnung)*/
    protected int nX;
    /** globale Freiheitsgradnummer nZ (nur für Systemberechnung)*/
    protected int nZ;
    /** globale Freiheitsgradnummer nR (nur für Systemberechnung)*/
    protected int nR;

    /** Koordinate x */
    protected double x;
    /** Koordinate x */
    protected double z;

    /** Knotenlast Fx */
    protected double Fx;
    /** Knotenlast Fz */
    protected double Fz;
    /** Knotenlast M */
    protected double M;

    /** Knotenverschiebung dX */
    protected double dX;
    /** Knotenverschiebung dZ */
    protected double dZ;
    /** Knotenverschiebung dR */
    protected double dR;

    /** Auflagerreaktion rFx */
    protected double rFx;
    /** Auflagerreaktion rFz */
    protected double rFz;
    /** Auflagerreaktion rM */
    protected double rM;

    /** Label */
    protected String label;

    public String getLabel()
    {
	return label;
    }

    public void setLabel(String label)
    {
	this.label = label;
    }

    public Node()
    {
	cX = cZ = cR = false;
	nX = nZ = nR = 0;
	dX = dZ = dR = 0.;
	x = 0.;
	z = 0.;
	Fx = 0.;
	Fz = 0.;
	M = 0.;
	rFx = rFz = rM = 0.;
	label = new String("");
    }

    public Node(boolean cX, boolean cZ, boolean cR)
    {
	this();
	this.cX = cX;
	this.cZ = cZ;
	this.cR = cR;
    }

    public Node(double x, double z)
    {
	this();
	this.x = x;
	this.z = z;
    }

    public Node(double x, double z, boolean cX, boolean cZ, boolean cR)
    {
	this();
	this.x = x;
	this.z = z;
	this.cX = cX;
	this.cZ = cZ;
	this.cR = cR;
    }

    public boolean getCX()
    {
	return cX;
    }

    public void setCX(boolean cX)
    {
	this.cX = cX;
    }

    public boolean getCZ()
    {
	return cZ;
    }

    public void setCZ(boolean cZ)
    {
	this.cZ = cZ;
    }

    public boolean getCR()
    {
	return cR;
    }

    public void setCR(boolean cR)
    {
	this.cR = cR;
    }

    public double getX()
    {
	return x;
    }

    public void setX(double x)
    {
	this.x = x;
    }

    public double getZ()
    {
	return z;
    }

    public void setZ(double z)
    {
	this.z = z;
    }


    public double getFx()
    {
	return Fx;
    }

    public void setFx(double Fx)
    {
	this.Fx = Fx;
    }

    public double getFz()
    {
	return Fz;
    }

    public void setFz(double Fz)
    {
	this.Fz = Fz;
    }

    public double getM()
    {
	return M;
    }

    public void setM(double M)
    {
	this.M = M;
    }

    public double getRFx()
    {
	return rFx;
    }

    public double getRFz()
    {
	return rFz;
    }

    public double getRM()
    {
	return rM;
    }

    public double getDX()
    {
	return dX;
    }

    public double getDZ()
    {
	return dZ;
    }

    public double getDR()
    {
	return dR;
    }

    /** Abstand des Knotens von einem Punkt */
    public double getDistance(double x, double z)
    {
	return Math.sqrt((this.x-x)*(this.x-x)+(this.z-z)*(this.z-z));
    }
}
 
