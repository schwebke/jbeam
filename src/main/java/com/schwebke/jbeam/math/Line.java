package com.schwebke.jbeam.math;
import com.schwebke.math.*;

/**
  * Mathematische Linie.
  * Implementiert das Distance-Interface und kann so
  * zur Berechnung des Abstandes Punkt--Linie benutzt werden.
  */

public class Line implements Distance
{
   /**
     * Erzeugen einer Linie aus zwei Vektoren.
     */
    public Line(MVector p1, MVector p2)
    {
    	this.a = p1;
	this.b = MVector.sub(p2, p1);
    }

    /**
      * Erzeugen einer Linie aus 4 Punktkoordinaten.
      */

    public Line(double p1x, double p1z, double p2x, double p2z)
    {
	a = new MVector(p1x, 0., p1z);
	b = new MVector(p2x - p1x, 0., p2z - p1z);
    }

    MVector a;
    MVector b;

    /**
      * Liefert den Abstand der Linie zum angegebenen Punkt.
      */

    public double getDistance(double x, double z)
    {
	MVector c=new MVector(x-a.x, 0., z-a.z);
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
		MVector.add(a, b), p));
	}

	return MVector.abs(MVector.cross(b, c))
	      /MVector.abs(b);
    }
}
