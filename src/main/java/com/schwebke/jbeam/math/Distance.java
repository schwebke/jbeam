package com.schwebke.jbeam.math;

/**
  * Interface zur Abstandsbestimmung.
  */

public interface Distance
{
   /**
     * Liefert Abstand des Objektes zu einem Punkt.
     */
    public double getDistance(double x, double z);
}
