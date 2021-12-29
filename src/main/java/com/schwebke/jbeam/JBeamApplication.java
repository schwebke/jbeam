package com.schwebke.jbeam;
/*
    JBeam - ein Stabwerksprogramm für die Java-Plattform

    Copyright (C) 1998, 1999   Kai Gerd Schwebke
    Copyright (C) 2000, 2001, 2002   Schwebke Software Development

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/
/**
 * Diese Klasse ist eine Java-Application, das heißt sie stellt
 * die Funktion <code>main</code> zur Verfügung und erzeugt
 * eine Instanz des JBeam-Hauptapplikations-Fensters.
 *
 * @author Kai Gerd Schwebke
 * @version 1.2.1
 *
 */
public class JBeamApplication implements Launcher
{
    static String args[];
    /**
     * Erstellen genau eines Hautptapplikationsfensters.
     */
    public static void main(String args[])
    {
	JBeamApplication.args = args;
	// erzeugen einer JBeamApplication und damit eines Launchers
	final JBeamApplication jbapp = new JBeamApplication();

	// erzeugen eines Hauptapplikations-Fensters
        java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new JBeam(jbapp);
                }
            });
    }

    /**
     * Callbackfunktion, wird beim Beenden des Hauptapplikations-Fensters
     * (JBeam) durch dieses aufgerufen.
     * Da hier nur ein Hauptapplikationsfenster erstellt wird, beendet
     * diese Funktion einfach die Anwendung, sobald dieses Fenster
     * geschlossen wird.
     */
    public void exit()
    {
	System.exit(0);
    }

    public String [] args()
    {
	return args;
    }
}

