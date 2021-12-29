package com.schwebke.awt.tools.display;

/**
  * Inhaltsobjekt für Display:
  * Neue Zeile beginnen.
  */
public class NewLine
{
   protected int vSkip;

   /**
     * Zeilenumbruch.
     */
   public NewLine()
   {
      vSkip = 0;
   }

   /**
     * Zeilenumbruch mit angegebenem zusätzlichem vertikalen Abstand.
     */
   public NewLine(int vSkip)
   {
      setVSkip(vSkip);
   }

   /**
     * Vertikaler Abstand.
     */
   public int getVSkip()
   {
      return vSkip;
   }

   /**
     * Vertikaler Abstand.
     */
   public void setVSkip(int vSkip)
   {
      this.vSkip = vSkip;
   }
}
