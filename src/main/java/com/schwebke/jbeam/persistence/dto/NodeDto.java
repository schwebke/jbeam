package com.schwebke.jbeam.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for nodes in JSON format.
 * Represents structural nodes with 2D coordinates, constraints, and loads.
 */
public class NodeDto {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("label")
    private String label = "";
    
    @JsonProperty("coordinates")
    private Coordinates2D coordinates;
    
    @JsonProperty("constraints")
    private Constraints2D constraints;
    
    @JsonProperty("loads")
    private NodalLoads2D loads;
    
    public NodeDto() {
    }
    
    public NodeDto(String id, Coordinates2D coordinates, Constraints2D constraints, NodalLoads2D loads) {
        this.id = id;
        this.coordinates = coordinates;
        this.constraints = constraints;
        this.loads = loads;
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
    
    public Coordinates2D getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(Coordinates2D coordinates) {
        this.coordinates = coordinates;
    }
    
    public Constraints2D getConstraints() {
        return constraints;
    }
    
    public void setConstraints(Constraints2D constraints) {
        this.constraints = constraints;
    }
    
    public NodalLoads2D getLoads() {
        return loads;
    }
    
    public void setLoads(NodalLoads2D loads) {
        this.loads = loads;
    }
    
    /**
     * 2D coordinates for plane frame analysis.
     */
    public static class Coordinates2D {
        @JsonProperty("x")
        private double x;
        
        @JsonProperty("z")
        private double z;
        
        public Coordinates2D() {
        }
        
        public Coordinates2D(double x, double z) {
            this.x = x;
            this.z = z;
        }
        
        public double getX() {
            return x;
        }
        
        public void setX(double x) {
            this.x = x;
        }
        
        public double getZ() {
            return z;
        }
        
        public void setZ(double z) {
            this.z = z;
        }
    }
    
    /**
     * 2D constraints for plane frame analysis.
     */
    public static class Constraints2D {
        @JsonProperty("x")
        private boolean x = false;
        
        @JsonProperty("z")
        private boolean z = false;
        
        @JsonProperty("r")
        private boolean r = false;
        
        public Constraints2D() {
        }
        
        public Constraints2D(boolean x, boolean z, boolean r) {
            this.x = x;
            this.z = z;
            this.r = r;
        }
        
        public boolean isX() {
            return x;
        }
        
        public void setX(boolean x) {
            this.x = x;
        }
        
        public boolean isZ() {
            return z;
        }
        
        public void setZ(boolean z) {
            this.z = z;
        }
        
        public boolean isR() {
            return r;
        }
        
        public void setR(boolean r) {
            this.r = r;
        }
    }
    
    /**
     * 2D nodal loads.
     */
    public static class NodalLoads2D {
        @JsonProperty("fx")
        private double fx = 0.0;
        
        @JsonProperty("fz")
        private double fz = 0.0;
        
        @JsonProperty("m")
        private double m = 0.0;
        
        public NodalLoads2D() {
        }
        
        public NodalLoads2D(double fx, double fz, double m) {
            this.fx = fx;
            this.fz = fz;
            this.m = m;
        }
        
        public double getFx() {
            return fx;
        }
        
        public void setFx(double fx) {
            this.fx = fx;
        }
        
        public double getFz() {
            return fz;
        }
        
        public void setFz(double fz) {
            this.fz = fz;
        }
        
        public double getM() {
            return m;
        }
        
        public void setM(double m) {
            this.m = m;
        }
    }
}