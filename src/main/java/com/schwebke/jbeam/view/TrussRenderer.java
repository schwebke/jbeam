package com.schwebke.jbeam.view;
import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.math.*;

import java.awt.*;

/** Zeichnen und Selektionstest beim Trusselement */
public class TrussRenderer extends BeamRenderer
{
    static final int N = 1;

    /** Zeichnen */
    static void draw(View view, Truss beam)
    {
	Color oldColor = view.getColor();

	if (view.model.getValidCalculation())
	{
            view.setRHpure();
	    if (view.showDisplacement)
	    {
		drawDisplaced(view, beam);
	    }

	    if (view.showNormalForce)
	    {
		drawStressResultant(view, beam, N);
	    }
            view.setRHstandard();
	}
	view.setColor(oldColor);

	drawElement(view, beam);

    }

    static void drawElement(View view, Truss truss)
    {
	Color oldColor = view.getColor();

	view.move(truss.getN1().getX(), truss.getN1().getZ());
	view.drawThickLine(truss.getN2().getX(), truss.getN2().getZ());

	// Label
	if (!truss.getLabel().equals(""))
	{
	    view.colorLabel();
	    view.move(truss.getN1().getX()+0.5*truss.getR().getX(),
		      truss.getN1().getZ()+0.5*truss.getR().getZ());
	    view.moveBRel(4.*truss.getN().getX(), 4.*truss.getN().getZ());
	    view.drawString(truss.getLabel());
	}

	view.setColor(oldColor);
    }

    private static void drawDisplaced(View view, Truss truss)
    {
	view.colorDis();
	view.move(
	    truss.getN1().getX()+truss.getN1().getDX()*view.displacementScale,
	    truss.getN1().getZ()+truss.getN1().getDZ()*view.displacementScale
	);
	view.drawLine(
	    truss.getN2().getX()+truss.getN2().getDX()*view.displacementScale,
	    truss.getN2().getZ()+truss.getN2().getDZ()*view.displacementScale
	);
	view.colorStd();
    }

    private static void drawStressResultant(View view, Truss beam, int sr)
    {
	int NumSeg = 10;

	double pX[] = new double [NumSeg+4];
	double pZ[] = new double [NumSeg+4];

	pX[0] = pX[NumSeg+3] = beam.getN1().getX();
	pZ[0] = pZ[NumSeg+3] = beam.getN1().getZ();
	pX[NumSeg+2] = beam.getN2().getX();
	pZ[NumSeg+2] = beam.getN2().getZ();

	double dX = pX[NumSeg+2] - pX[0];
	double dZ = pZ[NumSeg+2] - pZ[0];

	double nX = beam.getN().getX();
	double nZ = beam.getN().getZ();

	view.colorSectFill();

	if (sr == N)
	{
	    pX[1] = pX[0] - nX*beam.N(0.)*view.normalForceScale;
	    pZ[1] = pZ[0] - nZ*beam.N(0.)*view.normalForceScale;
	}

	int n=2;

	for (double f=(1./NumSeg); f<=1.0; f+=(1./NumSeg))
	{
	    if (sr == N)
	    {
		pX[n] = pX[0] + f*dX - nX*beam.N(f)*view.normalForceScale;
		pZ[n] = pZ[0] + f*dZ - nZ*beam.N(f)*view.normalForceScale;
	    }

	    n++;
	}

	view.fillPolygon(pX, pZ, NumSeg+4);

	view.colorSect();
	view.drawPolygon(pX, pZ, NumSeg+4);


	view.colorStd();
    
    }
}
 
