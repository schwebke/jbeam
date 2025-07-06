package com.schwebke.jbeam.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for Euler-Bernoulli beams in JSON format.
 * Represents full beam elements with distributed loads and internal hinges.
 */
public class EBBeamDto extends BeamDto {
    
    @JsonProperty("material")
    private EBMaterial material;
    
    @JsonProperty("distributedLoads")
    private DistributedLoads distributedLoads;
    
    @JsonProperty("internalHinges")
    private InternalHinges internalHinges;
    
    public EBBeamDto() {
    }
    
    public EBBeamDto(String id, List<String> nodeIds, double mass, EBMaterial material) {
        super(id, nodeIds, mass);
        this.material = material;
        this.distributedLoads = new DistributedLoads();
        this.internalHinges = new InternalHinges();
    }
    
    
    public EBMaterial getMaterial() {
        return material;
    }
    
    public void setMaterial(EBMaterial material) {
        this.material = material;
    }
    
    public DistributedLoads getDistributedLoads() {
        return distributedLoads;
    }
    
    public void setDistributedLoads(DistributedLoads distributedLoads) {
        this.distributedLoads = distributedLoads;
    }
    
    public InternalHinges getInternalHinges() {
        return internalHinges;
    }
    
    public void setInternalHinges(InternalHinges internalHinges) {
        this.internalHinges = internalHinges;
    }
    
    /**
     * Material properties for Euler-Bernoulli beams.
     */
    public static class EBMaterial {
        @JsonProperty("EA")
        private double EA;
        
        @JsonProperty("EI")
        private double EI;
        
        public EBMaterial() {
        }
        
        public EBMaterial(double EA, double EI) {
            this.EA = EA;
            this.EI = EI;
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
    }
    
    /**
     * Distributed loads along beam element.
     */
    public static class DistributedLoads {
        @JsonProperty("vi")
        private double vi = 0.0;
        
        @JsonProperty("vk")
        private double vk = 0.0;
        
        @JsonProperty("ni")
        private double ni = 0.0;
        
        @JsonProperty("nk")
        private double nk = 0.0;
        
        public DistributedLoads() {
        }
        
        public DistributedLoads(double vi, double vk, double ni, double nk) {
            this.vi = vi;
            this.vk = vk;
            this.ni = ni;
            this.nk = nk;
        }
        
        public double getVi() {
            return vi;
        }
        
        public void setVi(double vi) {
            this.vi = vi;
        }
        
        public double getVk() {
            return vk;
        }
        
        public void setVk(double vk) {
            this.vk = vk;
        }
        
        public double getNi() {
            return ni;
        }
        
        public void setNi(double ni) {
            this.ni = ni;
        }
        
        public double getNk() {
            return nk;
        }
        
        public void setNk(double nk) {
            this.nk = nk;
        }
    }
    
    /**
     * Internal hinge conditions at beam ends.
     */
    public static class InternalHinges {
        @JsonProperty("ni")
        private boolean ni = false;
        
        @JsonProperty("vi")
        private boolean vi = false;
        
        @JsonProperty("mi")
        private boolean mi = false;
        
        @JsonProperty("nk")
        private boolean nk = false;
        
        @JsonProperty("vk")
        private boolean vk = false;
        
        @JsonProperty("mk")
        private boolean mk = false;
        
        public InternalHinges() {
        }
        
        public InternalHinges(boolean ni, boolean vi, boolean mi, boolean nk, boolean vk, boolean mk) {
            this.ni = ni;
            this.vi = vi;
            this.mi = mi;
            this.nk = nk;
            this.vk = vk;
            this.mk = mk;
        }
        
        public boolean isNi() {
            return ni;
        }
        
        public void setNi(boolean ni) {
            this.ni = ni;
        }
        
        public boolean isVi() {
            return vi;
        }
        
        public void setVi(boolean vi) {
            this.vi = vi;
        }
        
        public boolean isMi() {
            return mi;
        }
        
        public void setMi(boolean mi) {
            this.mi = mi;
        }
        
        public boolean isNk() {
            return nk;
        }
        
        public void setNk(boolean nk) {
            this.nk = nk;
        }
        
        public boolean isVk() {
            return vk;
        }
        
        public void setVk(boolean vk) {
            this.vk = vk;
        }
        
        public boolean isMk() {
            return mk;
        }
        
        public void setMk(boolean mk) {
            this.mk = mk;
        }
    }
}