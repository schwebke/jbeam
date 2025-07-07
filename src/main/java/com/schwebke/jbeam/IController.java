package com.schwebke.jbeam;

import java.text.NumberFormat;

/**
 * Interface for controllers that can provide services to view classes.
 * This allows both GUI and CLI controllers to be used with the same view classes.
 */
public interface IController {
    
    /**
     * Get the number format for displaying numerical results.
     * 
     * @return NumberFormat instance for formatting numbers
     */
    NumberFormat getNumberFormat();
}