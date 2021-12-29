package com.schwebke.jbeam.view;
import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.math.*;
import com.schwebke.math.*;

import java.awt.*;

/** Zeichnen und Selektionstest beim Node */
public class NodeRenderer
{
   /** Zeichnen */
    static void draw(View view, Node node)
    {
	drawElement(view, node);
    }

    static void drawElement(View view, Node node)
    {
	Color oldColor=view.getColor();

	double x = node.getX();
	double z = node.getZ();
	double Fz = node.getFz();
	double Fx = node.getFx();
	double M = node.getM();

	view.move(x, z);
	if (!node.getCR())
	{
	    view.drawBCircle(.75);
	}

	if (node.getCX() && node.getCZ() && node.getCR())
	{
	    view.move(x, z);
	    view.drawBLineRel(0., 3.);
	    view.moveBRel(-3., 0.);
	    view.drawBLineRel(6., 0.);

	    for (int n=0; n<6; n++)
	    {
		view.drawBLineRel(-1., 1.);
		view.moveBRel(0., -1.);
	    }
	} else {
	    view.move(x, z);
	    if (node.getCR())
	    {
		view.moveBRel(-1.,-1.);
		view.drawBLineRel(2.,0.);
		view.drawBLineRel(0.,2.);
		view.drawBLineRel(-2.,0.);
		view.drawBLineRel(0.,-2.);
	    }

	    view.move(x, z);
	    if (node.getCX() && node.getCZ())
	    {
		double ax[]={0.0, -2.8, 2.8, 0.0};
		double ay[]={0.0,  4.0, 4.0, 0.0};
		view.drawBPolygonRel(ax, ay, ax.length);
		view.move(x, z);
		view.moveBRel(2.8, 4);
		for (int n=0; n<6; n++)
		{
		    view.drawBLineRel(-1., 1.);
		    view.moveBRel(0., -1.);
		}
	    } else if (node.getCX()) {
		double ax[]={0.0, -4.0, -4.0, 0.0};
		double ay[]={0.0, -2.8,  2.8, 0.0};
		view.drawBPolygonRel(ax, ay, ax.length);
		view.move(x, z);
		view.moveBRel(-4.5, -3.);
		view.drawBLineRel(0., 6.);
	    } else if (node.getCZ()) {
		double ax[]={0.0, -2.8, 2.8, 0.0};
		double ay[]={0.0,  4.0, 4.0, 0.0};
		view.drawBPolygonRel(ax, ay, ax.length);
		view.move(x, z);
		view.moveBRel(-3., 4.5);
		view.drawBLineRel(6., 0.);
	    }
	}

	if ( (Math.abs(Fx)>1e-10) || (Math.abs(Fz)>1e-10) )
	{
	    view.colorLoad();
	    double len=Math.sqrt(Fx*Fx + Fz*Fz);

	    view.move(x, z);
	    view.moveBRel((Fx/len)*(-15.), (Fz/len)*(-15.));
	    view.drawBLineRel((Fx/len)*12., (Fz/len)*12.);
	    MVector f= new MVector(Fx/len, 0., Fz/len);
	    MVector n= MVector.cross(
		(new MVector(0., 1., 0.)), f);
	    view.drawBLineRel(-f.x*4.+n.x*2., -f.z*4.+n.z*2.);
	    view.drawBLineRel(-n.x*4., -n.z*4.);
	    view.drawBLineRel(f.x*4.+n.x*2., f.z*4.+n.z*2.);

	    view.move(x, z);
	    view.moveBRel((Fx/len)*(-15.), (Fz/len)*(-15.));
	    view.drawString(view.controller.getNumberFormat().format(len));
	    
	}

	if ( Math.abs(M) > 1e-10 )
	{
	    view.colorLoad();

	    view.move(x,z);
	    view.drawBArc(7., 0, 180);

	    if (M>0.)
	    {
		view.moveBRel(-7., 0.);
	    } else {
		view.moveBRel(7., 0.);
	    }

	    view.drawBLineRel(-2., -2.);
	    view.drawBLineRel(4., 0.);
	    view.drawBLineRel(-2., 2.);

	    view.move(x,z);
	    view.moveBRel(0., -8.);
	    view.drawString(view.controller.getNumberFormat().format(M));

	}

	if (!node.getLabel().equals(""))
	{
	    view.colorLabel();
	    view.move(x,z);
	    view.moveBRel(-2., 4.);
	    view.drawString(node.getLabel());
	}

	view.setColor(oldColor);
    }

    /** Selektionstest */
    public static boolean selected(double x, double z, double r, Node node, View view)
    {
	double bscale = view.baseScale(1.);

	// BBox-Test
	double critR = 5.*bscale;
	if ( (x<(node.getX()-critR)) || (x>(node.getX()+critR))
	     || (z<(node.getZ()-critR)) || (z>(node.getZ()+critR)) )
	{
	    return false;
	}

	// expensive Test
	if ( !node.getCX() && !node.getCZ() )
	{
	    // punktförmig
	    return (Math.sqrt((x-node.getX())*(x-node.getX()) + 
		(z-node.getZ())*(z-node.getZ())) <= r);
	} else if ( node.getCX() && node.getCZ() && node.getCR() ) {
	    // Einspannung
	    Line l1 = new Line(node.getX(), node.getZ(), 
			       node.getX(), node.getZ()+3.5*bscale);
	    Line l2 = new Line(node.getX()-3.*bscale,
				node.getZ()+3.5*bscale,
				node.getX()+3.*bscale,
				node.getZ()+3.5*bscale);
	    return (Math.min(l1.getDistance(x, z), l2.getDistance(x, z))
		<= r);
	} else if ( node.getCZ() ) {
	    // Dreieck unterhalb
	    Line l1 = new Line(node.getX(), node.getZ(),
				node.getX()-2.8*bscale, node.getZ()+4.5*bscale);
	    Line l2 = new Line(node.getX()-2.8*bscale, node.getZ()+4.5*bscale,
				node.getX()+2.8*bscale, node.getZ()+4.5*bscale);
	    Line l3 = new Line(node.getX()+2.8*bscale, node.getZ()+4.5*bscale,
				node.getX(), node.getZ());
	    return (Math.min(l1.getDistance(x, z),
		    Math.min(l2.getDistance(x, z),
		    l3.getDistance(x, z))) <= r);
	} else {
	    // Dreieck links
	    Line l1 = new Line(node.getX(), node.getZ(),
				node.getX()-4.5*bscale, node.getZ()-2.8*bscale);
	    Line l2 = new Line(node.getX()-4.5*bscale, node.getZ()-2.8*bscale,
				node.getX()-4.5*bscale, node.getZ()+2.8*bscale);
	    Line l3 = new Line(node.getX()-4.5*bscale, node.getZ()+2.8*bscale,
				node.getX(), node.getZ());
	    return (Math.min(l1.getDistance(x, z),
		    Math.min(l2.getDistance(x, z),
		    l3.getDistance(x, z))) <= r);
	}
    }
}
 
