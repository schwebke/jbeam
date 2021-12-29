package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Properties-Dialog für Knoten
 */
public class DlgNode extends JDialog implements ActionListener
{
    private Node node;
    private JCheckBox cX;
    private JCheckBox cZ;
    private JCheckBox cR;
    private JTextField x;
    private JTextField z;
    private JTextField Fx;
    private JTextField Fz;
    private JTextField M;
    private JTextField label;
    private boolean OK;


    /**
     * Öffnet den Dialog und nimmt Einstellungen am Übergenen <code>Node</code> vor.
     */
    public DlgNode(Frame parent, Node node)
    {
	super(parent, "Edit Node", true);

      try {
        PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));

	this.node=node;

	Container dlg=getContentPane();
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2,2,2,2);
	dlg.setLayout(gb);

	c.fill=GridBagConstraints.BOTH;
	c.gridwidth=GridBagConstraints.REMAINDER;
	c.weightx=1.;

	cX = new JCheckBox(locale.getString("DlgNodeCX"), node.getCX());
	cZ = new JCheckBox(locale.getString("DlgNodeCZ"), node.getCZ());
	cR = new JCheckBox(locale.getString("DlgNodeCR"), node.getCR());

	gb.setConstraints(cX, c);
	dlg.add(cX);
	gb.setConstraints(cZ, c);
	dlg.add(cZ);
	gb.setConstraints(cR, c);
	dlg.add(cR);

	x = new JTextField(String.valueOf(node.getX()));
	z = new JTextField(String.valueOf(node.getZ()));
	Fx= new JTextField(String.valueOf(node.getFx()));
	Fz= new JTextField(String.valueOf(node.getFz()));
	M = new JTextField(String.valueOf(node.getM()));
	label = new JTextField(node.getLabel());

	JLabel l=new JLabel("X");
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(x, c);
	dlg.add(x);

	l=new JLabel("Z");
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(z, c);
	dlg.add(z);

	l=new JLabel(locale.getString("DlgNodeForceX"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(Fx, c);
	dlg.add(Fx);

	l=new JLabel(locale.getString("DlgNodeForceZ"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(Fz, c);
	dlg.add(Fz);

	l=new JLabel(locale.getString("DlgNodeMoment"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(M, c);
	dlg.add(M);

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
	  System.out.println("DlgNode build error: " + e.getMessage() );
      }

    }

    /**
     * Action-Handler für die 'OK'- und 'Cancel'-Buttons
     */
    public void actionPerformed(ActionEvent event)
    {
	boolean canClose=true;
	OK = false;
	if (event.getActionCommand().equals("OK"))
	{
	    OK = true;
	    node.setCX(cX.isSelected());
	    node.setCZ(cZ.isSelected());
	    node.setCR(cR.isSelected());
	    node.setLabel(label.getText());
	    try {
		node.setX((new Double(x.getText())).doubleValue());
		node.setZ((new Double(z.getText())).doubleValue());
		node.setFx((new Double(Fx.getText())).doubleValue());
		node.setFz((new Double(Fz.getText())).doubleValue());
		node.setM((new Double(M.getText())).doubleValue());
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
 
