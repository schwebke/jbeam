package com.schwebke.jbeam.view;

import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.*;

import java.io.*;
import java.util.*;

/** Alternativer View zur HTML-Ergenisausgabe */
public class HtmlView {

    Model model;
    JBeam controller;

    public HtmlView(Model model, JBeam controller) {
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

            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>");
            writer.println(locale.getString("ResTitle"));
            writer.println("</title>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("");
            writer.println("<h1>" + locale.getString("ResTitle") + "</h1>");

            if (!model.getValidCalculation()) {
                model.calculate();
                if (!model.getValidCalculation()) {
                    return;
                }
            }


            writer.println("<br><br><h2>"
                    + locale.getString("ResSupportReactions") + "</h2>");
            writer.println("<table cellpadding=5>");
            writer.println("<tr><th bgcolor=#FFCCCC>"
                    + locale.getString("ResNode") + "</th><th bgcolor=#CCCCFF>Fx</th><th bgcolor=#CCCCFF>Fz</th><th bgcolor=#CCCCFF>M</th></tr>");
            for (Node node : model.getNodeIterator()) {
                if ((!node.getLabel().equals("")) && (node.getCX() || node.getCZ() || node.getCR())) {
                    writer.println("<tr>");
                    writer.println("<td bgcolor=#00FFFF>" + node.getLabel() + "</td>");
                    if (node.getCX()) {
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(node.getRFx()) + "</td>");
                    } else {
                        writer.println("<td bgcolor=#CCFFCC>-</td>");
                    }
                    if (node.getCZ()) {
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(node.getRFz()) + "</td>");
                    } else {
                        writer.println("<td bgcolor=#CCFFCC>-</td>");
                    }
                    if (node.getCR()) {
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(node.getRM()) + "</td>");
                    } else {
                        writer.println("<td bgcolor=#CCFFCC>-</td>");
                    }
                    writer.println("</tr>");
                }
            }
            writer.println("</table>");

            Iterable<Node> nodeIterable = model.getNodeIterator();

            if (nodeIterable.iterator().hasNext()) {
                writer.println("<br><br><h2>"
                        + locale.getString("ResNodalDisplacements") + "</h2>");
                writer.println("<table cellpadding=5>");
                writer.println("<tr><th bgcolor=#FFCCCC>"
                        + locale.getString("ResNode") + "</th><th bgcolor=#CCCCFF>dx</th><th bgcolor=#CCCCFF>dz</th><th bgcolor=#CCCCFF>dr</th></tr>");
            }

            for (Node node : nodeIterable) {
                if (!node.getLabel().equals("")) {
                    writer.println("<tr>");
                    writer.println("<td bgcolor=#00FFFF>" + node.getLabel() + "</td>");
                    writer.println("<td align=right bgcolor=#CCFFCC>" + format(node.getDX()) + "</td>");
                    writer.println("<td align=right bgcolor=#CCFFCC>" + format(node.getDZ()) + "</td>");
                    writer.println("<td align=right bgcolor=#CCFFCC>" + format(node.getDR()) + "</td>");
                    writer.println("</tr>");
                }
            }
            writer.println("</table>");

            beamLoop:
            for (Beam element : model.getBeamIterator()) {
                if (element instanceof EBBeam) {
                    EBBeam beam = (EBBeam) element;
                    if (!beam.getLabel().equals("")) {
                        writer.println();
                        writer.println("<br><br><h2>"
                                + locale.getString("ResStressResultants") + "</h2>");
                        writer.println("<table cellpadding=5>");
                        writer.println("<tr><th bgcolor=#FFCCCC>EB-beam</th>");
                        writer.println("<th bgcolor=#CCCCFF>Ni</th>");
                        writer.println("<th bgcolor=#CCCCFF>Vi</th>");
                        writer.println("<th bgcolor=#CCCCFF>Mi</th>");
                        writer.println("<th bgcolor=#CCCCFF>Nk</th>");
                        writer.println("<th bgcolor=#CCCCFF>Vk</th>");
                        writer.println("<th bgcolor=#CCCCFF>Mk</th></tr>");
                        break beamLoop;
                    }
                }
            }

            for (Beam element : model.getBeamIterator()) {
                if (element instanceof EBBeam) {
                    EBBeam beam = (EBBeam) element;
                    if (!beam.getLabel().equals("")) {
                        writer.println("<tr><td bgcolor=#00FFFF>" + beam.getLabel() + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(beam.N(0.)) + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(beam.V(0.)) + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(beam.M(0.)) + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(beam.N(1.)) + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(beam.V(1.)) + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(beam.M(1.)) + "</td></tr>");
                    }
                }
            }
            writer.println("</table>");

            trussLoop:
            for (Beam element : model.getBeamIterator()) {
                if (element instanceof Truss) {
                    Truss truss = (Truss) element;
                    if (!truss.getLabel().equals("")) {
                        writer.println("<table cellpadding=5>");
                        writer.println("<tr><th bgcolor=#FFCCCC>Truss</th><th bgcolor=#CCCCFF>N</th></tr>");
                        break trussLoop;
                    }
                }
            }

            for (Beam element : model.getBeamIterator()) {
                if (element instanceof Truss) {
                    Truss truss = (Truss) element;
                    if (!truss.getLabel().equals("")) {
                        writer.println("<tr><td bgcolor=#00FFFF>" + truss.getLabel() + "</td>");
                        writer.println("<td align=right bgcolor=#CCFFCC>" + format(truss.N(0.)) + "</td></tr>");
                    }
                }
            }
            writer.println("</table>");

            writer.println("</body>");
            writer.println("</html>");

        } catch (Exception e) {
            System.out.println("HtmlView error: " + e.getMessage());
        }


    }
}
