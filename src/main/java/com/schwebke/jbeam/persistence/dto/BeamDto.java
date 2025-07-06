package com.schwebke.jbeam.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/**
 * Base Data Transfer Object for beams in JSON format.
 * Uses Jackson polymorphic type handling for different beam types.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TrussBeamDto.class, name = "truss"),
    @JsonSubTypes.Type(value = EBBeamDto.class, name = "ebbeam"),
    @JsonSubTypes.Type(value = EBSBeamDto.class, name = "ebsbeam")
})
public abstract class BeamDto {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("label")
    private String label = "";
    
    @JsonProperty("nodeIds")
    private List<String> nodeIds;
    
    @JsonProperty("mass")
    private double mass = 0.0;
    
    public BeamDto() {
    }
    
    public BeamDto(String id, List<String> nodeIds, double mass) {
        this.id = id;
        this.nodeIds = nodeIds;
        this.mass = mass;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public List<String> getNodeIds() {
        return nodeIds;
    }
    
    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }
    
    public double getMass() {
        return mass;
    }
    
    public void setMass(double mass) {
        this.mass = mass;
    }
    
}