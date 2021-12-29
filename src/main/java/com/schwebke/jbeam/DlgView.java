package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Dialog zur Konfiguration des View-Fensters
 */
public class DlgView extends JDialog implements ActionListener
{
    private JBeam controller;

    private JTextField baseSize;
    private JTextField displacementScale;
    private JTextField momentScale;
    private JTextField normalForceScale;
    private JTextField shearForceScale;


    /**
     * Öffnet den Dialog und nimmt Einstellungen am View des übergebenen Controllers vor.
     */
    public DlgView(Frame parent, JBeam controller)
    {
	super(parent, "Results", true);
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

	baseSize = new JTextField(String.valueOf(controller.view.getBaseSize()));
	JLabel l=new JLabel(locale.getString("DlgViewBaseSize"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(baseSize, c);
	dlg.add(baseSize);

	displacementScale = new JTextField(String.valueOf(controller.view.getDisplacementScale()));
	l=new JLabel(locale.getString("DlgViewDisplacementScale"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(displacementScale, c);
	dlg.add(displacementScale);

	momentScale = new JTextField(String.valueOf(controller.view.getMomentScale()));
	l=new JLabel(locale.getString("DlgViewMomentScale"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(momentScale, c);
	dlg.add(momentScale);

	normalForceScale = new JTextField(String.valueOf(controller.view.getNormalForceScale()));
	l=new JLabel(locale.getString("DlgViewNormalForceScale"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(normalForceScale, c);
	dlg.add(normalForceScale);

	shearForceScale = new JTextField(String.valueOf(controller.view.getShearForceScale()));
	l=new JLabel(locale.getString("DlgViewShearForceScale"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(shearForceScale, c);
	dlg.add(shearForceScale);

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
	setSize(350, getSize().height);
	setVisible(true);
      } catch (Exception e) {
	  System.out.println("DlgEBBeam build error: " + e.getMessage() );
      }
    }

    public void actionPerformed(ActionEvent event)
    {
	boolean canClose=true;
	if (event.getActionCommand().equals("OK"))
	{
	    try {
		controller.view.setBaseSize((new Double(baseSize.getText())).doubleValue());
		controller.view.setDisplacementScale((new Double(displacementScale.getText())).doubleValue());
		controller.view.setMomentScale((new Double(momentScale.getText())).doubleValue());
		controller.view.setNormalForceScale((new Double(normalForceScale.getText())).doubleValue());
		controller.view.setShearForceScale((new Double(shearForceScale.getText())).doubleValue());
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
 
