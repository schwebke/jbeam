package com.schwebke.jbeam.model;

import com.schwebke.jbeam.view.*;

import java.util.*;


/**
  * SelectableModel erweitertet das Model um
  * das Selektieren einer Menge von Objekten
  * und die Manipulation dieser Menge.
  * SelectableModel wird vom graphisch-interaktiven
  * Frontend als Model verwendet.
  */
public class SelectableModel extends Model
{
    static final long serialVersionUID = 4833467666795694803L;

    /// Selektionsliste
    LinkedList selectionList;

    /// Referenzpunkt X-Koordinate
    double refX;
    /// Referenzpunkt Z-Koordinate
    double refZ;
    /// Referenzpunkt aktiv
    boolean ref;
    /// Gummiband vom Referenzpunkt rastet auf dem Gitter ein
    boolean refSnap;
    /// Statt Gummiband wird ein Fangrechteck angezeigt
    boolean refRect;

    /// Knotenselektionsfeld für das Hinzufügen von Beamelementen
    Node node[];

    /**
      * Erzeugt ein neues, leeres Model.
      */
    public SelectableModel()
    {
      super();
      refX = 0.;
      refZ = 0.;
      ref = false;
      refSnap = false;
      refRect = false;
      node = new Node[2];
      selectionList = new LinkedList();
    }

    /**
      * Setzt die Selektionsliste zurück.
      */
    public void clearSelection()
    {
	selectionList.clear();
    }

    /**
      * Setzt das gesamte Model zurück.
      */
    @Override
    public void clearModel()
    {
       super.clearModel();
       selectionList.clear();
       refX=0.;
       refZ=0.;
       ref=false;
       node=new Node[2];
    }

    /// Den angegebenen Knoten aus dem Knotenselektionsfeld entnehmen.
    public Node getNode(int num)
    {
	Node n=node[num];
	node[num]=null;
	validCalculation=false;
	ref = false;
	return n;
    }

    /// Iterator auf die Selektionsliste.
    public Iterable getSelectionIterator()
    {
	return selectionList;
    }

    /// True, wenn Elemente in der Selektionsliste vorhanden sind.
    public boolean hasSelectedObjects()
    {
	return !selectionList.isEmpty();
    }

    /**
      * Liefert das erste Element aus der Selektionsliste
      * und setzt die Liste zurück.
      */
    public Object getSelectedObject()
    {
	validCalculation=false;
        return selectionList.pop();
    }

    /**
      * Löschen selektierter Model-Daten.
      */
    public int delete()
    {
	int delCount=0;
	for (Object obj : selectionList) {

	    if (obj instanceof Node)
	    {
	       // Alle Beams löschen, die an diesen Node anschließen.
	       // Zu löschende Beams werden in einer Liste zwischengespeichert,
	       // um den Iterator nicht durcheinander zu bringen.
		List<Beam> deleteList = new LinkedList<Beam>();
		Node node=(Node)obj;
		for (Beam beam : beamList) {
		    if ( (beam.n1 == node) || (beam.n2 == node) )
		    {
			deleteList.add(beam);
		    }
		}

		for (Beam beam : deleteList) {
		     if (beamList.remove(beam))
		     {
			 delCount++;
		     }
		}


		// delete Node
		if (nodeList.remove(node))
		{
		    delCount++;
		}
	    } else if (obj instanceof Beam) {
                Beam beam = (Beam)obj;
		if (beamList.remove(beam))
		{
		    delCount++;
		}
	    }
	}
	clearSelection();
	validCalculation=false;
	return delCount;
    }

    /**
      * Setzt den Referenzpunkt im Model.
      * Dieser dient dem Model zum Verschieben bzw. dem View,
      * um Gummibandlinien und Zoom-/Selektionsfenster zu
      * erzeugen.
      */
    public void setRef(double x, double z, boolean snap, boolean rect)
    {
	refX=x;
	refZ=z;
	ref=true;
	refSnap = snap;
	refRect = rect;
    }

    public boolean getRef()
    {
	return ref;
    }

    public double getRefX()
    {
	return refX;
    }

    public double getRefZ()
    {
	return refZ;
    }

    public boolean getRefSnap()
    {
	return refSnap;
    }

    public boolean getRefRect()
    {
	return refRect;
    }

    public void noRef()
    {
	ref = false;
    }

    /**
      * Verschieben der Elemente im Selektionssatz
      */
    public int move(double x, double z)
    {
	ref=false;

	int moveCount=0;

	// Bei selektierten Beams die angrenzenden
	// Knoten mitnehmen.
        ArrayList extendSelection = new ArrayList();
	for (Object obj : selectionList) {
	    if (obj instanceof Beam)
	    {
                Node n1 = ((Beam)obj).n1;
                Node n2 = ((Beam)obj).n2;
		if ( (!selectionList.contains(n1)) && (!extendSelection.contains(n1)) )
		{
		    extendSelection.add(n1);
		}
		if ( (!selectionList.contains(n2)) && (!extendSelection.contains(n2)) )
		{
		    extendSelection.add(n2);
		}
	    }
	}
        
        selectionList.addAll(extendSelection);

	for (Object obj : selectionList) {
	    if (obj instanceof Node)
	    {
		((Node)obj).x+=x-refX;
		((Node)obj).z+=z-refZ;
		moveCount++;
	    }
	}
	clearSelection();
	validCalculation=false;
	calculateBeamVectors();
	return moveCount;    
    }

    /**
      * Spezielle Selektierfunktion, die nur auf Knoten anspricht.
      * (Zur Bestimmung von Knoten für neue Elemente)
      */
    public boolean selectNode(double x, double z, int num, double r)
    {
	for (Node node : nodeList) {
	    if ( node.getDistance(x, z) < r )
	    {
		this.node[num]=node;
		setRef(node.getX(), node.getZ(), false, false);
		return true;
	    }
	}
	return false;
    }

    /**
      * Fügt der Selektionsliste Elemente hinzu, die durch
      * den angegebenen Punkt angesprochen werden, bzw.
      * entfernt bereits selektierte Elemente aus der Liste 
      * (Togglefunktion).
      */
    public int addSelection(double x, double z, double r, View view)
    {
	int selCount=0;

	for (Node node : nodeList) {
	    if ( NodeRenderer.selected(x, z, r, node, view) )
	    {
		if (!selectionList.remove(node))
		{
		    selectionList.add(node);
		    selCount++;
		} else {
		    selCount--;
		}
	    }
	}

	// Wenn mindestens ein Knoten gefunden wurde, werden
	// keine Beamelemente selektiert. Dies erleichtert
	// das gezielte Auswählen von Knoten.
	if (selCount==0)
	{
	    for (Beam beam : beamList) {
		if ( BeamRenderer.selected(x, z, r, beam, view) )
		{
		    if (!selectionList.remove(beam))
		    {
			selectionList.add(beam);
			selCount++;
		    } else {
			selCount--;
		    }
		}
	    }
	}

	return selCount;
    }

    /**
      * Fügt der Selektionsliste Elemente hinzu, die durch
      * das angegebene Fangfenster angesprochen werden, bzw.
      * entfernt bereits selektierte Elemente aus der Liste 
      * (Togglefunktion).
      */
    public int addSelectionWindow(double x, double z)
    {
	int selCount=0;
	double x1 = Math.min(x, refX),
	       x2 = Math.max(x, refX),
	       z1 = Math.min(z, refZ),
	       z2 = Math.max(z, refZ);

	for (Node node : nodeList) {
	    if ( (node.getX()>x1) &&
		  (node.getX()<x2) &&
		  (node.getZ()>z1) &&
		  (node.getZ()<z2) )
	    {
		if (!selectionList.remove(node))
		{
		    selectionList.add(node);
		    selCount++;
		} else {
		    selCount--;
		}
	    }
	}

	 for (Beam beam : beamList) {
	     if ( (beam.getN1().getX()>x1) &&
		  (beam.getN1().getX()<x2) &&
		  (beam.getN1().getZ()>z1) &&
		  (beam.getN1().getZ()<z2) &&
		  (beam.getN2().getX()>x1) &&
		  (beam.getN2().getX()<x2) &&
		  (beam.getN2().getZ()>z1) &&
		  (beam.getN2().getZ()<z2) )
	     {
		    if (!selectionList.remove(beam))
		    {
			selectionList.add(beam);
			selCount++;
		    } else {
			selCount--;
		    }
	     }
	 }

	noRef();
	return selCount;
    }


}
