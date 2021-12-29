package com.schwebke.jbeam.view;
import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.math.*;

import java.awt.*;

/** Zeichnen und Selektionstest beim EBSBeam */
public class EBSBeamRenderer extends BeamRenderer
{

    static final int M = 0;
    static final int N = 1;
    static final int V = 2;
    
    /** Zeichnen */
    static void draw(View view, EBSBeam beam)
    {
	Color oldColor = view.getColor();

	if (view.model.getValidCalculation())
	{
            view.setRHpure();
	    if (view.showDisplacement)
	    {
		drawDisplaced(view, beam);
	    }

	    if (view.showMoment)
	    {
		drawStressResultant(view, beam, M);
	    }

	    if (view.showNormalForce)
	    {
		drawStressResultant(view, beam, N);
	    }

	    if (view.showShearForce)
	    {
		drawStressResultant(view, beam, V);
	    }
            view.setRHstandard();
	}

	view.setColor(oldColor);

	drawElement(view, beam);
    }

    static void drawElement(View view, EBSBeam beam)
    {
	Color oldColor = view.getColor();
	double l = beam.getL();
	// Element
	view.move(beam.getN1().getX(), beam.getN1().getZ());
	view.drawThickLine(beam.getN2().getX(), beam.getN2().getZ());

	// gestrichelte Faser
	view.move(beam.getN1().getX(), beam.getN1().getZ());
	view.moveBRel(1.5*beam.getN().getX(), 1.5*beam.getN().getZ());
	view.drawDashedLineRel(beam.getR().getX(), beam.getR().getZ());

	// Verteilte Lasten
	if ((!view.showDisplacement)&&(!view.showMoment)
		&&(!view.showNormalForce)&&(!view.showShearForce))
	{
	    //   Querlast
	    if ((beam.getVi() != 0.) || (beam.getVk() != 0.) )
	    {
		double v = Math.abs((Math.abs(beam.getVi()) > Math.abs(beam.getVk()))?
					(beam.getVi()):(beam.getVk()));
		double pX[] = new double [5];
		double pZ[] = new double [5];
		pX[0] = pX[4] = beam.getN1().getX();
		pZ[0] = pZ[4] = beam.getN1().getZ();
		pX[3] = beam.getN2().getX();
		pZ[3] = beam.getN2().getZ();
		pX[1] = pX[0] - beam.getN().getX()*view.worldScale(view.getBaseSize())*beam.getVi()*4./v;
		pZ[1] = pZ[0] - beam.getN().getZ()*view.worldScale(view.getBaseSize())*beam.getVi()*4./v;
		pX[2] = pX[3] - beam.getN().getX()*view.worldScale(view.getBaseSize())*beam.getVk()*4./v;
		pZ[2] = pZ[3] - beam.getN().getZ()*view.worldScale(view.getBaseSize())*beam.getVk()*4./v;

		view.colorLoad();
		view.fillPolygon(pX, pZ, 5);
		view.colorStd();
	    }
	    //   Längslast
	    if ((beam.getNi() != 0.) || (beam.getNk() != 0.) )
	    {
		double n = Math.abs((Math.abs(beam.getNi()) > Math.abs(beam.getNk()))?
					(beam.getNi()):(beam.getNk()));
		double pX[] = new double [5];
		double pZ[] = new double [5];
		pX[0] = pX[4] = beam.getN1().getX();
		pZ[0] = pZ[4] = beam.getN1().getZ();
		pX[3] = beam.getN2().getX();
		pZ[3] = beam.getN2().getZ();
		pX[1] = pX[0] - beam.getN().getX()*view.worldScale(view.getBaseSize())*beam.getNi()*4./n;
		pZ[1] = pZ[0] - beam.getN().getZ()*view.worldScale(view.getBaseSize())*beam.getNi()*4./n;
		pX[2] = pX[3] - beam.getN().getX()*view.worldScale(view.getBaseSize())*beam.getNk()*4./n;
		pZ[2] = pZ[3] - beam.getN().getZ()*view.worldScale(view.getBaseSize())*beam.getNk()*4./n;

		view.colorLoadN();
		view.fillPolygon(pX, pZ, 5);
		view.colorStd();
	    }
	}

	// Elementgelenke Knoten i
	view.move(beam.getN1().getX(), beam.getN1().getZ());
	view.moveBRel(3.*beam.getR().getX()/l, 3.*beam.getR().getZ()/l);
	if (beam.getHinge(beam.hMi))
	{
	    view.drawBCircle(1.5);
	}
	if (beam.getHinge(beam.hVi))
	{
	    view.moveBRel(-.5*beam.getR().getX()/l, -.5*beam.getR().getZ()/l);
	    view.moveBRel(2.*beam.getN().getX(), 2.*beam.getN().getZ());
	    view.drawBLineRel(-4.*beam.getN().getX(), -4.*beam.getN().getZ());

	    view.move(beam.getN1().getX(), beam.getN1().getZ());
	    view.moveBRel(3.5*beam.getR().getX()/l, 3.5*beam.getR().getZ()/l);
	    view.moveBRel(2.*beam.getN().getX(), 2.*beam.getN().getZ());
	    view.drawBLineRel(-4.*beam.getN().getX(), -4.*beam.getN().getZ());
	}
	if (beam.getHinge(beam.hNi))
	{
	    view.move(beam.getN1().getX(), beam.getN1().getZ());
	    view.moveBRel(1.5*beam.getR().getX()/l, 1.5*beam.getR().getZ()/l);
	    view.moveBRel(1.*beam.getN().getX(), 1.*beam.getN().getZ());
	    view.drawBLineRel(4.*beam.getR().getX()/l, 4.*beam.getR().getZ()/l);

	    view.move(beam.getN1().getX(), beam.getN1().getZ());
	    view.moveBRel(1.5*beam.getR().getX()/l, 1.5*beam.getR().getZ()/l);
	    view.moveBRel(-1.0*beam.getN().getX(), -1.0*beam.getN().getZ());
	    view.drawBLineRel(4.*beam.getR().getX()/l, 4.*beam.getR().getZ()/l);
	}

	// Elementgelenke Knoten k
	view.move(beam.getN2().getX(), beam.getN2().getZ());
	view.moveBRel(-3.*beam.getR().getX()/l, -3.*beam.getR().getZ()/l);
	if (beam.getHinge(beam.hMk))
	{
	    view.drawBCircle(1.5);
	}
	if (beam.getHinge(beam.hVk))
	{
	    view.moveBRel(.5*beam.getR().getX()/l, .5*beam.getR().getZ()/l);
	    view.moveBRel(2.*beam.getN().getX(), 2.*beam.getN().getZ());
	    view.drawBLineRel(-4.*beam.getN().getX(), -4.*beam.getN().getZ());

	    view.move(beam.getN2().getX(), beam.getN2().getZ());
	    view.moveBRel(-3.5*beam.getR().getX()/l, -3.5*beam.getR().getZ()/l);
	    view.moveBRel(2.*beam.getN().getX(), 2.*beam.getN().getZ());
	    view.drawBLineRel(-4.*beam.getN().getX(), -4.*beam.getN().getZ());
	}
	if (beam.getHinge(beam.hNk))
	{
	    view.move(beam.getN2().getX(), beam.getN2().getZ());
	    view.moveBRel(-1.5*beam.getR().getX()/l, -1.5*beam.getR().getZ()/l);
	    view.moveBRel(1.*beam.getN().getX(), 1.*beam.getN().getZ());
	    view.drawBLineRel(-4.*beam.getR().getX()/l, -4.*beam.getR().getZ()/l);

	    view.move(beam.getN2().getX(), beam.getN2().getZ());
	    view.moveBRel(-1.5*beam.getR().getX()/l, -1.5*beam.getR().getZ()/l);
	    view.moveBRel(-1.0*beam.getN().getX(), -1.0*beam.getN().getZ());
	    view.drawBLineRel(-4.*beam.getR().getX()/l, -4.*beam.getR().getZ()/l);
	}

	// Label
	if (!beam.getLabel().equals(""))
	{
	    view.colorLabel();
	    view.move(beam.getN1().getX()+0.5*beam.getR().getX(),
		      beam.getN1().getZ()+0.5*beam.getR().getZ());
	    view.moveBRel(4.*beam.getN().getX(), 4.*beam.getN().getZ());
	    view.drawString(beam.getLabel());
	}

	view.setColor(oldColor);
    }

    
    private static void drawDisplaced(View view, EBSBeam beam)
    {
	/*
	double x1 = beam.getN1().getX()+beam.getV(EBSBeam.hNi)*view.displacementScale;
	double z1 = beam.getN1().getZ()+beam.getV(EBSBeam.hVi)*view.displacementScale;
	double x2 = beam.getN2().getX()+beam.getV(EBSBeam.hNk)*view.displacementScale;
	double z2 = beam.getN2().getZ()+beam.getV(EBSBeam.hVk)*view.displacementScale;
	*/
	double x1 = beam.getN1().getX()+beam.getN1().getDX()*view.displacementScale;
	double z1 = beam.getN1().getZ()+beam.getN1().getDZ()*view.displacementScale;
	double x2 = beam.getN2().getX()+beam.getN2().getDX()*view.displacementScale;
	double z2 = beam.getN2().getZ()+beam.getN2().getDZ()*view.displacementScale;

	double dX = x2-x1;
	double dZ = z2-z1;

	double nX = beam.getN().getX();
	double nZ = beam.getN().getZ();

	view.colorDis();

	view.move(x1 + nX*beam.w(0.)*view.displacementScale,
		  z1 + nZ*beam.w(0.)*view.displacementScale);

	for (double f=displayStep; f<=(1.0+eps); f+=displayStep)
	{
	    view.drawLine(
		x1 + f*dX + nX*beam.w(f)*view.displacementScale,
		z1 + f*dZ + nZ*beam.w(f)*view.displacementScale
	    );
	}


	view.colorStd();
    }

    private static void drawStressResultant(View view, EBSBeam beam, int sr)
    {
	double pX[] = new double [numSeg+4];
	double pZ[] = new double [numSeg+4];

	pX[0] = pX[numSeg+3] = beam.getN1().getX();
	pZ[0] = pZ[numSeg+3] = beam.getN1().getZ();
	pX[numSeg+2] = beam.getN2().getX();
	pZ[numSeg+2] = beam.getN2().getZ();

	double dX = pX[numSeg+2] - pX[0];
	double dZ = pZ[numSeg+2] - pZ[0];

	double nX = beam.getN().getX();
	double nZ = beam.getN().getZ();

	view.colorSectFill();

	if (sr == M)
	{
	    pX[1] = pX[0] + nX*beam.M(0.)*view.momentScale;
	    pZ[1] = pZ[0] + nZ*beam.M(0.)*view.momentScale;
	} else if (sr == N)
	{
	    pX[1] = pX[0] - nX*beam.N(0.)*view.normalForceScale;
	    pZ[1] = pZ[0] - nZ*beam.N(0.)*view.normalForceScale;
	} else {
	    pX[1] = pX[0] - nX*beam.V(0.)*view.shearForceScale;
	    pZ[1] = pZ[0] - nZ*beam.V(0.)*view.shearForceScale;
	}

	int n=2;

	for (double f=displayStep; f<=(1.0+eps); f+=displayStep)
	{
	    if (sr == M)
	    {
		pX[n] = pX[0] + f*dX + nX*beam.M(f)*view.momentScale;
		pZ[n] = pZ[0] + f*dZ + nZ*beam.M(f)*view.momentScale;
	    } else if (sr == N)
	    {
		pX[n] = pX[0] + f*dX - nX*beam.N(f)*view.normalForceScale;
		pZ[n] = pZ[0] + f*dZ - nZ*beam.N(f)*view.normalForceScale;
	    } else {
		pX[n] = pX[0] + f*dX - nX*beam.V(f)*view.shearForceScale;
		pZ[n] = pZ[0] + f*dZ - nZ*beam.V(f)*view.shearForceScale;
	    }

	    n++;
	}

	view.fillPolygon(pX, pZ, numSeg+4);

	view.colorSect();
	view.drawPolygon(pX, pZ, numSeg+4);


	view.colorStd();
    
    }
}
 
