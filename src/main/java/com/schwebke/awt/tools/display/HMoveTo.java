package com.schwebke.awt.tools.display;

/**
  * Inhaltsobjekt für Display:
  * Bewegt die aktuelle horizontale Position zu der
  * angegebenen Position.
  */
public class HMoveTo
{
   protected int hPos;

   /**
     * Sprung an den linken Rand.
     */
   public HMoveTo()
   {
      hPos = 0;
   }

   /**
     * Sprung an die angegebene Postion.
     */
   public HMoveTo(int hPos)
   {
      setHPos(hPos);
   }

   /**
     * Position.
     */
   public int getHPos()
   {
      return hPos;
   }

   /**
     * Position.
     */
   public void setHPos(int hPos)
   {
      this.hPos = hPos;
   }
}
