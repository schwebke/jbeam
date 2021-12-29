package com.schwebke.jbeam;

/** 
 * Objekte, die dieses Interface implementieren,
 * können ein JBeam-Applikationsfenster starten.
 * Die Applikation meldet ihre Terminierung über den 
 * Methodenaufruf von <code>exit</code> zurück.
 */
public interface Launcher
{
    /**
     * Informiert den Launcher über das Beenden der Applikation
     */
    public void exit();
    /**
     * (Kommandozeilen-)Argumente für die Applikation
     */
    public String [] args();
}
