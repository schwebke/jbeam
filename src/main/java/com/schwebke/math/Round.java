package com.schwebke.math;

/**
  * Runden von Zahlenwerten.
  */
public class Round
{
   /**
     * Runden des übergebenen Wertes auf die angegebene Stellenzahl.
     * Die Stellenzahl darf negativ sein (z.B. digits(12345, -2) == 12300).
     */
   public static double digits(double value, int digits)
   {
      double exponent = Math.pow(10., digits);
      return ((double)Math.round(value*exponent))/exponent;
   }
}
