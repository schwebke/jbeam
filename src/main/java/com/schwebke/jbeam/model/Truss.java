package com.schwebke.jbeam.model;

import com.schwebke.jbeam.math.*;
import com.schwebke.math.*;

public class Truss extends Beam
{
    static final long serialVersionUID = -356625506417692745L;

    //Properties
    //
    // sectional vals
    protected double EA;

    public Truss(Node n1, Node n2, double EA, double m)
    {
	super(n1, n2, m);
	this.EA = EA;
    }

    public double getEA()
    {
	return EA;
    }

    public void setEA(double EA)
    {
	this.EA = EA;
    }

    public void calSg()
    {
	double l=Math.sqrt((n1.x-n2.x)*(n1.x-n2.x)+(n1.z-n2.z)*(n1.z-n2.z));
	Sl=new double[6][6];
	for (int i=0; i<6; i++)
	{
	    for (int j=0; j<6; j++)
	    {
		Sl[i][j]=0.;
	    }
	}
	Sl[0][0]=Sl[3][3]=EA/l;
	Sl[0][3]=Sl[3][0]=-EA/l;

	calMl();
	transform();
    }

    public void postCalculate()
    {
	// Rückrechnung der Stabendschnittgrößen
	double V[][] = new double [6][1];
	V[0][0] = n1.getDX();
	V[1][0] = n1.getDZ();
	V[2][0] = n1.getDR();
	V[3][0] = n2.getDX();
	V[4][0] = n2.getDZ();
	V[5][0] = n2.getDR();

	double Rl[][] =
	    Matrix.multiply(
		Matrix.multiply(
		    Sl,
		    a
		),
		V
	    );

	Ni = -Rl[0][0];
	Vi = -Rl[1][0];
	Mi = Rl[2][0];
	Nk = Rl[3][0];
	Vk = Rl[4][0];
	Mk = Rl[5][0];

	//System.out.println(" Ni = "+Ni);
	//System.out.println(" Nk = "+Nk);
    }

    public double w(double f)
    {
	return 0.;
    }

    public double N(double f)
    {
	return f*Nk+(1.-f)*Ni;
    }

} 
