package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Dialog zur Einstellung des optischen und des Fang-Rasters, sowie des
 * Check-Radius (Toleranzradius bei Selektion von Objekten)
 */
public class DlgGrid extends JDialog implements ActionListener
{
    private JBeam controller;

    private JTextField vGrid;
    private JTextField sGrid;
    private JTextField checkR;


    /**
     * Öffnet den Dialog und nimmt Einstellungen am übergebenen Controller vor
     */
    public DlgGrid(Frame parent, JBeam controller)
    {
	super(parent, "Grid", true);
	this.controller=controller;

      try {
        PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));

	Container dlg=getContentPane();
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2,2,2,2);
	c.fill=GridBagConstraints.BOTH;
	dlg.setLayout(gb);

	vGrid = new JTextField(String.valueOf(controller.view.getGrid()));
	JLabel l=new JLabel(locale.getString("DlgGridVisibleGrid"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(vGrid, c);
	dlg.add(vGrid);

	sGrid = new JTextField(String.valueOf(controller.grid));
	l=new JLabel(locale.getString("DlgGridSnapGrid"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(sGrid, c);
	dlg.add(sGrid);

	checkR = new JTextField(String.valueOf(controller.checkR));
	l=new JLabel(locale.getString("DlgGridCheckRadius"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(checkR, c);
	dlg.add(checkR);

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
	  System.out.println("DlgGrid build error: " + e.getMessage() );
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
		controller.grid=(new Double(sGrid.getText())).doubleValue();
		controller.view.setGrid((new Double(vGrid.getText())).doubleValue());
		controller.checkR=(new Integer(checkR.getText())).intValue();
		if (controller.checkR<0)
		{
		    throw new NumberFormatException();
		}
		if (controller.grid<0.)
		{
		    throw new NumberFormatException();
		}
		if (controller.view.getGrid()<0.)
		{
		    throw new NumberFormatException();
		}
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
 
