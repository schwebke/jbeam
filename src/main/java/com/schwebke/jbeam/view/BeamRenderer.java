package com.schwebke.jbeam.view;
import com.schwebke.jbeam.model.*;


/**
  * Zeichnen und Selektionstest beim Beam
  */
public class BeamRenderer
{
    /** Anzahl Segmente und Schrittweite für Anzeige (bei Verschiebung, Momenten, ...) */
    protected static final int numSeg = 20;
    protected static final double displayStep = 1.0/numSeg;
    protected static final double eps = 1e-8;
    
   /** Selektionstest */
    public static boolean selected(double x, double z, double r, Beam beam, View view)
    {
       // Boundingbox
	double x1, x2, z1, z2;
	Node n1 = beam.getN1();
	Node n2 = beam.getN2();
	if (n1.getX() < n2.getX())
	{
	    x1 = n1.getX()-r;
	    x2 = n2.getX()+r;
	} else {
	    x2 = n1.getX()+r;
	    x1 = n2.getX()-r;
	}
	if ( (x<x1) || (x>x2) )
	{
	    return false;
	}
	if (n1.getZ() < n2.getZ())
	{
	    z1 = n1.getZ()-r;
	    z2 = n2.getZ()+r;
	} else {
	    z2 = n1.getZ()+r;
	    z1 = n2.getZ()-r;
	}
	if ( (z<z1) || (z>z2) )
	{
	    return false;
	}

	// teurer Test
	return beam.getDistance(x, z) <= r;
    }
}

