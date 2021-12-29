package com.schwebke.jbeam;

import com.schwebke.jbeam.model.*;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Properties-Dialog für das Euler-Bernoulli-Balken-Element (<code>EBBeam</code>)
 */
public class DlgEBBeam extends JDialog implements ActionListener
{
    private EBBeam beam;
    private JCheckBox hNi;
    private JCheckBox hVi;
    private JCheckBox hMi;
    private JCheckBox hNk;
    private JCheckBox hVk;
    private JCheckBox hMk;
    private JTextField EI;
    private JTextField EA;
    private JTextField m;
    private JTextField vi;
    private JTextField vk;
    private JTextField ni;
    private JTextField nk;
    private JTextField label;
    private boolean OK;


    /**
     * Öffnet das Dialogfenster zur Bearbeitung des übergebenen EBBeam-Elementes
     */
    public DlgEBBeam(Frame parent, EBBeam beam)
    {
	super(parent, "EB Beam", true);
      try {
	PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));
    
	this.beam=beam;

	Container dlg=getContentPane();
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2,2,2,2);
	dlg.setLayout(gb);

	c.fill=GridBagConstraints.BOTH;
	c.gridwidth=GridBagConstraints.REMAINDER;
	c.weightx=1.;

	hNi = new JCheckBox("Ni "+locale.getString("DlgEBBeamHinge"), beam.getHinge(beam.hNi));
	hVi = new JCheckBox("Vi "+locale.getString("DlgEBBeamHinge"), beam.getHinge(beam.hVi));
	hMi = new JCheckBox("Mi "+locale.getString("DlgEBBeamHinge"), beam.getHinge(beam.hMi));
	hNk = new JCheckBox("Nk "+locale.getString("DlgEBBeamHinge"), beam.getHinge(beam.hNk));
	hVk = new JCheckBox("Vk "+locale.getString("DlgEBBeamHinge"), beam.getHinge(beam.hVk));
	hMk = new JCheckBox("Mk "+locale.getString("DlgEBBeamHinge"), beam.getHinge(beam.hMk));

	gb.setConstraints(hNi, c);
	dlg.add(hNi);
	gb.setConstraints(hVi, c);
	dlg.add(hVi);
	gb.setConstraints(hMi, c);
	dlg.add(hMi);
	gb.setConstraints(hNk, c);
	dlg.add(hNk);
	gb.setConstraints(hVk, c);
	dlg.add(hVk);
	gb.setConstraints(hMk, c);
	dlg.add(hMk);

	EI = new JTextField(String.valueOf(beam.getEI()));
	EA = new JTextField(String.valueOf(beam.getEA()));
	m = new JTextField(String.valueOf(beam.getM()));
	vi = new JTextField(String.valueOf(beam.getVi()));
	vk = new JTextField(String.valueOf(beam.getVk()));
	ni = new JTextField(String.valueOf(beam.getNi()));
	nk = new JTextField(String.valueOf(beam.getNk()));
	label = new JTextField(beam.getLabel());

	JLabel l=new JLabel(locale.getString("DlgEBBeamEI"));
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(EI, c);
	dlg.add(EI);

	l=new JLabel(locale.getString("DlgEBBeamEA"));
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

	l=new JLabel("vi");
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(vi, c);
	dlg.add(vi);

	l=new JLabel("vk");
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(vk, c);
	dlg.add(vk);

	l=new JLabel("ni");
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(ni, c);
	dlg.add(ni);

	l=new JLabel("nk");
	c.weightx=0.2;
	c.gridwidth=GridBagConstraints.RELATIVE;
	gb.setConstraints(l, c);
	dlg.add(l);
	c.weightx=1.;
	c.gridwidth=GridBagConstraints.REMAINDER;
	gb.setConstraints(nk, c);
	dlg.add(nk);

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
     * Action-Handler für die OK- und Cancel-Buttons
     */
    public void actionPerformed(ActionEvent event)
    {
	boolean canClose=true;
	OK = false;
	if (event.getActionCommand().equals("OK"))
	{
	    OK = true;
	    beam.setHinge(beam.hNi, hNi.isSelected());
	    beam.setHinge(beam.hVi, hVi.isSelected());
	    beam.setHinge(beam.hMi, hMi.isSelected());
	    beam.setHinge(beam.hNk, hNk.isSelected());
	    beam.setHinge(beam.hVk, hVk.isSelected());
	    beam.setHinge(beam.hMk, hMk.isSelected());
	    beam.setLabel(label.getText());
	    try {
		beam.setEI((new Double(EI.getText())).doubleValue());
		beam.setEA((new Double(EA.getText())).doubleValue());
		beam.setM((new Double(m.getText())).doubleValue());
		beam.setVi((new Double(vi.getText())).doubleValue());
		beam.setVk((new Double(vk.getText())).doubleValue());
		beam.setNi((new Double(ni.getText())).doubleValue());
		beam.setNk((new Double(nk.getText())).doubleValue());
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
     * Liefert <b>true</b>, wenn der Dialog über 'OK' beendet wurde.
     */
    public boolean getOK()
    {
	return OK;
    }

}
 
