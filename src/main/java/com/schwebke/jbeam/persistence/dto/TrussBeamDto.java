package com.schwebke.jbeam.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for truss beams in JSON format.
 * Represents axial-only structural elements.
 */
public class TrussBeamDto extends BeamDto {
    
    @JsonProperty("material")
    private TrussMaterial material;
    
    public TrussBeamDto() {
    }
    
    public TrussBeamDto(String id, List<String> nodeIds, double mass, TrussMaterial material) {
        super(id, nodeIds, mass);
        this.material = material;
    }
    
    
    public TrussMaterial getMaterial() {
        return material;
    }
    
    public void setMaterial(TrussMaterial material) {
        this.material = material;
    }
    
    /**
     * Material properties for truss elements.
     */
    public static class TrussMaterial {
        @JsonProperty("EA")
        private double EA;
        
        public TrussMaterial() {
        }
        
        public TrussMaterial(double EA) {
            this.EA = EA;
        }
        
        public double getEA() {
            return EA;
        }
        
        public void setEA(double EA) {
            this.EA = EA;
        }
    }
}