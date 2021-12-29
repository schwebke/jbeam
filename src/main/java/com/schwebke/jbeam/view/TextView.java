package com.schwebke.jbeam.view;

import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.*;

import java.io.*;
import java.util.*;

/** Alternativer View zur ASCII-Text-Ergebnisausgabe */
public class TextView {

    Model model;
    JBeam controller;

    public TextView(Model model, JBeam controller) {
        this.model = model;
        this.controller = controller;
    }

    String format(double number) {
        return controller.getNumberFormat().format(number);
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
                if ((!node.getLabel().equals("")) && (node.getCX() || node.getCZ() || node.getCR())) {
                    writer.println("      " + locale.getString("ResNode")
                            + " '" + node.getLabel() + "'");
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
                if (!node.getLabel().equals("")) {
                    writer.println("      " + locale.getString("ResNode")
                            + " '" + node.getLabel() + "'");
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
                    if (!beam.getLabel().equals("")) {
                        writer.println("      EB-beam '" + beam.getLabel() + "'");
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
                    if (!truss.getLabel().equals("")) {
                        writer.println("      Truss '" + truss.getLabel() + "'");
                        writer.println("         N  = " + format(truss.N(0.)));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("TextView error: " + e.getMessage());
        }


    }
}
