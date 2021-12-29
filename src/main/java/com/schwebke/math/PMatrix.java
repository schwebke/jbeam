package com.schwebke.math;

/**
 * Matrix mit Profilspeicherung
 */
public class PMatrix
{
   /// Neue Matrix erzeugen
   public PMatrix(int rows, int cols)
   {
      this.rows = rows;
      this.cols = cols;
      minCol = new int [rows];
      maxCol = new int [rows];
      minRow = new int [cols];
      maxRow = new int [cols];
      m = new double [rows][];

      for (int i = 0; i < rows; ++i)
      {
	 m[i] = null;
	 minCol[i] = 0;
	 maxCol[i] = 0;
      }
      for (int i = 0; i < cols; ++i)
      {
	 minRow[i] = -1;
	 maxRow[i] = -1;
      }
   }


   /// Matrix kopieren
   public static PMatrix duplicate(PMatrix A)
   {
      PMatrix M = new PMatrix(A.rows, A.cols);

      for (int i = 0; i < M.rows; ++i)
      {
	 M.minCol[i] = A.minCol[i];
	 M.maxCol[i] = A.maxCol[i];
	 if (A.m[i] != null)
	 {
	    M.m[i] = new double [M.maxCol[i]-M.minCol[i]];
	    for (int j = 0; j < M.m.length; ++j)
	    {
	       M.m[i][j] = A.m[i][j];
	    }
	 }
      }
      for (int i = 0; i < M.cols; ++i)
      {
	 M.minRow[i] = A.minRow[i];
	 M.minCol[i] = A.minCol[i];
      }

      return M;
   }

   /// Element an der Stelle (row, col) setzen, dabei geg. die Matrix vergrößern
   public void set(int row, int col, double v)
   {
      check(row, col);

      if (m[row] == null)
      {
	 m[row] = new double [1];
	 minCol[row] = col;
	 maxCol[row] = col+1;
	 m[row][0] = v;
	 return;
      }

      if ( (col >= minCol[row]) && (col < maxCol[row]) )
      {
	 m[row][col-minCol[row]] = v;
	 return;
      }

      int minNew = Math.min(minCol[row], col);
      int maxNew = Math.max(maxCol[row], col+1);
      double[] mNew = new double [maxNew-minNew];


      for (int i = 0; i < (maxNew-minNew); ++i)
      {
	 mNew[i] = 0.;
      }
      for (int i = (minCol[row]-minNew); i < (maxCol[row]-minNew); ++i)
      {
	 mNew[i] = m[row][i-minCol[row]+minNew];
      }


      m[row] = mNew;
      minCol[row] = minNew;
      maxCol[row] = maxNew;

      m[row][col-minNew] = v;
      return;
   }


   /// Element an der Stelle (row, col) lesen. (Keine Matrixvergrößerung)
   public double get(int row, int col)
   {
      check(row, col);

      if (m[row] == null)
      {
	 return 0.;
      }

      if ((minCol[row] <= col) && (col < maxCol[row]))
      {
	 return m[row][col-minCol[row]];
      }

      return 0.;
   }

   /// Wert an der Stelle (row, col) aufaddieren, dabei geg. Matrix vergrößern
   public void add(int row, int col, double v)
   {
      set(row, col, get(row, col)+v);
   }

   /// Element an der Stelle (row, col) setzen (Keine Matrixvergrößerung, keine Prüfung)
   public void setUnchecked(int row, int col, double v)
   {
      m[row][col-minCol[row]] = v;
      return;
   }

   /// Element an der Stelle (row, col) lesen. (Keine Matrixvergrößerung, keine Prüfung)
   public double getUnchecked(int row, int col)
   {
      return m[row][col-minCol[row]];
   }

   /// Prüfen, ob ein Element an der Stelle (row, col) gespeichert ist.
   public boolean exist(int row, int col)
   {
      check(row, col);
      if (m[row] != null)
      {
	 if ((col >= minCol[row]) && (col < maxCol[row]))
	 {
	    return true;
	 }
      }
      return false;
   }

   /// Anzahl der Zeilen
   public int getRows()
   {
      return rows;
   }

   /// Anzahl der Spalten
   public int getCols()
   {
      return cols;
   }

   /// Prüfen ob Zeile vorhanden ist.
   public boolean existRow(int row)
   {
      return (m[row] != null);
   }

   /// Index der ersten besetzten Spalte
   public int getMinCol(int row)
   {
      return minCol[row];
   }

   /// Index der letzten besetzten Spalte
   public int getMaxCol(int row)
   {
      return maxCol[row];
   }

   /**
     * Index der ersten besetzten Zeile.
     * Nach Änderungen an der Matrix muss updateRowBounds()
     * aufgerufen werden, wenn diese Funktion verwendet wird.
     */
   public int getMinRow(int col)
   {
      return minRow[col];
   }

   /**
     * Index der letzten besetzten Zeile.
     * Nach Änderungen an der Matrix muss updateRowBounds()
     * aufgerufen werden, wenn diese Funktion verwendet wird.
     */
   public int getMaxRow(int col)
   {
      return maxRow[col];
   }

   /// Min und MaxRow-Werte aktualisieren
   public void updateRowBounds()
   {
      int row;
      for (row = 0; row < rows; ++row)
      {
	 int col;
	 for (col = minCol[row]; col < maxCol[row]; ++col)
	 {
	    if ( getUnchecked(row, col) != 0.)
	    {
	       minRow[col] = Math.min(minRow[col], row);
	       maxRow[col] = Math.max(maxRow[col], row+1);
	    }
	 }
      }
   }

   /// Matrix-Vektor-Produkt
   public static double[] multiply(PMatrix A, double[] b)
   {
      int n = A.getCols();
      double[] r = new double [n];

      for (int i = 0; i < n; ++i)
      {
	 double sum = 0.;
	 int j;
	 for (j = A.getMinCol(i); j < A.getMaxCol(i); ++j)
	 {
	    sum += A.getUnchecked(i,j)*b[j];
	 }
	 r[i] = sum;
      }
      return r;
   }

   protected int rows;
   protected int cols;
   protected int[] minCol;
   protected int[] maxCol;
   protected int[] minRow;
   protected int[] maxRow;
   protected double[][] m;

   protected void check(int row, int col)
   {
      if (row < 0)
      {
	 throw new ArrayIndexOutOfBoundsException("PMatrix: row index underflow");
      }
      if (row >= rows)
      {
	 throw new ArrayIndexOutOfBoundsException("PMatrix: row index overflow");
      }
      if (col < 0)
      {
	 throw new ArrayIndexOutOfBoundsException("PMatrix: col index underflow");
      }
      if (col >= cols)
      {
	 throw new ArrayIndexOutOfBoundsException("PMatrix: col index overflow");
      }
   }
}
