package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Dialog zur Einstellung der Querschnittswerte neuer Elemente
 */
public class DlgSectionalVals extends JDialog implements ActionListener
{
    private JBeam controller;

    private JTextField EA;
    private JTextField EI;
    private JTextField GAs;
    private JTextField m;


    /**
     * Öffnet den Dialog und nimmt Einstellungen am übergebenen Controller vor.
     */
    public DlgSectionalVals(Frame parent, JBeam controller)
    {
	super(parent, "Sectional vals", true);

      try {
        PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));

	this.controller=controller;

	Container dlg=getContentPane();
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2,2,2,2);
	c.fill=GridBagConstraints.BOTH;
	dlg.setLayout(gb);

	EA = new JTextField(String.valueOf(controller.EA));
	JLabel l=new JLabel(locale.getString("DlgSecValsDefaultEA"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(EA, c);
	dlg.add(EA);

	EI = new JTextField(String.valueOf(controller.EI));
	l=new JLabel(locale.getString("DlgSecValsDefaultEI"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(EI, c);
	dlg.add(EI);

	GAs = new JTextField(String.valueOf(controller.GAs));
	l=new JLabel(locale.getString("DlgSecValsDefaultGAs"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(GAs, c);
	dlg.add(GAs);

	m = new JTextField(String.valueOf(controller.m));
	l=new JLabel(locale.getString("DlgSecValsDefaultM"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(m, c);
	dlg.add(m);

	JButton button;

	JPanel bbar = new JPanel();

	button = new JButton(locale.getString("DlgOK"));
	button.addActionListener(this);
	bbar.add(button);

	button = new JButton(locale.getString("DlgCancel"));
	button.addActionListener(this);
	bbar.add(button);

	gb.setConstraints(bbar, c);
	dlg.add(bbar);

	if (parent != null)
	{
	   setLocation(parent.getLocation().x+100, parent.getLocation().y+100);
	} else {
	   setLocation(300, 200);
	}
	setResizable(false);

	pack();
	setVisible(true);

      } catch (Exception e) {
	  System.out.println("DlgSectionalVals build error: " + e.getMessage() );
      }
    }

    /**
     * Action-Handler für die 'OK'- und 'Cancel'-Buttons
     */
    public void actionPerformed(ActionEvent event)
    {
	boolean canClose=true;
	if (event.getActionCommand().equals("OK"))
	{
	    try {
		controller.EA=(new Double(EA.getText())).doubleValue();
		controller.EI=(new Double(EI.getText())).doubleValue();
		controller.GAs=(new Double(GAs.getText())).doubleValue();
		controller.m=(new Double(m.getText())).doubleValue();
	    } catch (NumberFormatException e) {
		canClose=false;
		JOptionPane.showMessageDialog(null, 
			    "Please enter a valid number",
			    "Number Format Error", 
			    JOptionPane.ERROR_MESSAGE);
	    }
	}
	if (canClose)
	{
	    setVisible(false);
	    dispose();
	}
    }
}
 
