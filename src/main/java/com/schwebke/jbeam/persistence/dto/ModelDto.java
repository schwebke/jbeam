package com.schwebke.jbeam.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for JBeam models in JSON format.
 * Represents the complete structural model.
 */
public class ModelDto {
    
    @JsonProperty("version")
    private String version = "1.0";
    
    @JsonProperty("modelType")
    private String modelType = "structural";
    
    @JsonProperty("nodes")
    private List<NodeDto> nodes;
    
    @JsonProperty("beams")
    private List<BeamDto> beams;
    
    public ModelDto() {
    }
    
    public ModelDto(List<NodeDto> nodes, List<BeamDto> beams) {
        this.nodes = nodes;
        this.beams = beams;
    }
    
    // Getters and setters
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getModelType() {
        return modelType;
    }
    
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
    
    public List<NodeDto> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<NodeDto> nodes) {
        this.nodes = nodes;
    }
    
    public List<BeamDto> getBeams() {
        return beams;
    }
    
    public void setBeams(List<BeamDto> beams) {
        this.beams = beams;
    }
}