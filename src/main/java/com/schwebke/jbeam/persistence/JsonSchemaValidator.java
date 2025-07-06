package com.schwebke.jbeam.persistence;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.IOException;
import java.io.InputStream;

/**
 * JSON Schema validator for JBeam models.
 * Validates JSON files against the JBeam model schema.
 */
public class JsonSchemaValidator {
    
    private static final String SCHEMA_RESOURCE_PATH = "/jbeam-model-schema.json";
    private Schema schema;
    
    public JsonSchemaValidator() throws IOException {
        loadSchema();
    }
    
    /**
     * Load the JSON schema from resources.
     */
    private void loadSchema() throws IOException {
        try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMA_RESOURCE_PATH)) {
            if (schemaStream == null) {
                throw new IOException("Schema file not found: " + SCHEMA_RESOURCE_PATH);
            }
            
            JSONObject schemaJson = new JSONObject(new JSONTokener(schemaStream));
            schema = SchemaLoader.load(schemaJson);
        }
    }
    
    /**
     * Validate a JSON string against the JBeam model schema.
     * 
     * @param jsonString the JSON string to validate
     * @throws ValidationException if validation fails
     */
    public void validate(String jsonString) throws ValidationException {
        JSONObject jsonObject = new JSONObject(jsonString);
        schema.validate(jsonObject);
    }
    
    /**
     * Validate a JSON object against the JBeam model schema.
     * 
     * @param jsonObject the JSON object to validate
     * @throws ValidationException if validation fails
     */
    public void validate(JSONObject jsonObject) throws ValidationException {
        schema.validate(jsonObject);
    }
    
    /**
     * Check if a JSON string is valid without throwing exceptions.
     * 
     * @param jsonString the JSON string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String jsonString) {
        try {
            validate(jsonString);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
    
    /**
     * Check if a JSON object is valid without throwing exceptions.
     * 
     * @param jsonObject the JSON object to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(JSONObject jsonObject) {
        try {
            validate(jsonObject);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}