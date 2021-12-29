package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Properties-Dialog für <code>Truss</code>-Elemente
 */
public class DlgTruss extends JDialog implements ActionListener
{
    private Truss truss;
    private JTextField EA;
    private JTextField m;
    private JTextField label;
    private boolean OK;


    /**
     * Öffnet den Dialog und nimmt Einstellungen am übergebenen Truss-Element vor.
     */
    public DlgTruss(Frame parent, Truss truss)
    {
	super(parent, "Edit Truss", true);

      try {
        PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));
	
	this.truss=truss;

	Container dlg=getContentPane();
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2,2,2,2);
	c.fill=GridBagConstraints.BOTH;
	dlg.setLayout(gb);

	EA = new JTextField(String.valueOf(truss.getEA()));
	m = new JTextField(String.valueOf(truss.getM()));
	label = new JTextField(truss.getLabel());
	
	JLabel l=new JLabel(locale.getString("DlgTrussEA"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(EA, c);
	dlg.add(EA);

	l=new JLabel(locale.getString("DlgM"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(m, c);
	dlg.add(m);

	l=new JLabel(locale.getString("DlgLabel"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(label, c);
	dlg.add(label);

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
	  System.out.println("DlgEBBeam build error: " + e.getMessage() );
      }
    }

    /**
     * Action-Handler für 'OK'- und 'Cancel'-Buttons
     */
    public void actionPerformed(ActionEvent event)
    {
	boolean canClose=true;
	OK = false;
	if (event.getActionCommand().equals("OK"))
	{
	    OK = true;
	    truss.setLabel(label.getText());
	    try {
		truss.setEA((new Double(EA.getText())).doubleValue());
		truss.setM((new Double(m.getText())).doubleValue());
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

    /**
     * Liefert <b>true</b>, wenn der Dialog mit OK beendet wurde.
     */
    public boolean getOK()
    {
	return OK;
    }
}
 
