package com.schwebke.jbeam.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for Euler-Bernoulli beams with shear deformation in JSON format.
 * Represents beams with additional shear stiffness properties.
 */
public class EBSBeamDto extends BeamDto {
    
    @JsonProperty("material")
    private EBSMaterial material;
    
    @JsonProperty("distributedLoads")
    private EBBeamDto.DistributedLoads distributedLoads;
    
    @JsonProperty("internalHinges")
    private EBBeamDto.InternalHinges internalHinges;
    
    public EBSBeamDto() {
    }
    
    public EBSBeamDto(String id, List<String> nodeIds, double mass, EBSMaterial material) {
        super(id, nodeIds, mass);
        this.material = material;
        this.distributedLoads = new EBBeamDto.DistributedLoads();
        this.internalHinges = new EBBeamDto.InternalHinges();
    }
    
    
    public EBSMaterial getMaterial() {
        return material;
    }
    
    public void setMaterial(EBSMaterial material) {
        this.material = material;
    }
    
    public EBBeamDto.DistributedLoads getDistributedLoads() {
        return distributedLoads;
    }
    
    public void setDistributedLoads(EBBeamDto.DistributedLoads distributedLoads) {
        this.distributedLoads = distributedLoads;
    }
    
    public EBBeamDto.InternalHinges getInternalHinges() {
        return internalHinges;
    }
    
    public void setInternalHinges(EBBeamDto.InternalHinges internalHinges) {
        this.internalHinges = internalHinges;
    }
    
    /**
     * Material properties for Euler-Bernoulli beams with shear deformation.
     */
    public static class EBSMaterial {
        @JsonProperty("EA")
        private double EA;
        
        @JsonProperty("EI")
        private double EI;
        
        @JsonProperty("GA")
        private double GA;
        
        public EBSMaterial() {
        }
        
        public EBSMaterial(double EA, double EI, double GA) {
            this.EA = EA;
            this.EI = EI;
            this.GA = GA;
        }
        
        public double getEA() {
            return EA;
        }
        
        public void setEA(double EA) {
            this.EA = EA;
        }
        
        public double getEI() {
            return EI;
        }
        
        public void setEI(double EI) {
            this.EI = EI;
        }
        
        public double getGA() {
            return GA;
        }
        
        public void setGA(double GA) {
            this.GA = GA;
        }
    }
}