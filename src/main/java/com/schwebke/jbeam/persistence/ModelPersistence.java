package com.schwebke.jbeam.persistence;

import com.schwebke.jbeam.model.SelectableModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for model persistence strategies.
 * Supports the Strategy pattern for different file formats.
 */
public interface ModelPersistence {
    
    /**
     * Save a model to an output stream.
     * 
     * @param model the model to save
     * @param outputStream the stream to write to
     * @throws IOException if writing fails
     */
    void save(SelectableModel model, OutputStream outputStream) throws IOException;
    
    /**
     * Load a model from an input stream.
     * 
     * @param inputStream the stream to read from
     * @return the loaded model
     * @throws IOException if reading fails
     * @throws ClassNotFoundException if deserialization fails
     */
    SelectableModel load(InputStream inputStream) throws IOException, ClassNotFoundException;
    
    /**
     * Get the file extension for this persistence format.
     * 
     * @return the file extension (without the dot)
     */
    String getFileExtension();
    
    /**
     * Get the file format description.
     * 
     * @return human-readable description
     */
    String getFormatDescription();
}