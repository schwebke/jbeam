package com.schwebke.jbeam.model;

import com.schwebke.jbeam.plugin.*;
import com.schwebke.math.*;

import java.util.*;
import java.io.*;

/**
 * Model. Stores geometry, topology and material properties,
 * performs the calculation.
 * This model can run as a "calculation core" even without a graphical
 * frontend.
 */
public class Model implements Serializable, IModel {

    static final long serialVersionUID = -5300914168613980355L;
    /// List of nodes.
    protected ArrayList<Node> nodeList;
    /// List of beam elements.
    protected ArrayList<Beam> beamList;
    /// True if this model contains valid (current) calculation results.
    protected boolean validCalculation;
    /** global stiffness matrix */
    protected double K[][];
    /** global mass matrix */
    protected double M[][];
    /** global load vector */
    protected double p[];
    /** eigenvectors */
    protected double EigenVec[][];
    /** eigenfrequencies */
    protected double f[];
    /** active eigenmode */
    protected int mode;
    /// Number of global DOFs
    protected int r;

    /**
     * Creates a new, empty model.
     */
    public Model() {
        nodeList = new ArrayList<Node>();
        beamList = new ArrayList<Beam>();
        validCalculation = false;
        K = null;
        M = null;
        p = null;
        EigenVec = null;
        mode = 0;
        r = 0;
    }

    /**
     * Führt die internen Vektorinformationen der
     * Beamelemente nach. Sollte nach Verschieben
     * von Knoten aufgerufen werden.
     */
    protected void calculateBeamVectors() {
        for (Beam b : beamList) {
            b.calculateVector();
        }
    }

    /// Liefert einen Iterator auf die Knotenliste.
    @Override
    public Iterable<Node> getNodeIterator() {
        return nodeList;
    }

    /// Liefert einen Iterator auf die Beamelementliste.
    @Override
    public Iterable<Beam> getBeamIterator() {
        return beamList;
    }

    /**
     * Liefert true, wenn dieses Model gültige
     * (aktuelle) Ergebnisse enthält.
     */
    public boolean getValidCalculation() {
        return validCalculation;
    }

    /// Fügt einen Knoten in das Model ein.
    @Override
    public void addNode(Node node) {
        nodeList.add(node);
        validCalculation = false;
    }

    /// Liefert die eindeutige Nummer eines Knotens.
    @Override
    public int getNodeIndex(Node node) {
        return nodeList.indexOf(node);
    }

    /// Liefert den Knoten zur angegebenen Nummer.
    @Override
    public Node getIndexNode(int index) {
        return nodeList.get(index);
    }

    /// Fügt ein Beamelement in das Model ein.
    @Override
    public void addBeam(Beam beam) {
        beamList.add(beam);
        validCalculation = false;
    }

    /// Liefert die eindeutige Nummer eines Beamelementes.
    @Override
    public int getBeamIndex(Beam beam) {
        return beamList.indexOf(beam);
    }

    /// Liefert das Beamelement zur angegebenen Nummer.
    @Override
    public Beam getIndexBeam(int index) {
        return beamList.get(index);
    }

    /// Löscht das Model.
    public void clearModel() {
        beamList.clear();
        nodeList.clear();
        validCalculation = false;
    }

    /**
     * Static analysis of the system.
     */
    public void calculate() {
        // Set up system matrices
        calculateGlobalMatrices();

        // Solve the linear equation system
        Solver.cholesky(K, p);

        // Back-calculation
        postCalculate(p);

        validCalculation = true;
    }

    /**
     * Modal analysis of the system.
     */
    public void calculateModal() {
        // Set up system matrices
        calculateGlobalMatrices();

        // Reduction to standard eigenvalue problem
        double tmp[] = new double[r];
        double dK[][] = Matrix.duplicate(K);
        for (int i = 0; i < r; ++i) {
            tmp[i] = 1.;
        }
        Solver.cholesky(dK, tmp);

        double LK[][] = new double[r][r];
        for (int i = 0; i < r - 1; ++i) {
            for (int j = i + 1; j < r; ++j) {
                LK[i][j] = 0.;
            }
        }
        for (int i = 0; i < r; ++i) {
            for (int j = 0; j <= i; ++j) {
                LK[i][j] = dK[i][j];
            }
        }

        double LKT[][] = Matrix.transpose(LK);

        double invLKT[][] = Matrix.invert(LKT);
        double invLK[][] = Matrix.transpose(invLKT);

        double A[][] = Matrix.multiply(invLK, Matrix.multiply(M, invLKT));

        // Determine eigenvalues and eigenvectors
        double V[][] = Eigen.cyclJac(A);

        // Sort eigenvalues
        double eigenVal[] = new double[r];
        int eigenValIdx[] = new int[r];
        for (int i = 0; i < r; ++i) {
            eigenValIdx[i] = i;
            eigenVal[i] = A[i][i];
        }
        for (int i = 0; i < r - 1; ++i) {
            double max = eigenVal[i];
            int maxJ = i;
            for (int j = i + 1; j < r; ++j) {
                if (eigenVal[j] > max) {
                    max = eigenVal[j];
                    maxJ = j;
                }
            }

            int ti = eigenValIdx[i];
            double tv = eigenVal[i];

            eigenValIdx[i] = eigenValIdx[maxJ];
            eigenVal[i] = eigenVal[maxJ];

            eigenValIdx[maxJ] = ti;
            eigenVal[maxJ] = tv;
        }

        // Back-transformation
        EigenVec = new double[r][r];
        f = new double[r];
        for (int i = 0; i < r; ++i) {
            double omega = Math.sqrt(1. / eigenVal[i]);
            f[i] = omega / (2. * Math.PI);

            for (int j = 0; j < r; ++j) {
                tmp[j] = V[j][eigenValIdx[i]];
            }
            tmp = Matrix.multiply(invLKT, tmp);

            double scale = 0.;
            for (int j = 0; j < r; ++j) {
                if (Math.abs(tmp[j]) > scale) {
                    scale = Math.abs(tmp[j]);
                }
            }
            for (int j = 0; j < r; ++j) {
                EigenVec[i][j] = 0.01 * tmp[j] / scale;
            }
        }

        validCalculation = true;
        setMode(0);
    }

    /** Eigenform i aktivieren */
    public void setMode(int i) {
        if (validCalculation) {
            mode = i;
            postCalculate(EigenVec[i]);
        }
    }

    public int getMode() {
        return mode;
    }

    public double getFreq() {
        if (f != null) {
            return f[mode];
        }
        return 0.0;
    }

    public void nextMode() {
        if ((validCalculation) && (EigenVec != null)) {
            ++mode;
            if (mode >= r) {
                mode = 0;
            }
            postCalculate(EigenVec[mode]);
        }
    }

    public void prevMode() {
        if ((validCalculation) && (EigenVec != null)) {
            --mode;
            if (mode < 0) {
                mode = r - 1;
            }
            postCalculate(EigenVec[mode]);
        }
    }

    /**
     * Calculate the global stiffness and mass matrices as well as
     * the global load vector.
     */
    protected void calculateGlobalMatrices() {
        // Determine and number the global degrees of freedom
        r = 0;
        for (Node node : nodeList) {
            if (!node.cX) {
                r++;
                node.nX = r;
            } else {
                node.nX = 0;
            }

            if (!node.cZ) {
                r++;
                node.nZ = r;
            } else {
                node.nZ = 0;
            }

            if (!node.cR) {
                r++;
                node.nR = r;
            } else {
                node.nR = 0;
            }
        }

        if (r == 0) {
            throw new ModelException("no global DOFs");
        }

        K = new double[r][r];
        M = new double[r][r];
        p = new double[r];
        for (int n = 0; n < r; n++) {
            p[n] = 0.;
        }

        // Generate the location matrix and the system stiffness matrix K
        for (Beam beam : beamList) {
            int locationVector[] = new int[6];
            locationVector[0] = beam.n1.nX;
            locationVector[1] = beam.n1.nZ;
            locationVector[2] = beam.n1.nR;
            locationVector[3] = beam.n2.nX;
            locationVector[4] = beam.n2.nZ;
            locationVector[5] = beam.n2.nR;

            // Berechnung der Elementmatrizen und des Elementlastvektors
            beam.calSg();

            // Aufaddieren der Elementeinträge in die Systemmatrizen
            for (int i = 0; i < 6; i++) {
                if (locationVector[i] != 0) {
                    K[locationVector[i] - 1][locationVector[i] - 1] += beam.Sg[i][i];
                    M[locationVector[i] - 1][locationVector[i] - 1] += beam.Mg[i][i];
                }
                for (int j = i + 1; j < 6; j++) {
                    if ((locationVector[i] != 0) && (locationVector[j] != 0)) {
                        K[locationVector[i] - 1][locationVector[j] - 1] += beam.Sg[i][j];
                        K[locationVector[j] - 1][locationVector[i] - 1] += beam.Sg[j][i];
                        M[locationVector[i] - 1][locationVector[j] - 1] += beam.Mg[i][j];
                        M[locationVector[j] - 1][locationVector[i] - 1] += beam.Mg[j][i];
                    }
                }
            }

            // Aufaddieren des Elementlastvektors in den Systemlastvektor
            if (!beam.n1.cX) {
                p[beam.n1.nX - 1] -= beam.Lg[0];
            }
            if (!beam.n1.cZ) {
                p[beam.n1.nZ - 1] -= beam.Lg[1];
            }
            if (!beam.n1.cR) {
                p[beam.n1.nR - 1] -= beam.Lg[2];
            }
            if (!beam.n2.cX) {
                p[beam.n2.nX - 1] -= beam.Lg[3];
            }
            if (!beam.n2.cZ) {
                p[beam.n2.nZ - 1] -= beam.Lg[4];
            }
            if (!beam.n2.cR) {
                p[beam.n2.nR - 1] -= beam.Lg[5];
            }
        }

        // Aufaddieren der Knotenlasten in den Systemlastvektor
        for (Node node : nodeList) {
            if (!node.cX) {
                p[node.nX - 1] += node.Fx;
            }
            if (!node.cZ) {
                p[node.nZ - 1] += node.Fz;
            }
            if (!node.cR) {
                p[node.nR - 1] += node.M;
            }
        }
    }

    /**
     * Verschiebungen in Elemente eintragen, Berechnung der von den Verschiebungen
     * abhängigen Grössen.
     */
    protected void postCalculate(double p[]) {
        // Verschiebungen in die Knoten schreiben
        for (Node node : nodeList) {
            if (!node.cX) {
                node.dX = p[node.nX - 1];
            } else {
                node.dX = 0.;
            }

            if (!node.cZ) {
                node.dZ = p[node.nZ - 1];
            } else {
                node.dZ = 0.;
            }

            if (!node.cR) {
                node.dR = p[node.nR - 1];
            } else {
                node.dR = 0.;
            }
        }

        // Berechnung von Schnittgrößen und anderen Folgeberechnungen der Elemente
        for (Beam beam : beamList) {
            beam.postCalculate();
        }

        // Berechnung der Auflaggerreaktionen
        //    zurücksetzen
        for (Node node : nodeList) {
            node.rFx = 0.;
            node.rFz = 0.;
            node.rM = 0.;
        }

        //    aufaddieren
        for (Beam beam : beamList) {

            double Sl[][] = new double[6][1];
            Sl[0][0] = -beam.Ni;
            Sl[1][0] = -beam.Vi;
            Sl[2][0] = -beam.Mi;
            Sl[3][0] = beam.Nk;
            Sl[4][0] = beam.Vk;
            Sl[5][0] = beam.Mk;

            double Sg[][] =
                    Matrix.multiply(
                    Matrix.transpose(beam.a),
                    Sl);

            if (beam.n1.cX) {
                beam.n1.rFx += Sg[0][0];
            }
            if (beam.n1.cZ) {
                beam.n1.rFz += Sg[1][0];
            }
            if (beam.n1.cR) {
                beam.n1.rM += Sg[2][0];
            }

            if (beam.n2.cX) {
                beam.n2.rFx += Sg[3][0];
            }
            if (beam.n2.cZ) {
                beam.n2.rFz += Sg[4][0];
            }
            if (beam.n2.cR) {
                beam.n2.rM += Sg[5][0];
            }
        }
    }
}
