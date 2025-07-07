/*
    JBeam - a structural analysis program for the Java platform

    Copyright (C) 1998 Kai Gerd Schwebke

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package com.schwebke.jbeam;
// project-specific packages
import com.schwebke.jbeam.view.*;
import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.tool.*;
import com.schwebke.jbeam.plugin.*;
import com.schwebke.jbeam.persistence.*;

// external packages
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main window class of the application (Controller)
 *
 * @author Kai Gerd Schwebke
 *
 * @version 2.1.0
 */
public class JBeam implements IHost, IController
{
    /**
      * Instance of the View. (graphical editing window)
      */
    View view;
    /**
      * Instance of the Model.
      */
    SelectableModel model;
    /**
     * Formatter for floating point numbers (default 3 decimal places)
     */
    NumberFormat numberFormat;

    private JFrame mainFrame;
    private MainMenuBar menuBar;
    private MainButtonBar buttonBar;
    private Launcher launcher;
    private JLabel status;
    private JLabel mode;
    private JLabel posX;
    private JLabel posY;
    private String addElementType;

    public final static String version="4.1.0";
    public final static String verdate="2025-07-06";

    // last directory for FileChoose
    File fileChooseDir;

    /** Snap grid */
    double grid;

    /** Checkradius */
    int checkR;

    /** Default cross-section values for newly created objects */
    double EA;
    double EI;
    double GAs;
    double m;


    /** true if the program is locked for user input (during remote controlling)*/
    private boolean lock;


    /**
     * Opens an application main window. On exit, the <code>exit</code> method
     * of the passed launcher is called.
     */
    public JBeam(Launcher launcher)
    {
	System.out.println("JBeam " + version + " / " + verdate);
        
        com.schwebke.CatchingEventQueue.installErrorDialogueHandler();
        
        fileChooseDir = new File(System.getProperty("user.dir"));

	numberFormat = NumberFormat.getNumberInstance();
	numberFormat.setMinimumFractionDigits(3);
	numberFormat.setMaximumFractionDigits(3);

	addElementType = null;
	lock = false;

	// Launcher registrieren
	// (Launcher kriegt von uns einen Rückruf, wenn der Benutzer
	//  dieses Fenster schließt.)
	this.launcher=launcher;

	// Hauptfenster der Anwendung erzeugen
	mainFrame=new JFrame("JBeam");
	Image icon = mainFrame.getToolkit().getImage("resourcen/icon.gif");
	mainFrame.setIconImage(icon);
	mainFrame.setLocation(200, 100);

	// Model erzeugen
	model=new SelectableModel();

	// automatisches Zerstören des Fensters durch Benutzer verbieten
	// und stattdessen WindowEvents selber verarbeiten
	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	mainFrame.addWindowListener(new JBeamWindowListener());

	// Menü erzeugen und ActionListener für Menü registrieren
	mainFrame.setJMenuBar(menuBar = new MainMenuBar(new JBeamActionListener()));

	// Layoutmanager ist BorderLayout
	mainFrame.getContentPane().setLayout(new BorderLayout());

	// Erzeugen des graphisch-interaktiven Editorfensters
	//  JScrollPane->JViewport->JBeamView
	view=new View(model, this);
	view.addMouseListener(new JBeamViewMouseListener());
	view.addMouseMotionListener(new JBeamViewMouseMotionListener());
        view.addMouseWheelListener(new JBeamViewMouseWheelListener());
	view.setPreferredSize(new Dimension(600, 400));


	// Statuszeile erzeugen
	JPanel sbar = new JPanel(new FlowLayout(FlowLayout.LEFT));


	// Vorbelegen der Statuslabels mit einem String angemessener Breite,
	// damit der Packer ein gutes Layout erzeugen kann.
	// Die jetzt erzielte Breite wird später als 'preferredSize' gespeichert.
	status = new JLabel("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
	sbar.add(status);

	posX = new JLabel("XXXXXXXXXX");
	posY = new JLabel("XXXXXXXXXX");
	sbar.add(posX);
	sbar.add(posY);

	mode = new JLabel("XXXXXXXXXXXXXXXXXXXX");
	sbar.add(mode);
	

	// Button-Bar erzeugen
	buttonBar = new MainButtonBar(new JBeamActionListener());

	// Defaults laden
	initApp();

	// Statuszeile einfügen
	mainFrame.getContentPane().add("South", sbar);

	// Editorfenster einfügen
	mainFrame.getContentPane().add("Center", view);

	// ButtonBar einfügen
	mainFrame.getContentPane().add("North", buttonBar);

	// Layouten und sichtbarmachen des Hauptfensters
	mainFrame.pack();
	mainFrame.setVisible(true);


	// Größe der Status-Label (mit 10*'X') merken
	status.setPreferredSize(status.getSize());
	mode.setPreferredSize(mode.getSize());
	posX.setPreferredSize(posX.getSize());
	posY.setPreferredSize(posY.getSize());

	// Belegen der Status-Label mit dem tatsächlichen Text
	status.setText("ready");
	mode.setText("select");
	posX.setText("X: -");
	posY.setText("Z: -");
        
    }

    /** Initialisiert eine JBeam-Appletinstanz */
    public JBeam(com.schwebke.jbeam.applet.JBeamApplet jbApplet)
    {
	numberFormat = NumberFormat.getNumberInstance();
	numberFormat.setMinimumFractionDigits(3);
	numberFormat.setMaximumFractionDigits(3);

	addElementType = null;
	lock = false;

	// Launcher deregistrieren
	// (dieses Fenster kann nicht vom Benutzer geschlossen werden)
	this.launcher=null;

	// Hauptfenster der Anwendung existiert nicht
	// (jedenfalls vom Java-API-Standpunkt aus - JApplet taugt
	//  nicht als Parent für Dialoge. Hier darf aber null übergeben
	//  werden.)
	mainFrame=null;

	// Model erzeugen
	model=new SelectableModel();

	// Menü erzeugen und ActionListener für Menü registrieren
	jbApplet.setJMenuBar(menuBar = new MainMenuBar(new JBeamActionListener()));

	// Layoutmanager ist BorderLayout
	jbApplet.getContentPane().setLayout(new BorderLayout());

	// Erzeugen des graphisch-interaktiven Editorfensters
	//  JScrollPane->JViewport->JBeamView
	view=new View(model, this);
	view.addMouseListener(new JBeamViewMouseListener());
	view.addMouseMotionListener(new JBeamViewMouseMotionListener());
        view.addMouseWheelListener(new JBeamViewMouseWheelListener());
	//view.setPreferredSize(new Dimension(600, 400));


	// Statuszeile erzeugen
	JPanel sbar = new JPanel(new FlowLayout(FlowLayout.LEFT));


	// Erzeugen der Statuslabels
	status = new JLabel();
	sbar.add(status);

	posX = new JLabel();
	posY = new JLabel();
	sbar.add(posX);
	sbar.add(posY);

	mode = new JLabel();
	sbar.add(mode);
	

	// Button-Bar erzeugen
	buttonBar = new MainButtonBar(new JBeamActionListener());

	// Defaults laden
	initApp();

	// Statuszeile einfügen
	jbApplet.getContentPane().add("South", sbar);

	// Editorfenster einfügen
	jbApplet.getContentPane().add("Center", view);

	// ButtonBar einfügen
	jbApplet.getContentPane().add("North", buttonBar);

	// Belegen der Status-Label mit dem initialen Text
	status.setText("ready");
	mode.setText("select");
	posX.setText("X: -");
	posY.setText("Z: -");
    }


    private void initApp()
    {
	grid=1.;
	checkR=5;
	EA=100000.;
	EI=10000.;
	GAs=1.E15;
	m=1.;

	menuBar.initMenuBar();
	buttonBar.initButtonBar();
    }

    /**
      * Klasse zum Verarbeiten von Fensterereignissen.
      * Abfangen des Schließen-Buttons
      */
    class JBeamWindowListener extends WindowAdapter
    {
	public void windowClosing(WindowEvent event)
	{
	    if (!lock)
	    {
		exitApp();
	    }
	}
    }

    /**
      * Klasse zum Verarbeiten von Menü-, Buttonbar- und Timer-Events
      */
    class JBeamActionListener implements ActionListener
    {
	// timer für Animation der Eigenform
	javax.swing.Timer timer;

	// Animation an/aus
	boolean frozen;

	// aktueller Zeitindex
	double time;

	// Zeitschritt
	double timeStep;

	// Amplitude
	double baseScale;

	JBeamActionListener()
	{
	   timer = new javax.swing.Timer(50, this);
	   timer.setInitialDelay(0);
	   timer.setCoalesce(true);
	   time = 0.;
	   timeStep = Math.PI/20.;
	   frozen = true;
	}

	protected void stopAnimation()
	{
	    if (frozen == false)
	    {
	       frozen = true;
	       menuBar.animateMode.setSelected(false);
	       if (timer.isRunning())
	       {
		  timer.stop();
	       }
	       view.setDisplacementScale(baseScale);
	       view.repaint();
	    }
	}
	
	protected void startAnimation()
	{
	    if (frozen == true)
	    {
	       frozen = false;
	       baseScale = view.getDisplacementScale();
	       menuBar.animateMode.setSelected(true);
	       if (!timer.isRunning())
	       {
		  timer.start();
	       }
	    }
	}
	
	public void actionPerformed(ActionEvent event)
	{
	    if (lock)
	    {
		return;
	    }
	       
	    // nächstes Frame der Animation anzeigen
	    if (event.getSource() == timer)
	    {
	       if (!model.getValidCalculation())
	       {
		  stopAnimation();
		  return;
	       }

	       time += timeStep;
	       if (time >= 2.*Math.PI)
	       {
		  time -= 2.*Math.PI;
	       }
	       view.setDisplacementScale(baseScale*Math.sin(time));
	       view.repaint();
	    }

	    String cmd=event.getActionCommand();

	    if (cmd != null)
	    {
	       status.setText("ready");

	       if (cmd.equals("new"))
	       {
		  stopAnimation();
		  resetApp();
	       }

	       if (cmd.equals("exit"))
	       {
		   stopAnimation();
		   exitApp();
	       }

	       if (cmd.equals("select window"))
	       {
		   mode.setText("select window: first point");
	       }

	       if (cmd.equals("zoom in"))
	       {
		   view.zoom(1/0.75);
		   view.repaint();
	       }

	       if (cmd.equals("zoom out"))
	       {
		   view.zoom(0.75);
		   view.repaint();
	       }

	       if (cmd.equals("pan"))
	       {
		   mode.setText("pan: first point");
	       }

	       if (cmd.equals("zoom window"))
	       {
		   mode.setText("zoom window: first point");
	       }

	       if (cmd.equals("show all"))
	       {
		   view.showAll();
		   view.repaint();
	       }

	       if (cmd.equals("sectional vals"))
	       {
		   DlgSectionalVals dlg = new DlgSectionalVals(mainFrame, JBeam.this);
	       }

	       if (cmd.equals("snap and grid"))
	       {
		   DlgGrid dlg = new DlgGrid(mainFrame, JBeam.this);
		   view.repaint();
	       }

	       if (cmd.equals("view"))
	       {
		  stopAnimation();

		  DlgView dlg = new DlgView(mainFrame, JBeam.this);
		  view.repaint();
	       }

	       // Menu Node
	       if (cmd.equals("free node"))
	       {
		   mode.setText("add free node");
	       }

	       if (cmd.equals("r constrained node"))
	       {
		   mode.setText("add r constrained node");
	       }

	       if (cmd.equals("rx constrained node"))
	       {
		   mode.setText("add rx constrained node");
	       }

	       if (cmd.equals("rz constrained node"))
	       {
		   mode.setText("add rz constrained node");
	       }

	       if (cmd.equals("rxz constrained node"))
	       {
		   mode.setText("add rxz constrained node");
	       }

	       if (cmd.equals("x constrained node"))
	       {
		   mode.setText("add x constrained node");
	       }

	       if (cmd.equals("z constrained node"))
	       {
		   mode.setText("add z constrained node");
	       }

	       if (cmd.equals("xz constrained node"))
	       {
		   mode.setText("add xz constrained node");
	       }

	       // Menu edit
	       if (cmd.equals("select"))
	       {
		   mode.setText("select");
		   model.noRef();
		   view.repaint();
	       }

	       if (cmd.equals("info"))
	       {
		   mode.setText("info");
		   model.noRef();
		   view.repaint();
	       }

	       if (cmd.equals("properties"))
	       {
		   Truss refTruss=null;
		   EBBeam refEBBeam=null;
		   EBSBeam refEBSBeam=null;
		   Node refNode=null;
		   boolean changeTruss=true;
		   boolean changeEBBeam=true;
		   boolean changeEBSBeam=true;
		   boolean changeNode=true;

		   changeProperties: while (model.hasSelectedObjects())
		   {
		       Object element=model.getSelectedObject();
		       if ((element instanceof Node) && changeNode)
		       {
			   if (refNode==null)
			   {
			       DlgNode dlgnode = new DlgNode(mainFrame, (Node)element);
			       if (!dlgnode.getOK())
			       {
				   //break changeProperties;
				   changeNode = false;
			       }
			       refNode=(Node)element;
			   } else
			   {
			       Node node=(Node)element;
			       node.setCX(refNode.getCX());
			       node.setCZ(refNode.getCZ());
			       node.setCR(refNode.getCR());

			       node.setFx(refNode.getFx());
			       node.setFz(refNode.getFz());
			       node.setM(refNode.getM());
			   }
		       }
		       if ((element instanceof Truss) && changeTruss)
		       {
			   if (refTruss==null)
			   {
			       DlgTruss dlgtruss = new DlgTruss(mainFrame, (Truss)element);
			       if (!dlgtruss.getOK())
			       {
				   //break changeProperties;
				   changeTruss = false;
			       }
			       refTruss=(Truss)element;
			   } else {
			       Truss truss=(Truss)element;
			       truss.setEA(refTruss.getEA());
			       truss.setM(refTruss.getM());
			   }
		       }
		       if (( (element instanceof EBBeam) && (!(element instanceof EBSBeam)) ) && changeEBBeam)
		       {
			   if (refEBBeam==null)
			   {
			       DlgEBBeam dlgebbeam = new DlgEBBeam(mainFrame, (EBBeam)element);
			       if (!dlgebbeam.getOK())
			       {
				   //break changeProperties;
				   changeEBBeam = false;
			       }
			       refEBBeam=(EBBeam)element;
			   } else {
			       EBBeam ebbeam=(EBBeam)element;
			       ebbeam.setEA(refEBBeam.getEA());
			       ebbeam.setEI(refEBBeam.getEI());
			       ebbeam.setM(refEBBeam.getM());
			       ebbeam.setVi(refEBBeam.getVi());
			       ebbeam.setVk(refEBBeam.getVk());
			       ebbeam.setNi(refEBBeam.getNi());
			       ebbeam.setNk(refEBBeam.getNk());
			   }
		       }
		       if ((element instanceof EBSBeam) && changeEBSBeam)
		       {
			   if (refEBSBeam==null)
			   {
			       DlgEBSBeam dlgebsbeam = new DlgEBSBeam(mainFrame, (EBSBeam)element);
			       if (!dlgebsbeam.getOK())
			       {
				   //break changeProperties;
				   changeEBSBeam = false;
			       }
			       refEBSBeam=(EBSBeam)element;
			   } else {
			       EBSBeam ebsbeam=(EBSBeam)element;
			       ebsbeam.setEA(refEBSBeam.getEA());
			       ebsbeam.setEI(refEBSBeam.getEI());
			       ebsbeam.setM(refEBSBeam.getM());
			       ebsbeam.setGAs(refEBSBeam.getGAs());
			       ebsbeam.setVi(refEBSBeam.getVi());
			       ebsbeam.setVk(refEBSBeam.getVk());
			       ebsbeam.setNi(refEBSBeam.getNi());
			       ebsbeam.setNk(refEBSBeam.getNk());
			   }
		       }
		   }
		   view.repaint();
	       }

	       if (cmd.equals("delete"))
	       {
		   int n=model.delete();
		   status.setText("Delete: "+n+" elements found");
		   if (n>0)
		   {
		       view.repaint();
		   }
	       }

	       if (cmd.equals("move"))
	       {
		   mode.setText("move: first point");
	       }

	       if (cmd.equals("truss") || cmd.equals("EB beam") || cmd.equals("EBS beam"))
	       {
		   mode.setText("element: first point");
		   addElementType = cmd;
	       }

	       // Menu File
	       if (cmd.equals("save"))
	       {
		   JFileChooser chooser = new JFileChooser();
                   if (fileChooseDir != null) {
                       chooser.setCurrentDirectory(fileChooseDir);
                   }
		   
		   // Add file filters for supported formats
		   ExampleFileFilter jbmFilter = 
		       new ExampleFileFilter("jbm", "JBeam Data Files");
		   ExampleFileFilter jsonFilter = 
		       new ExampleFileFilter("json", "JBeam JSON Files");
		   chooser.addChoosableFileFilter(jsonFilter);
		   chooser.addChoosableFileFilter(jbmFilter);
		   chooser.setFileFilter(jsonFilter); // Default to JSON
		   
		   chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		   chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int ret = chooser.showDialog(mainFrame, null);

		   if (ret == JFileChooser.APPROVE_OPTION)
		   {
		       status.setText("writing...");
		       FileOutputStream ostream = null;
		       try {
		           String filename = chooser.getSelectedFile().getAbsolutePath();
		           String extension = getFileExtension(filename);
		           
		           // Add appropriate extension if not present
		           if (extension.isEmpty()) {
		               String filterDesc = chooser.getFileFilter().getDescription();
		               if (filterDesc.contains("Data Files")) {
		                   filename += ".jbm";
		                   extension = "jbm";
		               } else {
		                   filename += ".json";
		                   extension = "json";
		               }
		           }
		           
		           ostream = new FileOutputStream(filename);
		           PersistenceManager.getInstance().save(model, ostream, extension);
		           status.setText("write OK");
                           fileChooseDir = chooser.getCurrentDirectory();
		       } catch (Exception e) {
			   status.setText("write error: "+e.getMessage());
			   JOptionPane.showMessageDialog(null, 
			       "File write error!\n"+
			       "Check the filename and permissions.\n"+
			       "Error: " + e.getMessage(), 
			       "Write Error", 
			       JOptionPane.ERROR_MESSAGE);
		       } finally {
			   try {
			       if (ostream != null) {
				   ostream.close();
			       }
			   } catch (IOException e) {
			   }
		       }
		   }
	       }

	       if (cmd.equals("open"))
	       {
		   JFileChooser chooser = new JFileChooser();
                   if (fileChooseDir != null) {
                       chooser.setCurrentDirectory(fileChooseDir);
                   }

		   // Add file filters for supported formats
		   ExampleFileFilter jbmFilter = 
		       new ExampleFileFilter("jbm", "JBeam Data Files");
		   ExampleFileFilter jsonFilter = 
		       new ExampleFileFilter("json", "JBeam JSON Files");
		   chooser.addChoosableFileFilter(jsonFilter);
		   chooser.addChoosableFileFilter(jbmFilter);
		   chooser.setFileFilter(jsonFilter); // Default to JSON
		   
		   chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		   chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int ret = chooser.showDialog(mainFrame, null);

		   if (ret == JFileChooser.APPROVE_OPTION)
		   {
		       status.setText("reading...");
		       FileInputStream istream = null;
		       try {
		           String filename = chooser.getSelectedFile().getAbsolutePath();
		           String extension = getFileExtension(filename);
		           
		           istream = new FileInputStream(filename);
		           model = PersistenceManager.getInstance().load(istream, extension);
		           view.reset();
		           view.setModel(model);
		           initApp();
		           
		           // Check for validation warnings from JSON loading
		           String statusMessage = "read OK";
		           if ("json".equals(extension) && JsonPersistence.lastValidationResult != null) {
		               if (!JsonPersistence.lastValidationResult.getWarnings().isEmpty()) {
		                   int warningCount = JsonPersistence.lastValidationResult.getWarnings().size();
		                   statusMessage = "read OK (" + warningCount + " warning" + (warningCount > 1 ? "s" : "") + ")";
		                   
		                   // Show warnings dialog
		                   StringBuilder warningMessage = new StringBuilder();
		                   warningMessage.append("Model loaded successfully with warnings:\n\n");
		                   for (String warning : JsonPersistence.lastValidationResult.getWarnings()) {
		                       warningMessage.append("• ").append(warning).append("\n");
		                   }
		                   warningMessage.append("\nYou can continue working with this model.");
		                   
		                   JOptionPane.showMessageDialog(mainFrame,
		                       warningMessage.toString(),
		                       "Model Warnings",
		                       JOptionPane.WARNING_MESSAGE);
		               }
		               // Clear the result after use
		               JsonPersistence.lastValidationResult = null;
		           }
		           status.setText(statusMessage);
                           fileChooseDir = chooser.getCurrentDirectory();
		       } catch (Exception e) {
			   status.setText("read error: "+e.getMessage());
			   JOptionPane.showMessageDialog(null, 
			       "File read error!\n"+
			       "Check if the file is present and readable.\n"+
			       "Error: " + e.getMessage(), 
			       "Read Error", 
			       JOptionPane.ERROR_MESSAGE);
		       } finally {
			   try {
			       if (istream != null) {
				   istream.close();
			       }
			   } catch (IOException e) {
			       // nothing
			   }
		       }
		   }
	       }

	       // Menu Results
	       if (cmd.equals("calculate"))
	       {
		   status.setText("calculating...");
		   try {
		       model.calculate();
		   } catch (RuntimeException e) {
		       JOptionPane.showMessageDialog(null, 
			   "Runtime Exception:\n"+
			   e.getMessage(), 
			   "Calculation Error", 
			   JOptionPane.ERROR_MESSAGE);
		   } catch (Exception e) {
		       JOptionPane.showMessageDialog(null, 
			   "Exception:\n"+
			   e.getMessage(), 
			   "Calculation Error", 
			   JOptionPane.ERROR_MESSAGE);
		   } catch (Error e) {
		       JOptionPane.showMessageDialog(null, 
			   "Error:\n"+
			   e.getMessage(), 
			   //"Out of Memory",
			   "Calculation Error", 
			   JOptionPane.ERROR_MESSAGE);
		   }
		   status.setText("calculation complete");
		   view.repaint();
	       }

	       if (cmd.equals("calculate modal"))
	       {
		   status.setText("calculating modal...");
		   try {
		       model.calculateModal();
		   } catch (RuntimeException e) {
		       JOptionPane.showMessageDialog(null, 
			   "Runtime Exception:\n"+
			   e.getMessage(), 
			   "Calculation Error", 
			   JOptionPane.ERROR_MESSAGE);
		   } catch (Exception e) {
		       JOptionPane.showMessageDialog(null, 
			   "Exception:\n"+
			   e.getMessage(), 
			   "Calculation Error", 
			   JOptionPane.ERROR_MESSAGE);
		   } catch (Error e) {
		       JOptionPane.showMessageDialog(null, 
			   "Error:\n"+
			   e.getMessage(), 
			   "Calculation Error", 
			   JOptionPane.ERROR_MESSAGE);
		   }
		   status.setText("Mode = "+model.getMode()+", f = "+model.getFreq());
		   view.repaint();
	       }

	       if (cmd.equals("show next mode"))
	       {
		   model.nextMode();
		   status.setText("Mode = "+model.getMode()+", f = "+model.getFreq());
		   view.repaint();
	       }

	       if (cmd.equals("show prev mode"))
	       {
		   model.prevMode();
		   status.setText("Mode = "+model.getMode()+", f = "+model.getFreq());
		   view.repaint();
	       }

	       if (cmd.equals("animate mode"))
	       {
		  if (((AbstractButton)event.getSource()).isSelected())
		  {
		     startAnimation();
		     status.setText("Animation: Mode = "+model.getMode());
		     view.repaint();
		  } else {
		     stopAnimation();
		     view.setDisplacementScale(baseScale);
		     view.repaint();
		  }
	       }


	       if (cmd.equals("show displacement"))
	       {
		   if (((AbstractButton)event.getSource()).isSelected())
		   {
		       view.setShowDisplacement(true);
		   } else {
		       view.setShowDisplacement(false);
		   }
		   view.repaint();
	       }

	       if (cmd.equals("show moment"))
	       {
		   if (((AbstractButton)event.getSource()).isSelected())
		   {
		       view.setShowMoment(true);
		   } else {
		       view.setShowMoment(false);
		   }
		   view.repaint();
	       }

	       if (cmd.equals("show normal force"))
	       {
		   if (((AbstractButton)event.getSource()).isSelected())
		   {
		       view.setShowNormalForce(true);
		   } else {
		       view.setShowNormalForce(false);
		   }
		   view.repaint();
	       }

	       if (cmd.equals("show shear force"))
	       {
		   if (((AbstractButton)event.getSource()).isSelected())
		   {
		       view.setShowShearForce(true);
		   } else {
		       view.setShowShearForce(false);
		   }
		   view.repaint();
	       }

	       if (cmd.equals("write result"))
	       {
		   JFileChooser chooser = new JFileChooser();
                   if (fileChooseDir != null) {
                       chooser.setCurrentDirectory(fileChooseDir);
                   }
                   
		   ExampleFileFilter jbmFilter = 
		       new ExampleFileFilter("txt", "ASCII text file");
		   chooser.setFileFilter(jbmFilter);
		   chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		   chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int ret = chooser.showDialog(mainFrame, null);

		   if (ret == JFileChooser.APPROVE_OPTION)

		   {
		       status.setText("writing...");
		       FileWriter ostream=null;
		       try {
			   ostream=new FileWriter(
				   chooser.getSelectedFile().getAbsolutePath()
			       );
			   PrintWriter p=new PrintWriter(ostream);
			   TextView textView = new TextView(model, JBeam.this);
			   textView.write(p);
			   status.setText("write OK");
			   p.close();
                           fileChooseDir = chooser.getCurrentDirectory();
		       } catch (IOException e) {
			   status.setText("write error: "+e.getMessage());
			   JOptionPane.showMessageDialog(null, 
			       "File write error!\n"+
			       "Check the filename and permissions.", 
			       "Write Error", 
			       JOptionPane.ERROR_MESSAGE);
		       } finally {
			   try {
			       if (ostream != null)
			       {
				   ostream.close();
			       }
			   } catch (IOException e) {
			   }
		       }
		   }
	       }

	       if (cmd.equals("write result html"))
	       {
		   JFileChooser chooser = new JFileChooser();
                   if (fileChooseDir != null) {
                       chooser.setCurrentDirectory(fileChooseDir);
                   }
                   
		   ExampleFileFilter jbmFilter = 
		       new ExampleFileFilter("html", "HTML document");
		   chooser.setFileFilter(jbmFilter);
		   chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		   chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		   int ret = chooser.showDialog(mainFrame, null);

		   if (ret == JFileChooser.APPROVE_OPTION)

		   {
		       status.setText("writing...");
		       FileWriter ostream=null;
		       try {
                           File outFile = chooser.getSelectedFile().getAbsoluteFile();
			   ostream=new FileWriter(outFile);
			   PrintWriter p=new PrintWriter(ostream);
			   HtmlView textView = new HtmlView(model, JBeam.this);
			   textView.write(p);
			   status.setText("write OK");
			   p.close();
                           fileChooseDir = chooser.getCurrentDirectory();
                           try {
                            Desktop.getDesktop().browse(outFile.toURI());
                           } catch (Exception e) { }
		       } catch (IOException e) {
			   status.setText("write error: "+e.getMessage());
			   JOptionPane.showMessageDialog(null, 
			       "File write error!\n"+
			       "Check the filename and permissions.", 
			       "Write Error", 
			       JOptionPane.ERROR_MESSAGE);
		       } finally {
			   try {
			       if (ostream != null)
			       {
				   ostream.close();
			       }
			   } catch (IOException e) {
			   }
		       }
		   }
	       }

	       // Menu ?
	       if (cmd.equals("about"))
	       {
		   JOptionPane.showMessageDialog(null, 
		       "JBeam "+version+"\n"+
		       verdate+"\n"+
		       "by Schwebke Software Development\n\n"+
		       "This is copyrighted software -\n  see 'COPYING'"+
		       " for details.",
		       "about JBeam", 
		       JOptionPane.INFORMATION_MESSAGE);
	       }
               
               updateCursorShape();
	    }
	}
    }

    /**
      * Klasse zum Verarbeiten von Mausereignissen
      */
    class JBeamViewMouseListener extends MouseAdapter
    {
	public void mouseClicked(MouseEvent event)
	{
	    if (lock)
	    {
		return;
	    }

	    // left button
	    if ( (event.getModifiers() & event.BUTTON1_MASK) != 0 )
	    {
		if (!posX.getText().equals("X: -"))
		{
		    double x=snap(view.worldX(event.getX()));
		    double z=snap(view.worldZ(event.getY()));


		    if (mode.getText().equals("add free node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add r constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, false, false, true));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add rx constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, true, false, true));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add rz constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, false, true, true));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add rxz constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, true, true, true));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add x constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, true, false, false));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add z constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, false, true, false));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("add xz constrained node"))
		    {
			if (isFree(x, z))
			{
			    model.addNode(new Node(x, z, true, true, false));
			    view.repaint();
			    status.setText("OK");
			}
			else
			{
			    status.setText("Error: location already occupied");
			}
		    }

		    if (mode.getText().equals("select"))
		    {
			// we don't snap when selecting
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());
			int n=model.addSelection(x, z, view.worldScale(checkR), view);
			status.setText("Selection: "+n+" added");
			if (n!=0)
			{
			    view.repaint();
			}
		    }

		    if (mode.getText().equals("info"))
		    {
			// we don't snap when selecting
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());
			model.clearSelection();
			int n=model.addSelection(x, z, view.worldScale(checkR), view);
			if (n==0)
			{
			   status.setText("Info: nothing found");
			} else if (n>1) {
			   status.setText("Info: more than one object found");
			} else {
			   Object obj = model.getSelectionIterator().iterator().next();
			   DlgInfo dlgInfo = new DlgInfo(JBeam.this, mainFrame, model, obj);
			}
			model.clearSelection();
		    }

		    if (mode.getText().equals("select window: second point"))
		    {
			// we don't snap when zooming
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());

			model.addSelectionWindow(x, z);
			view.repaint();
			mode.setText("select");
		    }

		    if (mode.getText().equals("select window: first point"))
		    {
			// we don't snap when zooming
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());

			model.setRef(x, z, false, true);
			view.repaint();
			mode.setText("select window: second point");
		    }


		    if (mode.getText().equals("move: second point"))
		    {
			int n=model.move(x, z);
			view.repaint();
			mode.setText("select");
			status.setText("Move: "+n+" Elements found");
		    }

		    if (mode.getText().equals("move: first point"))
		    {
			model.setRef(x, z, true, false);
			view.repaint();
			mode.setText("move: second point");
		    }

		    if (mode.getText().equals("pan: second point"))
		    {
			// we don't snap when panning
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());

			view.pan(x-model.getRefX(), z-model.getRefZ());
			model.noRef();
			view.repaint();
			mode.setText("select");
		    }

		    if (mode.getText().equals("pan: first point"))
		    {
			// we don't snap when panning
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());

			model.setRef(x, z, false, false);
			view.repaint();
			mode.setText("pan: second point");
		    }

		    if (mode.getText().equals("zoom window: second point"))
		    {
			// we don't snap when zooming
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());

			view.zoomWindow(x, z, model.getRefX(), model.getRefZ());
			model.noRef();
			view.repaint();
			mode.setText("select");
		    }

		    if (mode.getText().equals("zoom window: first point"))
		    {
			// we don't snap when zooming
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());

			model.setRef(x, z, false, true);
			view.repaint();
			mode.setText("zoom window: second point");
		    }

		    if (mode.getText().equals("element: second point"))
		    {
			// we don't snap when selecting
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());
			if (model.selectNode(x, z, 1, view.worldScale(checkR)))
			{
			    Node n1=model.getNode(0);
			    Node n2=model.getNode(1);
			    if (n1 != n2)
			    {
				mode.setText("element: first point");
				status.setText("OK");

				Beam beam = null;
				if (addElementType.equals("truss"))
				{
				    beam = new Truss(n1, n2, EA, m);
				}
				if (addElementType.equals("EB beam"))
				{
				    beam = new EBBeam(n1, n2, EI, EA, m);
				}
				if (addElementType.equals("EBS beam"))
				{
				    beam = new EBSBeam(n1, n2, EI, EA, GAs, m);
				}

				if ( beam != null )
				{
				    model.addBeam(beam);
				}

			    } else {
				mode.setText("element: first point");
				status.setText("Error: Node 1 and 2 are equal");
			    }
			}
			else
			{
			    status.setText("Error: No Node found");
			}
			view.repaint();
		    } else if (mode.getText().equals("element: first point"))
		    {
			// we don't snap when selecting
			x=view.worldX(event.getX());
			z=view.worldZ(event.getY());
			if (model.selectNode(x, z, 0, view.worldScale(checkR)))
			{
			    mode.setText("element: second point");
			    status.setText("Node selected");
			}
			else
			{
			    status.setText("Error: No Node found");
			}
			view.repaint();
		    }
		}
	    }

	    // right/middle button
	    if ( (event.getModifiers() & (event.BUTTON2_MASK|event.BUTTON3_MASK))
			 != 0 )
	    {
		// select one and edit
		// we don't snap when selecting
		double x=view.worldX(event.getX());
		double z=view.worldZ(event.getY());
		model.clearSelection();
		model.noRef();
		mode.setText("select");
                updateCursorShape();
		int n=model.addSelection(x, z, view.worldScale(checkR), view);
		status.setText("Selection: "+n+" added");
		if (n==1)
		{
		    Object element=model.getSelectedObject();
		    if (element instanceof Node)
		    {
			DlgNode dlgnode = new DlgNode(mainFrame, (Node)element);
		    }
		    if (element instanceof Truss)
		    {
			DlgTruss dlgtruss = new DlgTruss(mainFrame, (Truss)element);
		    }
		    if ( (element instanceof EBBeam) && (!(element instanceof EBSBeam)) )
		    {
			DlgEBBeam dlgebbeam = new DlgEBBeam(mainFrame, (EBBeam)element);
		    }
		    if (element instanceof EBSBeam)
		    {
			DlgEBSBeam dlgebsbeam = new DlgEBSBeam(mainFrame, (EBSBeam)element);
		    }
		}
		view.repaint();
	    }
	}

	public void mouseExited(MouseEvent event)
	{
	    posX.setText("X: -");
	    posY.setText("Z: -");
	}
    }

    /**
      * Klasse zum Verarbeiten der Mausposition
      * (Statusanzeige der Koordinaten und pan mit mittlerer Maustaste).
      */
    class JBeamViewMouseMotionListener extends MouseMotionAdapter
    {
        @Override
	public void mouseMoved(MouseEvent event)
	{
            dragging = false;
            
	    if (lock)
	    {
		return;
	    }

	    double x=view.worldX(event.getX());
	    double z=view.worldZ(event.getY());
	    posX.setText("X: "+numberFormat.format(snap(x)));
	    posY.setText("Z: "+numberFormat.format(snap(z)));
	}
        
        @Override
        public void mouseDragged(MouseEvent event)
        {
            int x = event.getX();
            int y = event.getY();
            
            if (dragging) {
                int dX = x - dragX;
                int dY = y - dragY;
                if ((event.getModifiersEx() & (MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) != 0) {
                    view.pan(dX, dY);
                    view.repaint();
                }
            }
            
            dragging = true;
            dragX = x;
            dragY = y;
        }
        
        private boolean dragging;
        private int dragX;
        private int dragY;
    }
    
    /**
     * Klasse zum Verarbeiten der Mausrad-Events
     * (zoom in / zoom out)
     */
    class JBeamViewMouseWheelListener implements MouseWheelListener {
        private static final double mouseZoomFactor = 1.1;
        private static final double mouseZoomFactorI = 1.0/mouseZoomFactor;
        
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int r = e.getWheelRotation();
            while (r > 0) {
                view.zoom(mouseZoomFactor);
                --r;
            }
            while (r < 0) {
                view.zoom(mouseZoomFactorI);
                ++r;
            }
            view.repaint();
        }
    }
    
    /** Cursor-Form abhängig vom Modus setzen */
    private void updateCursorShape() {
       // Cursor-Form (abhängig vom Mode)
       String modeText = mode.getText();
       if (modeText.equals("info")) {
           view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
       } else if (modeText.startsWith("add") || modeText.startsWith("element")) {
           view.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
       } else {
           view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
       }
    }

    /**
      * Löschen des Modells. Zurücksetzen des Views und des Controllers.
      * Dient zum Beginn eines neuen Systems.
      */
    public void resetApp()
    {
       model.clearModel();
       initApp();
       view.reset();
       view.repaint();
    }

    /**
      * Aufräumarbeiten beim Programmende.
      */
    private void exitApp()
    {
	mainFrame.setVisible(false);
	mainFrame.dispose();
	System.out.println("JBeam shutdown");
	launcher.exit();
    }

    /**
     * Sperren der Benutzeroberfläche (für remote controlling)
     */
    public boolean lockUI()
    {
       synchronized (this) {
	  if (lock == false)
	  {
	     // lock möglich
	     status.setText("locked");
	     lock = true;
	     return true;
	  } else {
	     // ist bereits gelockt
	     return false;
	  }
       }
    }

    /**
     * Freigeben der Benutzeroberfläche (für remote controlling)
     */
    public void unlockUI()
    {
       synchronized (this) {
	  status.setText("ready");
	  lock = false;
	  view.repaint();
       }
    }

    /**
     * snapt den übergenen Wert in Welt-Koordinaten auf den nächsten gültigen Snapgrid-Wert
     */
    public double snap(double v)
    {
	if (grid>1e-10)
	{
	    return Math.round(v/grid)*grid;
	}
	return v;
    }

    /**
     * Testet, ob an der übergenen Stelle Objekte vorhanden sind.
     * Wenn nein, dann wird true zurückgeliefert */
    boolean isFree(double x, double z)
    {
	int n=model.addSelection(x, z, view.worldScale(checkR), view);
	model.clearSelection();
	if (n>0)
	    return false;
	return true;
    }

    /**
     * liefert Formatierer für zur Ausgabe bestimmter Gleitkommazahlen
     */
    public NumberFormat getNumberFormat()
    {
	return numberFormat;
    }

   /// Formatiert eine Zahl auf einen String mit 4 geltenden Ziffern
   public String format(double value)
   {
      int digits = 4 - (int)Math.ceil(Math.log(Math.max(Math.abs(value),1e-8))/Math.log(10.));
      return format(value, digits);
   }

    /// Formatiert eine Zahl in einen String mit der gewünschten Anzahl an Nachkommastellen
   public String format(double value, int digits)
   {
      int currentDigitsMax = numberFormat.getMaximumFractionDigits();
      int currentDigitsMin = numberFormat.getMinimumFractionDigits();
      if (digits > 0)
      {
	 numberFormat.setMaximumFractionDigits(digits);
	 numberFormat.setMinimumFractionDigits(digits);
      } else {
	 numberFormat.setMaximumFractionDigits(0);
	 numberFormat.setMinimumFractionDigits(0);
      }
      String vStr = numberFormat.format(com.schwebke.math.Round.digits(value, digits));
      numberFormat.setMaximumFractionDigits(currentDigitsMax);
      numberFormat.setMinimumFractionDigits(currentDigitsMin);

      return vStr;
   }

    /**
     * liefert das Model dieses Controllers
     */
    public IModel getModel()
    {
	return (IModel)model;
    }

    /**
     * zeichnet View neu
     */
    public void modelHasChanged()
    {
	view.repaint();
    }

    /**
      * Liefert Standardwert EI für neue Elemente.
      */
    public double getDefEI()
    {
	return EI;
    }

    /**
      * Liefert Standardwert GAs für neue Elemente.
      */
    public double getDefGAs()
    {
	return GAs;
    }

    /**
      * Liefert Standardwert EA für neue Elemente.
      */
    public double getDefEA()
    {
	return EA;
    }

    /**
      * Liefert Standardwert m für neue Elemente.
      */
    public double getDefM()
    {
	return m;
    }

    /**
      * Open InputStream on resource. Try to access via file-IO and URL/http.
      */
   public static InputStream getResourceAsStream(String resourceName)
   {
      try {
	 InputStream resourceStream = new FileInputStream("resourcen/"+resourceName);
	 return resourceStream;
      } catch (FileNotFoundException fe) {
      } catch (SecurityException se) {
      }

      try {
	 URL url = new URL(
			com.schwebke.jbeam.applet.JBeamApplet.baseUrl +
			"resourcen/"+resourceName);
	 return url.openStream();
      } catch (MalformedURLException me) {
      } catch (IOException ie) {
      }

      throw new Error("JBeam::getResourceAsStream("+resourceName+"): cannot access");
   }
   
   /**
    * Helper method to extract file extension from filename.
    * 
    * @param filename the filename
    * @return the file extension (without dot), or empty string if no extension
    */
   private String getFileExtension(String filename) {
       if (filename == null || filename.isEmpty()) {
           return "";
       }
       
       int lastDotIndex = filename.lastIndexOf('.');
       if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
           return "";
       }
       
       return filename.substring(lastDotIndex + 1).toLowerCase();
   }

}

