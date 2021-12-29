package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;
import com.schwebke.awt.tools.display.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Dialog zur Konfiguration des View-Fensters
 */
public class DlgInfo extends JDialog implements ActionListener
{
    /**
     * Zeigt Informationen zum übergebenen Object an
     */
    public DlgInfo(JBeam jbeam, JFrame parent, Model model, Object obj)
    {
	super(parent, "Info", true);

      try {
        PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));

	Container content = this.getContentPane();
	content.setLayout(new BorderLayout());

	Display display = new Display(new Dimension(300, 400));
	List info = display.getContent();

	Font ues = new Font("SansSerif", Font.BOLD, 14);
	Font sues = new Font("SansSerif", Font.PLAIN, 12);
	HMoveTo tab1 = new HMoveTo(90);

	if (obj instanceof Node)
	{
	   Node node = (Node)obj;

	   Text t = new Text(locale.getString("DlgInfoNode"));
	   t.setFont(ues);
	   info.add(t);
	   
	   if (!node.getLabel().equals(""))
	   {
	      t.setText(t.getText()+" "+node.getLabel());
	   }

	   if (model.getValidCalculation())
	   {
	      if ((!node.getCX()) || (!node.getCZ()) || (!node.getCR()))
	      {
		 info.add(new NewLine(4));
		 info.add(t = new Text(locale.getString("DlgInfoDisplacements")));
		 t.setFont(sues);

		 if (!node.getCX())
		 {
		    info.add(new NewLine());
		    info.add(new Text("dX"));
		    info.add(tab1);
		    info.add(new Text(jbeam.format(node.getDX())));
		 }

		 if (!node.getCZ())
		 {
		    info.add(new NewLine());
		    info.add(new Text("dZ"));
		    info.add(tab1);
		    info.add(new Text(jbeam.format(node.getDZ())));
		 }

		 if (!node.getCR())
		 {
		    info.add(new NewLine());
		    info.add(new Text("dR"));
		    info.add(tab1);
		    info.add(new Text(jbeam.format(node.getDR())));
		 }
	      }


	      if (node.getCX() || node.getCZ() || node.getCR())
	      {
		 info.add(new NewLine(4));
		 info.add(t = new Text(locale.getString("DlgInfoSupportReactions")));
		 t.setFont(sues);

		 if (node.getCX())
		 {
		    info.add(new NewLine());
		    info.add(new Text("Fx"));
		    info.add(tab1);
		    info.add(new Text(jbeam.format(node.getRFx())));
		 }
		 if (node.getCZ())
		 {
		    info.add(new NewLine());
		    info.add(new Text("Fz"));
		    info.add(tab1);
		    info.add(new Text(jbeam.format(node.getRFz())));
		 }
		 if (node.getCR())
		 {
		    info.add(new NewLine());
		    info.add(new Text("M"));
		    info.add(tab1);
		    info.add(new Text(jbeam.format(node.getRM())));
		 }
	      }
	   } else {
	      info.add(new NewLine());
	      info.add(new Text(locale.getString("DlgInfoNoResults")));
	   }
	}
	if (obj instanceof Truss)
	{
	   Truss truss = (Truss)obj;

	   Text t = new Text(locale.getString("DlgInfoTruss"));
	   t.setFont(ues);
	   info.add(t);

	   if (model.getValidCalculation())
	   {
	      info.add(new NewLine(4));
	      info.add(t = new Text(locale.getString("DlgInfoNormalForces")));
	      t.setFont(sues);

	      info.add(new NewLine());
	      info.add(new Text("N1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(truss.N(0.))));

	      info.add(new NewLine());
	      info.add(new Text("N2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(truss.N(1.))));
	   } else {
	      info.add(new NewLine());
	      info.add(new Text(locale.getString("DlgInfoNoResults")));
	   }
	}
	if (obj instanceof EBBeam)
	{
	   EBBeam beam = (EBBeam)obj;
	   EBSBeam beams = null;

	   Text t = new Text(locale.getString("DlgInfoEBBeam"));
	   if (obj instanceof EBSBeam)
	   {
	      t = new Text(locale.getString("DlgInfoEBSBeam"));
	      beams = (EBSBeam)obj;
	   }
	   t.setFont(ues);
	   info.add(t);

	   if (model.getValidCalculation())
	   {
	      info.add(new NewLine(4));
	      info.add(t = new Text(locale.getString("DlgInfoDisplacements")));
	      t.setFont(sues);

	      info.add(new NewLine());
	      info.add(new Text("dX1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.getV(0))));

	      info.add(new NewLine());
	      info.add(new Text("dZ1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.getV(1))));

	      info.add(new NewLine());
	      info.add(new Text("dR1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.getV(2))));

	      info.add(new NewLine());
	      info.add(new Text("dX2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.getV(3))));

	      info.add(new NewLine());
	      info.add(new Text("dZ2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.getV(4))));

	      info.add(new NewLine());
	      info.add(new Text("dR2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.getV(5))));

	      if (beams != null)
	      {
		 double fMaxDis = beams.fDisplaceMax();
		 double maxDis = beams.displace(fMaxDis);
		 double fMinDis = beams.fDisplaceMin();
		 double minDis = beams.displace(fMinDis);

		 info.add(new NewLine());
		 info.add(new Text(locale.getString("DlgInfoMaxDisplace")));
		 info.add(tab1);
		 info.add(new Text(jbeam.format(maxDis)+" "+
		                   locale.getString("DlgInfoAt")+" "+
		                   jbeam.format(fMaxDis*beam.getL())));

		 info.add(new NewLine());
		 info.add(new Text(locale.getString("DlgInfoMinDisplace")));
		 info.add(tab1);
		 info.add(new Text(jbeam.format(minDis)+" "+
		                   locale.getString("DlgInfoAt")+" "+
		                   jbeam.format(fMinDis*beam.getL())));
	      }


	      info.add(new NewLine(4));
	      info.add(t = new Text(locale.getString("DlgInfoMoments")));
	      t.setFont(sues);

	      info.add(new NewLine());
	      info.add(new Text("M1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.M(0.))));

	      info.add(new NewLine());
	      info.add(new Text("M2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.M(1.))));

	      double fMinM = beam.fMinM();
	      double fMaxM = beam.fMaxM();

	      info.add(new NewLine());
	      info.add(new Text("Mmin"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.M(fMinM))+" "+
	                        locale.getString("DlgInfoAt")+" "+
	                        jbeam.format(fMinM*beam.getL())));

	      info.add(new NewLine());
	      info.add(new Text("Mmax"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.M(fMaxM))+" "+
	                        locale.getString("DlgInfoAt")+" "+
	                        jbeam.format(fMaxM*beam.getL())));


	      info.add(new NewLine(4));
	      info.add(t = new Text(locale.getString("DlgInfoShearForces")));
	      t.setFont(sues);

	      info.add(new NewLine());
	      info.add(new Text("V1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.V(0.))));

	      info.add(new NewLine());
	      info.add(new Text("V2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.V(1.))));

	      double fMinV = beam.fMinV();
	      double fMaxV = beam.fMaxV();

	      info.add(new NewLine());
	      info.add(new Text("Vmin"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.V(fMinV))+" "+
				locale.getString("DlgInfoAt")+" "+
	                        jbeam.format(fMinV*beam.getL())));

	      info.add(new NewLine());
	      info.add(new Text("Vmax"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.V(fMaxV))+" "+
			        locale.getString("DlgInfoAt")+" "+
	                        jbeam.format(fMaxV*beam.getL())));


	      info.add(new NewLine(4));
	      info.add(t = new Text(locale.getString("DlgInfoNormalForces")));
	      t.setFont(sues);

	      info.add(new NewLine());
	      info.add(new Text("N1"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.N(0.))));

	      info.add(new NewLine());
	      info.add(new Text("N2"));
	      info.add(tab1);
	      info.add(new Text(jbeam.format(beam.N(1.))));
	   } else {
	      info.add(new NewLine());
	      info.add(new Text(locale.getString("DlgInfoNoResults")));
	   }
	}

	content.add(display, "Center");

	JButton button;

	JPanel bbar = new JPanel();
	bbar.setLayout(new GridLayout(0,1));

	button = new JButton(locale.getString("DlgOK"));
	button.addActionListener(this);
	bbar.add(button);

	content.add(bbar, "South");

	if (parent != null)
	{
	   setLocation(parent.getLocation().x+100, parent.getLocation().y+100);
	} else {
	   setLocation(300, 200);
	}
	setResizable(false);

	pack();
	setSize(350, getSize().height);
	setVisible(true);
      } catch (Exception e) {
	  System.out.println("DlgInfo build error: " + e.getMessage() );
      }
    }

    public void actionPerformed(ActionEvent event)
    {
	boolean canClose=true;
	if (event.getActionCommand().equals("OK"))
	{
	}
	if (canClose)
	{
	    setVisible(false);
	    dispose();
	}
    }
}
 
