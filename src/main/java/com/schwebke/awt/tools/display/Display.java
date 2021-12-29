package com.schwebke.awt.tools.display;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * Panel zur Ausgabe von formatiertem Text.
 */
public class Display
        extends JPanel {

    private static final RenderingHints rh;

    static {
        rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.add(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
    }
    
    
    protected Dimension size;
    protected List content;
    protected double lineHeight;
    protected int baseIndent;

    /**
     * Neues Panel der angegebenen Grösse erzeugen.
     */
    public Display(Dimension size) {
        this.size = size;
        content = new ArrayList();
        lineHeight = 1.2;
        baseIndent = 5;
    }

    /**
     * Neues Panel der angegebenen Grösse erzeugen.
     * Die Inhalte werden im Abstand "baseIndent" vom
     * linken Rand her eingerückt.
     */
    public Display(Dimension size, int baseIndent) {
        this.size = size;
        content = new ArrayList();
        lineHeight = 1.2;
        this.baseIndent = baseIndent;
    }

    /**
     * Liefert den Container mit den dazustellenden Inhalten.
     */
    public List getContent() {
        return content;
    }

    /**
     * Setzt den Container mit den dazustellenden Inhalten.
     */
    public void setContent(List content) {
        this.content = content;
    }

    @Override
    public void paint(Graphics gAwt) {
        Graphics2D g = (Graphics2D)gAwt;
        g.addRenderingHints(rh);
        
        int x = baseIndent;
        int y = 1;

        List line = new ArrayList();

        for (Object item : content) {

            if (item instanceof NewLine) {
                int vSkip = ((NewLine) item).getVSkip();
                x = baseIndent;
                y += lineHeight * getSize(line).height;
                paintLine(g, line, x, y);
                y += vSkip;
                line = new ArrayList();
            } else {
                line.add(item);
            }
        }
        y += lineHeight * getSize(line).height;
        paintLine(g, line, x, y);
    }

    protected void paintLine(Graphics g, List line, int x, int y) {
        for (Object item : line) {
            if (item instanceof Text) {
                Text t = (Text) item;
                g.setColor(t.getColor());
                g.setFont(t.getFont());
                g.drawString(t.getText(), x, y);
                x += getSize(t).width;
            }

            if (item instanceof Bar) {
                Bar b = (Bar) item;
                g.setColor(b.getBorder());
                g.drawRect(x, y - b.getHeight(), b.getWidth(), b.getHeight());
                g.setColor(b.getBackground());
                g.fillRect(x + 1, y - b.getHeight() + 1, b.getWidth() - 1, b.getHeight() - 1);
                g.setColor(b.getFill());
                g.fillRect(x + 1, y - b.getHeight() + 1, (int) ((b.getWidth() - 1) * b.getRatio()), b.getHeight() - 1);

                x += b.getWidth() + b.getSpace();
            }

            if (item instanceof HMoveTo) {
                x = ((HMoveTo) item).getHPos() + baseIndent;
            }
        }
    }

    protected Dimension getSize(Text t) {
        FontMetrics fm = getFontMetrics(t.getFont());

        return new Dimension(fm.stringWidth(t.text), fm.getHeight());
    }

    protected Dimension getSize(List line) {
        int height = 0;
        int width = 0;

        for (Object item : line) {
            Dimension size = null;
            if (item instanceof Text) {
                size = getSize((Text) item);
            }

            if (size != null) {
                if (size.height > height) {
                    height = size.height;
                }
                width += size.width;
            }
        }

        return new Dimension(width, height);
    }

    public Dimension getMinimumSize() {
        return size;
    }

    public Dimension getMaximumSize() {
        return size;
    }

    public Dimension getPreferredSize() {
        return size;
    }
}
