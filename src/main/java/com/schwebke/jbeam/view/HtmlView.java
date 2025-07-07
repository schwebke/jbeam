package com.schwebke.jbeam.view;

import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.*;

import java.io.*;
import java.util.*;

/** Alternative view for HTML result output */
public class HtmlView {

    Model model;
    IController controller;
    boolean showAllItems;

    public HtmlView(Model model, IController controller) {
        this(model, controller, false);
    }
    
    public HtmlView(Model model, IController controller, boolean showAllItems) {
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
                if (shouldIncludeNode(node) && (node.getCX() || node.getCZ() || node.getCR())) {
                    writer.println("<tr>");
                    writer.println("<td bgcolor=#00FFFF>" + getNodeDisplayName(node) + "</td>");
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
                if (shouldIncludeNode(node)) {
                    writer.println("<tr>");
                    writer.println("<td bgcolor=#00FFFF>" + getNodeDisplayName(node) + "</td>");
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
                    if (shouldIncludeBeam(beam)) {
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
                    if (shouldIncludeBeam(beam)) {
                        writer.println("<tr><td bgcolor=#00FFFF>" + getBeamDisplayName(beam) + "</td>");
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
                    if (shouldIncludeBeam(truss)) {
                        writer.println("<table cellpadding=5>");
                        writer.println("<tr><th bgcolor=#FFCCCC>Truss</th><th bgcolor=#CCCCFF>N</th></tr>");
                        break trussLoop;
                    }
                }
            }

            for (Beam element : model.getBeamIterator()) {
                if (element instanceof Truss) {
                    Truss truss = (Truss) element;
                    if (shouldIncludeBeam(truss)) {
                        writer.println("<tr><td bgcolor=#00FFFF>" + getBeamDisplayName(truss) + "</td>");
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
