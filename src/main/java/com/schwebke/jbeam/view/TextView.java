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

    public TextView(Model model, IController controller) {
        this(model, controller, false);
    }
    
    public TextView(Model model, IController controller, boolean showAllItems) {
        this.model = model;
        this.controller = controller;
        this.showAllItems = showAllItems;
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


    }
}
