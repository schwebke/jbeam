package com.schwebke.jbeam.persistence;

import com.schwebke.jbeam.model.SelectableModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for different model persistence formats.
 * Implements Strategy pattern to support multiple file formats.
 */
public class PersistenceManager {
    
    private static final PersistenceManager INSTANCE = new PersistenceManager();
    private final Map<String, ModelPersistence> persistenceStrategies;
    
    private PersistenceManager() {
        persistenceStrategies = new HashMap<>();
        
        // Register default strategies
        JavaPersistence javaPersistence = new JavaPersistence();
        persistenceStrategies.put(javaPersistence.getFileExtension(), javaPersistence);
        
        // JSON persistence will be registered when Jackson is available
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            JsonPersistence jsonPersistence = new JsonPersistence();
            persistenceStrategies.put(jsonPersistence.getFileExtension(), jsonPersistence);
        } catch (ClassNotFoundException e) {
            // Jackson not available, JSON persistence disabled
            System.out.println("Jackson not available, JSON persistence disabled");
        }
    }
    
    public static PersistenceManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get persistence strategy for a file extension.
     * 
     * @param extension the file extension (without dot)
     * @return the persistence strategy, or null if not found
     */
    public ModelPersistence getPersistence(String extension) {
        return persistenceStrategies.get(extension.toLowerCase());
    }
    
    /**
     * Save a model using the appropriate persistence strategy.
     * 
     * @param model the model to save
     * @param outputStream the output stream
     * @param extension the file extension to determine format
     * @throws IOException if saving fails
     * @throws UnsupportedOperationException if format not supported
     */
    public void save(SelectableModel model, OutputStream outputStream, String extension) 
            throws IOException {
        ModelPersistence persistence = getPersistence(extension);
        if (persistence == null) {
            throw new UnsupportedOperationException("Unsupported file format: " + extension);
        }
        persistence.save(model, outputStream);
    }
    
    /**
     * Load a model using the appropriate persistence strategy.
     * 
     * @param inputStream the input stream
     * @param extension the file extension to determine format
     * @return the loaded model
     * @throws IOException if loading fails
     * @throws ClassNotFoundException if deserialization fails
     * @throws UnsupportedOperationException if format not supported
     */
    public SelectableModel load(InputStream inputStream, String extension) 
            throws IOException, ClassNotFoundException {
        ModelPersistence persistence = getPersistence(extension);
        if (persistence == null) {
            throw new UnsupportedOperationException("Unsupported file format: " + extension);
        }
        return persistence.load(inputStream);
    }
    
    /**
     * Get all supported file extensions.
     * 
     * @return array of supported extensions
     */
    public String[] getSupportedExtensions() {
        return persistenceStrategies.keySet().toArray(new String[0]);
    }
    
    /**
     * Get format description for an extension.
     * 
     * @param extension the file extension
     * @return format description, or null if not supported
     */
    public String getFormatDescription(String extension) {
        ModelPersistence persistence = getPersistence(extension);
        return persistence != null ? persistence.getFormatDescription() : null;
    }
}