package com.schwebke.awt.tools.display;
import java.awt.*;

/**
  * Inhaltsobjekt für Display:
  * Zeichnet einen Balken.
  */
public class Bar
{
   int width;
   int height;
   int space;
   Color border;
   Color background;
   Color fill;
   double ratio;

   /**
     * Erzeugt einen neuen Balken angegebener Grösse.
     */
   public Bar(int width, int height)
   {
      this.width = width;
      this.height = height;
      space = 10;
      border = Color.black;
      background = Color.white;
      fill = Color.red;
      ratio = 0.;
   }

   /**
     * Setzt das vom Balken dazustellende Verhältnis (zwischen 0 und 1).
     */
   public void setRatio(double ratio)
   {
      this.ratio = ratio;
   }

   /**
     * Liefert das vom Balken dazustellende Verhältnis (zwischen 0 und 1).
     */
   public double getRatio()
   {
      return ratio;
   }

   /**
     * Farbe des Randes.
     */
   public Color getBorder()
   {
      return border;
   }

   /**
     * Farbe des Hintergrundes.
     */
   public Color getBackground()
   {
      return background;
   }

   /**
     * Farbe der Füllung.
     */
   public Color getFill()
   {
      return fill;
   }

   /**
     * Breite des Balkens.
     */
   public int getWidth()
   {
      return width;
   }

   /**
     * Höhe des Balkens.
     */
   public int getHeight()
   {
      return height;
   }

   /**
     * Abstand nach dem Balken zu den folgenden Inhalten.
     */
   public int getSpace()
   {
      return space;
   }
}
