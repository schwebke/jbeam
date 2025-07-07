package com.schwebke.jbeam.view;

import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.*;

import java.io.*;
import java.util.*;

/** Alternative view for ASCII text result output */
public class TextView {

    Model model;
    IController controller;
    boolean showAllItems;
    boolean dumpMatrices;

    public TextView(Model model, IController controller) {
        this(model, controller, false);
    }
    
    public TextView(Model model, IController controller, boolean showAllItems) {
        this(model, controller, showAllItems, false);
    }
    
    public TextView(Model model, IController controller, boolean showAllItems, boolean dumpMatrices) {
        this.model = model;
        this.controller = controller;
        this.showAllItems = showAllItems;
        this.dumpMatrices = dumpMatrices;
    }

    String format(double number) {
        return controller.getNumberFormat().format(number);
    }
    
    /**
     * Generate JSON-style ID for a node based on its position in the model.
     */
    private String getNodeId(Node targetNode) {
        int counter = 1;
        for (Node node : model.getNodeIterator()) {
            if (node == targetNode) {
                return "node-" + counter;
            }
            counter++;
        }
        return "node-unknown";
    }
    
    /**
     * Generate JSON-style ID for a beam based on its position in the model.
     */
    private String getBeamId(Beam targetBeam) {
        int counter = 1;
        for (Beam beam : model.getBeamIterator()) {
            if (beam == targetBeam) {
                return "beam-" + counter;
            }
            counter++;
        }
        return "beam-unknown";
    }
    
    /**
     * Get display name for a node (label if available, otherwise JSON-style ID).
     */
    private String getNodeDisplayName(Node node) {
        if (node.getLabel() != null && !node.getLabel().trim().isEmpty()) {
            return "'" + node.getLabel() + "' (" + getNodeId(node) + ")";
        } else {
            return getNodeId(node);
        }
    }
    
    /**
     * Get display name for a beam (label if available, otherwise JSON-style ID).
     */
    private String getBeamDisplayName(Beam beam) {
        if (beam.getLabel() != null && !beam.getLabel().trim().isEmpty()) {
            return "'" + beam.getLabel() + "' (" + getBeamId(beam) + ")";
        } else {
            return getBeamId(beam);
        }
    }
    
    /**
     * Check if node should be included in output.
     */
    private boolean shouldIncludeNode(Node node) {
        if (showAllItems) {
            return true;
        }
        return node.getLabel() != null && !node.getLabel().trim().isEmpty();
    }
    
    /**
     * Check if beam should be included in output.
     */
    private boolean shouldIncludeBeam(Beam beam) {
        if (showAllItems) {
            return true;
        }
        return beam.getLabel() != null && !beam.getLabel().trim().isEmpty();
    }

    public void write(PrintWriter writer) {
        try {
            FileInputStream localeRes = new FileInputStream("resourcen/locale.txt");
            PropertyResourceBundle locale = new PropertyResourceBundle(localeRes);


            writer.println(locale.getString("ResTitle"));
            writer.println();
            writer.println();

            if (!model.getValidCalculation()) {
                model.calculate();
                if (!model.getValidCalculation()) {
                    return;
                }
            }


            writer.println("   " + locale.getString("ResSupportReactions"));
            for (Node node : model.getNodeIterator()) {
                if (shouldIncludeNode(node) && (node.getCX() || node.getCZ() || node.getCR())) {
                    writer.println("      " + locale.getString("ResNode")
                            + " " + getNodeDisplayName(node));
                    if (node.getCX()) {
                        writer.println("         Fx = " + format(node.getRFx()));
                    }
                    if (node.getCZ()) {
                        writer.println("         Fz = " + format(node.getRFz()));
                    }
                    if (node.getCR()) {
                        writer.println("         M  = " + format(node.getRM()));
                    }
                }
            }

            writer.println();
            writer.println("   " + locale.getString("ResNodalDisplacements"));
            for (Node node : model.getNodeIterator()) {
                if (shouldIncludeNode(node)) {
                    writer.println("      " + locale.getString("ResNode")
                            + " " + getNodeDisplayName(node));
                    writer.println("         dx = " + format(node.getDX()));
                    writer.println("         dz = " + format(node.getDZ()));
                    writer.println("         dr = " + format(node.getDR()));
                }
            }

            writer.println();
            writer.println("   " + locale.getString("ResStressResultants"));
            for (Beam element : model.getBeamIterator()) {
                if (element instanceof EBBeam) {
                    EBBeam beam = (EBBeam) element;
                    if (shouldIncludeBeam(beam)) {
                        writer.println("      EB-beam " + getBeamDisplayName(beam));
                        writer.println("         Ni = " + format(beam.N(0.)));
                        writer.println("         Vi = " + format(beam.V(0.)));
                        writer.println("         Mi = " + format(beam.M(0.)));
                        writer.println("         Nk = " + format(beam.N(1.)));
                        writer.println("         Vk = " + format(beam.V(1.)));
                        writer.println("         Mk = " + format(beam.M(1.)));
                    }
                }

                if (element instanceof Truss) {
                    Truss truss = (Truss) element;
                    if (shouldIncludeBeam(truss)) {
                        writer.println("      Truss " + getBeamDisplayName(truss));
                        writer.println("         N  = " + format(truss.N(0.)));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("TextView error: " + e.getMessage());
        }
        
        // Dump matrices if requested
        if (dumpMatrices) {
            dumpElementMatrices(writer);
        }
    }
    
    /**
     * Dump element matrices for debugging.
     */
    private void dumpElementMatrices(PrintWriter writer) {
        writer.println();
        writer.println("===============================================");
        writer.println("ELEMENT MATRICES DUMP");
        writer.println("===============================================");
        
        int beamCounter = 1;
        for (Beam beam : model.getBeamIterator()) {
            writer.println();
            writer.println("--- BEAM " + beamCounter + " (" + getBeamId(beam) + ") ---");
            dumpBeamMatrices(beam, writer);
            beamCounter++;
        }
    }
    
    /**
     * Dump matrices for a single beam element.
     */
    private void dumpBeamMatrices(Beam beam, PrintWriter writer) {
        // Basic beam properties
        writer.println("Type: " + beam.getClass().getSimpleName());
        writer.println("Length: " + format(beam.getL()));
        writer.println("Nodes: " + getNodeDisplayName(beam.getN1()) + " -> " + getNodeDisplayName(beam.getN2()));
        
        // Beam-specific properties
        if (beam instanceof EBBeam) {
            EBBeam ebBeam = (EBBeam) beam;
            writer.println("EI: " + format(ebBeam.getEI()));
            writer.println("EA: " + format(ebBeam.getEA()));
        } else if (beam instanceof Truss) {
            Truss truss = (Truss) beam;
            writer.println("EA: " + format(truss.getEA()));
        }
        
        // Location vector (DOF mapping)
        writer.println();
        writer.println("Location Vector:");
        Node n1 = beam.getN1();
        Node n2 = beam.getN2();
        writer.println("  [DOF mapping not available - protected access]");
        
        // Matrices
        writer.println();
        writer.println("Local Stiffness Matrix (Sl):");
        formatMatrix(beam.getSl(), writer);
        
        writer.println();
        writer.println("Global Stiffness Matrix (Sg):");
        formatMatrix(beam.getSg(), writer);
        
        writer.println();
        writer.println("Transformation Matrix (a):");
        formatMatrix(beam.getTransformationMatrix(), writer);
        
        writer.println();
        writer.println("Local Mass Matrix (Ml):");
        formatMatrix(beam.getMl(), writer);
        
        writer.println();
        writer.println("Global Mass Matrix (Mg):");
        formatMatrix(beam.getMg(), writer);
        
        writer.println();
        writer.println("Global Load Vector (Lg):");
        formatVector(beam.getLg(), writer);
        
        // Note: Inner hinge information not available due to protected access
        if (beam instanceof EBBeam) {
            writer.println();
            writer.println("Inner Hinges: [information not available - protected access]");
        }
    }
    
    /**
     * Format a matrix for text output.
     */
    private void formatMatrix(double[][] matrix, PrintWriter writer) {
        if (matrix == null) {
            writer.println("  [null]");
            return;
        }
        
        for (int i = 0; i < matrix.length; i++) {
            writer.print("  [");
            for (int j = 0; j < matrix[i].length; j++) {
                if (j > 0) writer.print(", ");
                writer.print(String.format("%12.6f", matrix[i][j]));
            }
            writer.println("]");
        }
    }
    
    /**
     * Format a vector for text output.
     */
    private void formatVector(double[] vector, PrintWriter writer) {
        if (vector == null) {
            writer.println("  [null]");
            return;
        }
        
        writer.print("  [");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) writer.print(", ");
            writer.print(String.format("%12.6f", vector[i]));
        }
        writer.println("]");
    }
}
