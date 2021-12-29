package com.schwebke.jbeam.view;

import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.*;
import com.schwebke.math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.*;
import javax.swing.*;

/** View. Graphische Darstellung im JBeam-Hauptfenster */
public class View extends JComponent {

    /** Bezugs-Größe der graphischen Elemente */
    double baseSize;
    /** Cursor-Position sX (Screen-Koordinaten) */
    double sX;
    /** Cursor-Position sY (Screen-Koordinaten) */
    double sY;
    /** aktuelle Zeichenfarbe */
    Color color;
    /** Aktueller Grafikkontext. Wird zwischengespeichert, um mehrfache Übergabe
     * von <code>paint()</code> an tiefere Funktionen zu vermeiden. */
    Graphics2D g;
    /** Skalierung fX der Worldkoordinaten */
    double fX;
    /** Skalierung fZ der Worldkoordinaten */
    double fZ;
    /** Rasterabstand in World-Koordinaten */
    double grid;
    /** Position des Ursprungs der World-Koordinaten in Screen-Koordinaten */
    int oX;
    /** Position des Ursprungs der World-Koordinaten in Screen-Koordinaten */
    int oY;
    // Art der Darstellung
    boolean showDisplacement;
    double displacementScale;
    boolean showMoment;
    double momentScale;
    boolean showShearForce;
    double shearForceScale;
    boolean showNormalForce;
    double normalForceScale;
    // Model
    SelectableModel model;
    // Controller
    JBeam controller;
    
    
    // Graphics2D setup constants
    private static final Stroke normalLine = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke thickLine = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke dashedLine = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1.0f, new float[]{10.0f, 10.0f}, 0.0f);
    
    private static final RenderingHints rhStandard;
    private static final RenderingHints rhPure;

    static {
        rhStandard = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rhStandard.add(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
        
        rhPure = (RenderingHints)rhStandard.clone();
        
        rhPure.add(new RenderingHints(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE));
        
        rhStandard.add(new RenderingHints(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE));
    }

    public View(SelectableModel model, JBeam controller) {
        this.model = model;
        this.controller = controller;

        reset();

        setBackground(Color.white);
        setBorder(BorderFactory.createLoweredBevelBorder());
    }

    public void reset() {
        sX = 0;
        sY = 0;
        fX = 20.;
        fZ = 20.;
        oX = 300;
        oY = 200;
        color = Color.black;
        grid = 2.;
        baseSize = 4.;
        showDisplacement = false;
        displacementScale = 100.;
        showMoment = false;
        momentScale = 1.;
        showShearForce = false;
        shearForceScale = 1.;
        showNormalForce = false;
        normalForceScale = 1.;
        rubberBand = null;
    }

    /**
     * Gummibandlinie und Zoom-/Selektionsfenster in Echtzeit zeichnen
     */
    class RubberBand extends MouseMotionAdapter {

        int baseX;
        int baseY;
        int oldX;
        int oldY;
        JComponent component;
        boolean snap;
        boolean rect;

        RubberBand(int baseX, int baseY, JComponent component,
                boolean snap, boolean rect) {
            this.baseX = oldX = baseX;
            this.baseY = oldY = baseY;
            this.component = component;
            this.snap = snap;
            this.rect = rect;
        }

        @Override
        public void mouseMoved(MouseEvent event) {
            Graphics2D g = (Graphics2D)component.getGraphics();
            g.setRenderingHints(rhStandard);
            g.setColor(Color.red);
            g.setXORMode(component.getBackground());
            if (!rect) {
                g.drawLine(baseX, baseY, oldX, oldY);
            } else {
                g.drawRect(Math.min(baseX, oldX),
                        Math.min(baseY, oldY),
                        Math.abs(oldX - baseX),
                        Math.abs(oldY - baseY));
            }
            if (snap) {
                oldX = screenX(controller.snap(worldX(event.getX())));
                oldY = screenY(controller.snap(worldZ(event.getY())));
            } else {
                oldX = event.getX();
                oldY = event.getY();
            }

            if (!rect) {
                g.drawLine(baseX, baseY, oldX, oldY);
            } else {
                g.drawRect(Math.min(baseX, oldX),
                        Math.min(baseY, oldY),
                        Math.abs(oldX - baseX),
                        Math.abs(oldY - baseY));
            }
        }
    }
    RubberBand rubberBand;

    void addRubberBand(int baseX, int baseY, boolean snap, boolean rect) {
        if (rubberBand != null) {
            removeMouseMotionListener(rubberBand);
        }
        rubberBand = new RubberBand(baseX, baseY, this, snap, rect);
        addMouseMotionListener(rubberBand);
    }

    void removeRubberBand() {
        removeMouseMotionListener(rubberBand);
        rubberBand = null;
        repaint();
    }

    public void setModel(SelectableModel model) {
        this.model = model;
        this.repaint();
    }

    @Override
    public void paint(Graphics gAWT) {
        g = (Graphics2D) gAWT;
        g.setRenderingHints(rhStandard);

        g.setColor(getBackground());
        g.setStroke(normalLine);
        g.fillRect(0, 0, getSize().width, getSize().height);

        // Grid zeichnen
        // Weltkoordinaten der zu zeichnenden Region ermitteln und
        //   auf Raster snappen
        double wxStart = snap(worldX(g.getClipBounds().x));
        double wzStart = snap(worldZ(g.getClipBounds().y));
        double wxStop = snap(worldX(g.getClipBounds().x + g.getClipBounds().width));
        double wzStop = snap(worldZ(g.getClipBounds().y + g.getClipBounds().height));

        double wX;
        double wZ;

        g.setColor(Color.black);

        if (Math.min(grid*fX, grid*fZ) > 5.0) {
            for (wX = wxStart; wX <= wxStop; wX += grid) {
                int x = screenX(wX);
                for (wZ = wzStart; wZ <= wzStop; wZ += grid) {
                    int y = screenY(wZ);
                    g.drawLine(x, y, x, y);
                }
            }
        }

        colorStd();
        for (Beam beam : model.getBeamIterator()) {

            if (beam instanceof Truss) {
                TrussRenderer.draw(this, (Truss) beam);
            }

            if (beam instanceof EBBeam) {
                EBBeamRenderer.draw(this, (EBBeam) beam);
            }
            if (beam instanceof EBSBeam) {
                EBSBeamRenderer.draw(this, (EBSBeam) beam);
            }
        }
        for (Node node : model.getNodeIterator()) {
            NodeRenderer.draw(this, node);
        }

        colorSel();
        for (Object obj : model.getSelectionIterator()) {

            if (obj instanceof Truss) {
                TrussRenderer.drawElement(this, (Truss) obj);
            }

            if (obj instanceof EBBeam) {
                EBBeamRenderer.drawElement(this, (EBBeam) obj);
            }

            if (obj instanceof EBSBeam) {
                EBSBeamRenderer.drawElement(this, (EBSBeam) obj);
            }

            if (obj instanceof Node) {
                NodeRenderer.drawElement(this, (Node) obj);
            }
        }

        if (model.getRef()) {
            if (rubberBand == null) {
                addRubberBand(
                        screenX(model.getRefX()),
                        screenY(model.getRefZ()),
                        model.getRefSnap(),
                        model.getRefRect());
            }
        } else {
            if (rubberBand != null) {
                removeRubberBand();
            }
        }

        // Border zeichnen lassen
        super.paint(g);
    }

    /** Abbildung World -> Screen (gerundet) */
    public int screenX(double wX) {
        return (int) (wX * fX + oX);
    }

    /** Abbildung World -> Screen (gerundet) */
    public int screenY(double wZ) {
        return (int) (wZ * fZ + oY);
    }
    
    /** Abbildung World -> Screen (double) */
    public double screenXd(double wX) {
        return wX * fX + oX;
    }

    /** Abbildung World -> Screen (double) */
    public double screenYd(double wZ) {
        return wZ * fZ + oY;
    }

    /** Abbildung Screen -> World */
    public double worldX(double sX) {
        return (sX - oX) / fX;
    }

    /** Abbildung Screen -> World */
    public double worldZ(double sY) {
        return (sY - oY) / fZ;
    }

    /** Abbildung Screen -> World */
    public double worldScale(double s) {
        return s / ((fX + fZ) / 2);
    }

    /** Snappen einer Worldkoordinate */
    double snap(double v) {
        return Math.round(v / grid) * grid;
    }

    /** Abbildung BasisE -> World */
    public double baseScale(double b) {
        return worldScale(b * baseSize);
    }

    // Zeichenfunktionen in World-Koordinaten
    public void setRHstandard() {
        g.setRenderingHints(rhStandard);
    }
    
    /* sub-pixel Rendering -- sieht bei Polylinien (Verschiebung, Momenten, ...)
     * besser aus). Für Standard-Geometrie (Auflager, Elemente, Punkte) ist das
     * nicht so gut, da es unscharf wirkt.
     */
    public void setRHpure() {
        g.setRenderingHints(rhPure);
    }
    
    public void move(double wX, double wZ) {
        sX = screenXd(wX);
        sY = screenYd(wZ);
    }

    public void drawLine(double wX, double wZ) {
        g.setColor(color);
        Shape l = new Line2D.Double(sX, sY, sX = screenXd(wX), sY = screenYd(wZ));
        g.draw(l);
    }

    public void drawThickLine(double wX, double wZ) {
        g.setColor(color);
        g.setStroke(thickLine);
        double sXnew = screenXd(wX);
        double sYnew = screenYd(wZ);
        Shape l = new Line2D.Double(sX, sY, sXnew, sYnew);
        g.draw(l);
        g.setStroke(normalLine);
        
        sX = sXnew;
        sY = sYnew;
    }

    public void drawDashedLineRel(double wX, double wZ) {
        g.setColor(color);
        g.setStroke(dashedLine);
        double sXnew = screenXd(worldX(sX) + wX);
        double sYnew = screenYd(worldZ(sY) + wZ);
        Shape l = new Line2D.Double(sX, sY, sXnew, sYnew);
        g.draw(l);
        g.setStroke(normalLine);

        sX = sXnew;
        sY = sYnew;
    }

    public void fillPolygon(double ax[], double az[], int l) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(screenXd(ax[0]), screenYd(az[0]));
        for (int n = 1; n < l; ++n) {
            p.lineTo(screenXd(ax[n]), screenYd(az[n]));
        }
        p.lineTo(screenXd(ax[0]), screenYd(az[0]));
        g.setColor(color);
        g.fill(p);
    }

    public void drawPolygon(double ax[], double az[], int l) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(screenXd(ax[0]), screenYd(az[0]));
        for (int n = 1; n < l; ++n) {
            p.lineTo(screenXd(ax[n]), screenYd(az[n]));
        }
        p.lineTo(screenXd(ax[0]), screenYd(az[0]));
        g.setColor(color);
        g.draw(p);
    }

    // Zeichenfunktionen in Basis-Einheiten
    public void drawBLineRel(double dbX, double dbY) {
        g.setColor(color);
        Shape l = new Line2D.Double(sX, sY, sX += dbX * baseSize, sY += dbY * baseSize);
        g.draw(l);
    }

    public void moveBRel(double dbX, double dbY) {
        sX += dbX * baseSize;
        sY += dbY * baseSize;
    }

    public void drawBCircle(double bR) {
        g.setColor(color);
        int r = (int) (bR * baseSize);
        Shape e = new Ellipse2D.Double(sX - r, sY - r, 2 * r, 2 * r);
        g.draw(e);
    }

    public void drawBArc(double bR, int startAngle, int arcAngle) {
        g.setColor(color);
        int r = (int) (bR * baseSize);
        Shape a = new Arc2D.Double(sX - r, sY - r, 2 * r, 2 * r, startAngle, arcAngle, Arc2D.OPEN);
        g.draw(a);
    }

    public void fillBPolygonRel(double ax[], double ay[], int l) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(sX + ax[0] * baseSize, sY + ay[0] * baseSize);
        for (int n = 1; n < l; ++n) {
            p.lineTo(sX + ax[n] * baseSize, sY + ay[n] * baseSize);
        }
        p.lineTo(sX + ax[0] * baseSize, sY + ay[0] * baseSize);
        g.setColor(color);
        g.fill(p);
    }

    public void drawBPolygonRel(double ax[], double ay[], int l) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(sX + ax[0] * baseSize, sY + ay[0] * baseSize);
        for (int n = 1; n < l; ++n) {
            p.lineTo(sX + ax[n] * baseSize, sY + ay[n] * baseSize);
        }
        p.lineTo(sX + ax[0] * baseSize, sY + ay[0] * baseSize);
        g.setColor(color);
        g.draw(p);
    }

    // Farbwahl
    public void colorStd() {
        color = Color.black;
    }

    public void colorSel() {
        color = Color.blue;
    }

    public void colorDis() {
        color = Color.blue;
    }

    public void colorRef() {
        color = Color.red;
    }

    public void colorSect() {
        color = Color.red;
    }

    private static final Color fillColor = new Color(1.0f, 1.0f, 0.0f, 0.5f);
    public void colorSectFill() {
        color = fillColor;
    }

    public void colorLoad() {
        color = Color.red;
    }

    public void colorLoadN() {
        color = Color.green;
    }

    public void colorLabel() {
        color = Color.green.darker();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    // Textausgabe
    public void drawString(String str) {
        g.setColor(color);
        g.drawString(str, (float)sX, (float)sY);
    }

    /** Zoom */
    public void zoom(double f) {
        // Fixpunkt = Mittelpunkt
        int mX = getSize().width / 2;
        int mY = getSize().height / 2;
        oX -= (int) ((mX - oX) * f + oX - mX);
        oY -= (int) ((mY - oY) * f + oY - mY);
        fX *= f;
        fZ *= f;
    }

    /** Pan (world coordinates) */
    public void pan(double dwX, double dwZ) {
        oX += dwX * fX;
        oY += dwZ * fZ;
    }
    
    /** Pan (screen coordinates) */
    public void pan(int dX, int dY) {
        oX += dX;
        oY += dY;
    }

    /** Zoom All */
    public void showAll() {
        Iterator<Node> nodeIterator = model.getNodeIterator().iterator();

        double x1, x2, z1, z2;

        if (!nodeIterator.hasNext()) {
            return;
        }

        Node node = (Node) nodeIterator.next();
        x1 = x2 = node.getX();
        z1 = z2 = node.getZ();

        while (nodeIterator.hasNext()) {
            node = nodeIterator.next();
            if (node.getX() < x1) {
                x1 = node.getX();
            }
            if (node.getX() > x2) {
                x2 = node.getX();
            }
            if (node.getZ() < z1) {
                z1 = node.getZ();
            }
            if (node.getZ() > z2) {
                z2 = node.getZ();
            }
        }

        x1 -= (x2 - x1) * 0.1;
        x2 += (x2 - x1) * 0.1;
        z1 -= (z2 - z1) * 0.1;
        z2 += (z2 - z1) * 0.1;
        zoomWindow(x1, z1, x2, z2);
    }

    /** Zoom Window */
    public void zoomWindow(double x1, double z1, double x2, double z2) {
        int swX = getSize().width;
        int swY = getSize().height;
        fX = Math.abs(swX / (x2 - x1));
        fZ = Math.abs(swY / (z2 - z1));
        fX = Math.min(fX, fZ);
        fZ = fX;
        oX += swX / 2 - ((x1 + x2) * 0.5 * fX + oX);
        oY += swY / 2 - ((z1 + z2) * 0.5 * fZ + oY);
    }

    // Grid
    public double getGrid() {
        return grid;
    }

    public void setGrid(double grid) {
        this.grid = grid;
    }

    public double getBaseSize() {
        return baseSize;
    }

    public void setBaseSize(double baseSize) {
        this.baseSize = baseSize;
    }

    public void setShowDisplacement(boolean showDisplacement) {
        this.showDisplacement = showDisplacement;
    }

    public double getDisplacementScale() {
        return displacementScale;
    }

    public void setDisplacementScale(double displacementScale) {
        this.displacementScale = displacementScale;
    }

    public void setShowShearForce(boolean showShearForce) {
        this.showShearForce = showShearForce;
    }

    public double getShearForceScale() {
        return shearForceScale;
    }

    public void setShearForceScale(double shearForceScale) {
        this.shearForceScale = shearForceScale;
    }

    public void setShowNormalForce(boolean showNormalForce) {
        this.showNormalForce = showNormalForce;
    }

    public double getNormalForceScale() {
        return normalForceScale;
    }

    public void setNormalForceScale(double normalForceScale) {
        this.normalForceScale = normalForceScale;
    }

    public void setShowMoment(boolean showMoment) {
        this.showMoment = showMoment;
    }

    public double getMomentScale() {
        return momentScale;
    }

    public void setMomentScale(double momentScale) {
        this.momentScale = momentScale;
    }
}
