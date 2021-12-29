package com.schwebke.awt.tools.display;
import java.awt.*;

/**
  * Inhaltsobjekt für Display:
  * Text.
  */
public class Text
{
   String text;
   Color color;
   Font font;

   /**
     * Text erzeugen.
     */
   public Text(String text)
   {
      this.text = text;
      color = Color.black;
      font = new Font("SansSerif", Font.PLAIN, 10);
   }

   /**
     * Farbe des Textes.
     */
   public void setColor(Color color)
   {
      this.color = color;
   }

   /**
     * Farbe des Textes.
     */
   public Color getColor()
   {
      return color;
   }

   /**
     * Text.
     */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
     * Text.
     */
   public String getText()
   {
      return text;
   }

   /**
     * Schriftart.
     */
   public void setFont(Font font)
   {
      this.font = font;
   }

   /**
     * Schriftart.
     */
   public Font getFont()
   {
      return font;
   }
}
