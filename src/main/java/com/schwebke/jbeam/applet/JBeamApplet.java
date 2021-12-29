package com.schwebke.jbeam.applet;

import javax.swing.*;
import com.schwebke.jbeam.*;

public class JBeamApplet
        extends JApplet {

    JBeam jBeam;

    @Override
    public void init() {
        if (getParameter("baseUrl") != null) {
            baseUrl = getParameter("baseUrl");
        }
        jBeam = new JBeam(this);
    }
    public static String baseUrl = "./";
}
