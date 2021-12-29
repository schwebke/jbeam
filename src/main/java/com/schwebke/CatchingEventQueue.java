/*
 * Copyright (c) 2010, Kai Schwebke. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Kai Schwebke (kai@schwebke.com)
 * or visit www.schwebke.com if you need additional information or have any
 * questions.
 */

/*
 * $Id: CatchingEventQueue.java 200 2010-08-15 19:21:24Z kai $
 */

package com.schwebke;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 */
public class CatchingEventQueue extends EventQueue {

    @Override
    protected void dispatchEvent(AWTEvent e) {
        try {
            super.dispatchEvent(e);
        } catch (Throwable t) {
            t.printStackTrace();
            String msg = t.getMessage();
            if ( (msg == null) || (msg.length() == 0) ) {
                msg = "Fatal: "+t.getClass().toString();
            }
            JDialog d = new ExceptionDialog(null, "JBeam: Oops!", msg,
                    java.awt.Dialog.ModalityType.APPLICATION_MODAL,
                    t);
            d.setVisible(true);
        }
    }
    
    public static void installErrorDialogueHandler() {
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        queue.push(new CatchingEventQueue());
    }

}
