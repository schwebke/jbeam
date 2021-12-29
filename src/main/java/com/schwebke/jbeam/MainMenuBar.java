package com.schwebke.jbeam;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;

/**
 * Legt das Menü an
 */
class MainMenuBar extends JMenuBar
{
    ActionListener listener;

    JCheckBoxMenuItem showDisplacement;
    JCheckBoxMenuItem showMoment;
    JCheckBoxMenuItem showNormalForce;
    JCheckBoxMenuItem showShearForce;

    JCheckBoxMenuItem animateMode;

    public MainMenuBar(ActionListener listener)
    {
      try {
	FileInputStream localeRes = null;
	PropertyResourceBundle locale = null;

	try {
	   localeRes = new FileInputStream("resourcen/locale.txt");
	   locale = new PropertyResourceBundle(localeRes);
	} catch (Exception ex) {
	}
	if (locale == null)
	{
	   URL url = new URL(
			      com.schwebke.jbeam.applet.JBeamApplet.baseUrl +
			      "resourcen/locale.txt"
			   );
	   locale = new PropertyResourceBundle(url.openStream());
	}

	this.listener = listener;
	JMenu m;
	JMenuItem i;

	//File
	m = new JMenu(locale.getString("MenuFile"));
	addMenuItem(m, locale.getString("MItemNew"), "new");
	addMenuItem(m, locale.getString("MItemOpen"), "open");
	addMenuItem(m, locale.getString("MItemSave"), "save");
	m.addSeparator();
	addMenuItem(m, locale.getString("MItemExit"), "exit");
	add(m);

	//Edit
	m = new JMenu(locale.getString("MenuEdit"));
	addMenuItem(m, locale.getString("MItemSelect"), "select");
	addMenuItem(m, locale.getString("MItemInfo"), "info");
	addMenuItem(m, locale.getString("MItemSelectWindow"), "select window");
	addMenuItem(m, locale.getString("MItemProperties"), "properties");
	addMenuItem(m, locale.getString("MItemMove"), "move");
	addMenuItem(m, locale.getString("MItemDelete"), "delete");
	add(m);

	//Preferences
	m = new JMenu(locale.getString("MenuPreferences"));
	addMenuItem(m, locale.getString("MItemSecVals"), "sectional vals");
	addMenuItem(m, locale.getString("MItemSnapGrid"), "snap and grid");
	addMenuItem(m, locale.getString("MItemView"), "view");
	m.addSeparator();
	addMenuItem(m, locale.getString("MItemZoomIn"), "zoom in");
	addMenuItem(m, locale.getString("MItemZoomOut"), "zoom out");
	addMenuItem(m, locale.getString("MItemPan"), "pan");
	addMenuItem(m, locale.getString("MItemZoomWindow"), "zoom window");
	addMenuItem(m, locale.getString("MItemShowAll"), "show all");
	add(m);

	//Node
	m = new JMenu(locale.getString("MenuNode"));
	addMenuItem(m, locale.getString("MItemNodeF"), "free node");
	addMenuItem(m, locale.getString("MItemNodeX"), "x constrained node");
	addMenuItem(m, locale.getString("MItemNodeZ"), "z constrained node");
	addMenuItem(m, locale.getString("MItemNodeXZ"), "xz constrained node");
	m.addSeparator();
	addMenuItem(m, locale.getString("MItemNodeR"), "r constrained node");
	addMenuItem(m, locale.getString("MItemNodeRX"), "rx constrained node");
	addMenuItem(m, locale.getString("MItemNodeRZ"), "rz constrained node");
	addMenuItem(m, locale.getString("MItemNodeRXZ"), "rxz constrained node");
	add(m);

	//Element
	m = new JMenu(locale.getString("MenuElement"));
	addMenuItem(m, locale.getString("MItemTruss"), "truss");
	addMenuItem(m, locale.getString("MItemEBBeam"), "EB beam");
	addMenuItem(m, locale.getString("MItemEBSBeam"), "EBS beam");
	add(m);

	//Calculation
	m = new JMenu(locale.getString("MenuResults"));
	addMenuItem(m, locale.getString("MItemCalculate"), "calculate");
	addMenuItem(m, locale.getString("MItemCalculateModal"), "calculate modal");
	m.addSeparator();
	showDisplacement = new JCheckBoxMenuItem(locale.getString("MItemShowDis"));
	addCheckBoxMenuItem(m, "show displacement", showDisplacement);
	showMoment = new JCheckBoxMenuItem(locale.getString("MItemShowM"));
	addCheckBoxMenuItem(m, "show moment", showMoment);
	showNormalForce = new JCheckBoxMenuItem(locale.getString("MItemShowN"));
	addCheckBoxMenuItem(m, "show normal force", showNormalForce);
	showShearForce = new JCheckBoxMenuItem(locale.getString("MItemShowV"));
	addCheckBoxMenuItem(m, "show shear force", showShearForce);
	m.addSeparator();
	addMenuItem(m, locale.getString("MItemNextMode"), "show next mode");
	addMenuItem(m, locale.getString("MItemPrevMode"), "show prev mode");
	animateMode = new JCheckBoxMenuItem(locale.getString("MItemAnimateMode"));
	addCheckBoxMenuItem(m, "animate mode", animateMode);
	m.addSeparator();
	addMenuItem(m, locale.getString("MItemWriteASC"), "write result");
	addMenuItem(m, locale.getString("MItemWriteHTML"), "write result html");
	add(m);
	

	m = new JMenu("?");
	addMenuItem(m, locale.getString("MItemAbout"), "about");
	add(m);
      } catch (Exception e) {
	  System.out.println("Menu build error: " + e.getMessage() );
      }
    }

    void initMenuBar()
    {
	showDisplacement.setSelected(false);
	showMoment.setSelected(false);
    }

    private void addMenuItem(JMenu menu, String name)
    {
	JMenuItem i;

	i=new JMenuItem(name);
	i.setActionCommand(name);
	i.addActionListener(listener);

	menu.add(i);
    }

    private void addMenuItem(JMenu menu, String name, String cmd)
    {
	JMenuItem i;

	i=new JMenuItem(name);
	i.setActionCommand(cmd);
	i.addActionListener(listener);

	menu.add(i);
    }

    
    private void addCheckBoxMenuItem(JMenu menu, String cmd, JCheckBoxMenuItem i)
    {
	i.setActionCommand(cmd);
	i.addActionListener(listener);

	menu.add(i);
    }

}
