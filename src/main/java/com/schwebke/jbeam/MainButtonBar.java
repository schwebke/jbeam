package com.schwebke.jbeam;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Legt die Button-Leiste an
 */
class MainButtonBar extends JToolBar
{
    ActionListener listener;
    public MainButtonBar(ActionListener listener)
    {
	this.listener=listener;

	// Buttons erzeugen


      try {
	PropertyResourceBundle locale =
	   new PropertyResourceBundle(JBeam.getResourceAsStream("locale.txt"));

	addButton("New.gif", locale.getString("ButtonNew"), "new");
	addButton("Open.gif", locale.getString("ButtonOpen"), "open");
	addButton("Save.gif", locale.getString("ButtonSave"), "save");

	addSeparator();

	//    Edit
	addButton("Select.gif", locale.getString("ButtonSelect"), "select");
	addButton("SelectWindow.gif", locale.getString("ButtonSelectWindow"), "select window");
	addButton("Properties.gif", locale.getString("ButtonProperties"), "properties");
	addButton("Move.gif", locale.getString("ButtonMove"), "move");
	addButton("Delete.gif", locale.getString("ButtonDelete"), "delete");

	addSeparator();

	addButton("Info.gif", locale.getString("ButtonInfo"), "info");

	addSeparator();

	addButton("ZoomIn.gif", locale.getString("ButtonZoomIn"), "zoom in");
	addButton("ZoomOut.gif", locale.getString("ButtonZoomOut"), "zoom out");
	addButton("Pan.gif", locale.getString("ButtonPan"), "pan");
	addButton("ZoomWindow.gif", locale.getString("ButtonZoomWindow"), "zoom window");
	addButton("ShowAll.gif", locale.getString("ButtonShowAll"), "show all");

	addSeparator();

	//    FreeNode
	addButton("FreeNode.gif", locale.getString("ButtonFNode"), "free node");

	//    r constrained node
	addButton("RNode.gif", locale.getString("ButtonRNode"), "r constrained node");

	addSeparator();

	addButton("Truss.gif", locale.getString("ButtonTruss"), "truss");
	addButton("EBBeam.gif", locale.getString("ButtonEBBeam"), "EB beam");
	addButton("EBSBeam.gif", locale.getString("ButtonEBSBeam"), "EBS beam");

      } catch (Exception e) {
	  System.out.println("ButtonBar build error: " + e.getMessage() );
      }


    }

    void initButtonBar()
    {
    }

    void addButton(String iconName, String command)
    {
	ImageIcon icon = null;
	try {
	   icon = new ImageIcon("resourcen/"+iconName);
	} catch (Exception e) {
	}
	try {
	   if (icon == null)
	   {
	      icon = new ImageIcon(
			new URL(
			   com.schwebke.jbeam.applet.JBeamApplet.baseUrl +
			   "resourcen/"+iconName));
	   }
	} catch (Exception e2) {
	}
	JButton button = new JButton(icon);
	//button.setMargin(new Insets(1, 1, 1, 1));
	button.setToolTipText(command);
	button.setActionCommand(command);
	button.addActionListener(listener);
	add(button);
    }

    void addButton(String iconName, String tooltip, String command)
    {
	ImageIcon icon = null;
	try {
	   icon = new ImageIcon("resourcen/"+iconName);
	} catch (Exception e) {
	}
	try {
	   if (icon == null)
	   {
	      icon = new ImageIcon(
			new URL(
			   com.schwebke.jbeam.applet.JBeamApplet.baseUrl +
			   "resourcen/"+iconName));
	   }
	} catch (Exception e2) {
	}
	JButton button = new JButton(icon);
	//button.setMargin(new Insets(1, 1, 1, 1));
	button.setToolTipText(tooltip);
	button.setActionCommand(command);
	button.addActionListener(listener);
	add(button);
    }
}

