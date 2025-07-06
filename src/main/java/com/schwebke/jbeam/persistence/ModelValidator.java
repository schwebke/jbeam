package com.schwebke.jbeam.persistence;

import com.schwebke.jbeam.persistence.dto.*;
import java.util.*;

/**
 * Validator for JBeam models focusing on data integrity issues that would prevent loading.
 * Engineering concerns generate warnings only - users should be able to save/load incomplete models.
 */
public class ModelValidator {
    
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    
    /**
     * Validate a model for data integrity issues.
     * Only hard errors prevent loading - everything else is a warning.
     * 
     * @param modelDto the model to validate
     * @return validation results
     */
    public ValidationResult validate(ModelDto modelDto) {
        errors.clear();
        warnings.clear();
        
        if (modelDto == null) {
            errors.add("Model is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        validateBasicStructure(modelDto);
        validateNodeIntegrity(modelDto.getNodes());
        validateBeamIntegrity(modelDto.getBeams());
        validateReferences(modelDto.getBeams(), modelDto.getNodes());
        checkGeometricCoincidence(modelDto.getNodes());
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate basic model structure - only hard requirements.
     */
    private void validateBasicStructure(ModelDto modelDto) {
        // Allow empty models for work-in-progress
        if (modelDto.getNodes() == null) {
            errors.add("Nodes array is null");
        }
        if (modelDto.getBeams() == null) {
            errors.add("Beams array is null");
        }
        
        // Warn about empty models but don't prevent loading
        if (modelDto.getNodes() != null && modelDto.getNodes().isEmpty()) {
            warnings.add("Model contains no nodes");
        }
        if (modelDto.getBeams() != null && modelDto.getBeams().isEmpty()) {
            warnings.add("Model contains no beams");
        }
    }
    
    /**
     * Validate node data integrity - only check for data corruption.
     */
    private void validateNodeIntegrity(List<NodeDto> nodes) {
        if (nodes == null) return;
        
        Set<String> nodeIds = new HashSet<>();
        
        for (NodeDto node : nodes) {
            // Null/empty IDs would cause lookup failures - ERROR
            if (node.getId() == null || node.getId().trim().isEmpty()) {
                errors.add("Node has null or empty ID");
                continue;
            }
            
            // Duplicate IDs would cause lookup conflicts - ERROR
            if (nodeIds.contains(node.getId())) {
                errors.add("Duplicate node ID found: " + node.getId());
            }
            nodeIds.add(node.getId());
            
            // Check for data corruption in coordinates
            if (node.getCoordinates() != null) {
                double x = node.getCoordinates().getX();
                double z = node.getCoordinates().getZ();
                
                // NaN/Infinite would cause calculation failures - ERROR
                if (!Double.isFinite(x) || !Double.isFinite(z)) {
                    errors.add("Node " + node.getId() + " has invalid coordinates: (" + x + ", " + z + ")");
                }
            } else {
                errors.add("Node " + node.getId() + " has null coordinates");
            }
            
            // Check for data corruption in loads (but allow any finite values)
            if (node.getLoads() != null) {
                validateFiniteValues("Node " + node.getId() + " loads", 
                    node.getLoads().getFx(), node.getLoads().getFz(), node.getLoads().getM());
            }
        }
    }
    
    /**
     * Validate beam data integrity - only check for data corruption.
     */
    private void validateBeamIntegrity(List<BeamDto> beams) {
        if (beams == null) return;
        
        Set<String> beamIds = new HashSet<>();
        
        for (BeamDto beam : beams) {
            // Null/empty IDs would cause lookup failures - ERROR
            if (beam.getId() == null || beam.getId().trim().isEmpty()) {
                errors.add("Beam has null or empty ID");
                continue;
            }
            
            // Duplicate IDs would cause lookup conflicts - ERROR
            if (beamIds.contains(beam.getId())) {
                errors.add("Duplicate beam ID found: " + beam.getId());
            }
            beamIds.add(beam.getId());
            
            // Null or wrong number of node references would cause crashes - ERROR
            if (beam.getNodeIds() == null) {
                errors.add("Beam " + beam.getId() + " has null nodeIds");
                continue;
            }
            if (beam.getNodeIds().size() != 2) {
                errors.add("Beam " + beam.getId() + " must have exactly 2 node references, found: " + beam.getNodeIds().size());
                continue;
            }
            
            // Null node IDs would cause lookup failures - ERROR
            for (int i = 0; i < beam.getNodeIds().size(); i++) {
                if (beam.getNodeIds().get(i) == null || beam.getNodeIds().get(i).trim().isEmpty()) {
                    errors.add("Beam " + beam.getId() + " has null or empty node ID at position " + i);
                }
            }
            
            // Self-referencing beams might cause calculation issues - WARNING
            if (beam.getNodeIds().size() == 2 && beam.getNodeIds().get(0).equals(beam.getNodeIds().get(1))) {
                warnings.add("Beam " + beam.getId() + " references the same node twice");
            }
            
            // Check for data corruption in mass (but allow any finite value including 0)
            if (!Double.isFinite(beam.getMass())) {
                errors.add("Beam " + beam.getId() + " has invalid mass: " + beam.getMass());
            }
            
            // Check material properties for data corruption only
            validateBeamMaterialIntegrity(beam);
        }
    }
    
    /**
     * Validate beam material data integrity - only check for corruption, not engineering validity.
     */
    private void validateBeamMaterialIntegrity(BeamDto beam) {
        if (beam instanceof TrussBeamDto) {
            TrussBeamDto truss = (TrussBeamDto) beam;
            if (truss.getMaterial() != null) {
                double ea = truss.getMaterial().getEA();
                if (!Double.isFinite(ea)) {
                    errors.add("Truss " + beam.getId() + " has invalid EA: " + ea);
                } else if (ea <= 0) {
                    warnings.add("Truss " + beam.getId() + " has non-positive EA: " + ea);
                }
            }
        } else if (beam instanceof EBBeamDto) {
            EBBeamDto ebBeam = (EBBeamDto) beam;
            if (ebBeam.getMaterial() != null) {
                double ea = ebBeam.getMaterial().getEA();
                double ei = ebBeam.getMaterial().getEI();
                if (!Double.isFinite(ea)) {
                    errors.add("Beam " + beam.getId() + " has invalid EA: " + ea);
                } else if (ea <= 0) {
                    warnings.add("Beam " + beam.getId() + " has non-positive EA: " + ea);
                }
                if (!Double.isFinite(ei)) {
                    errors.add("Beam " + beam.getId() + " has invalid EI: " + ei);
                } else if (ei <= 0) {
                    warnings.add("Beam " + beam.getId() + " has non-positive EI: " + ei);
                }
            }
        } else if (beam instanceof EBSBeamDto) {
            EBSBeamDto ebsBeam = (EBSBeamDto) beam;
            if (ebsBeam.getMaterial() != null) {
                double ea = ebsBeam.getMaterial().getEA();
                double ei = ebsBeam.getMaterial().getEI();
                double ga = ebsBeam.getMaterial().getGA();
                if (!Double.isFinite(ea)) {
                    errors.add("Beam " + beam.getId() + " has invalid EA: " + ea);
                } else if (ea <= 0) {
                    warnings.add("Beam " + beam.getId() + " has non-positive EA: " + ea);
                }
                if (!Double.isFinite(ei)) {
                    errors.add("Beam " + beam.getId() + " has invalid EI: " + ei);
                } else if (ei <= 0) {
                    warnings.add("Beam " + beam.getId() + " has non-positive EI: " + ei);
                }
                if (!Double.isFinite(ga)) {
                    errors.add("Beam " + beam.getId() + " has invalid GA: " + ga);
                } else if (ga <= 0) {
                    warnings.add("Beam " + beam.getId() + " has non-positive GA: " + ga);
                }
            }
        }
    }
    
    /**
     * Validate node references in beams - critical for preventing crashes.
     */
    private void validateReferences(List<BeamDto> beams, List<NodeDto> nodes) {
        if (beams == null || nodes == null) return;
        
        Set<String> validNodeIds = new HashSet<>();
        for (NodeDto node : nodes) {
            if (node.getId() != null) {
                validNodeIds.add(node.getId());
            }
        }
        
        Set<String> referencedNodeIds = new HashSet<>();
        
        for (BeamDto beam : beams) {
            if (beam.getNodeIds() != null) {
                for (String nodeId : beam.getNodeIds()) {
                    if (nodeId != null && !validNodeIds.contains(nodeId)) {
                        // This would cause null pointer exceptions - ERROR
                        errors.add("Beam " + beam.getId() + " references non-existent node: " + nodeId);
                    }
                    if (nodeId != null) {
                        referencedNodeIds.add(nodeId);
                    }
                }
            }
        }
        
        // Isolated nodes are an engineering concern, not a loading issue - WARNING
        for (NodeDto node : nodes) {
            if (node.getId() != null && !referencedNodeIds.contains(node.getId())) {
                warnings.add("Node " + node.getId() + " is not connected to any beam");
            }
        }
    }
    
    /**
     * Check for geometric coincidence - useful warning but not an error.
     */
    private void checkGeometricCoincidence(List<NodeDto> nodes) {
        if (nodes == null) return;
        
        Map<String, NodeDto> coordinateMap = new HashMap<>();
        
        for (NodeDto node : nodes) {
            if (node.getCoordinates() != null && node.getId() != null) {
                double x = node.getCoordinates().getX();
                double z = node.getCoordinates().getZ();
                
                if (Double.isFinite(x) && Double.isFinite(z)) {
                    String coordKey = String.format("%.6f,%.6f", x, z);
                    if (coordinateMap.containsKey(coordKey)) {
                        warnings.add("Nodes " + coordinateMap.get(coordKey).getId() + 
                                   " and " + node.getId() + " have coincident coordinates");
                    } else {
                        coordinateMap.put(coordKey, node);
                    }
                }
            }
        }
    }
    
    /**
     * Helper method to validate finite numerical values.
     */
    private void validateFiniteValues(String context, double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                errors.add(context + " contains invalid numerical value: " + value);
            }
        }
    }
    
    /**
     * Validation result container.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        public String getFormattedMessage() {
            StringBuilder sb = new StringBuilder();
            
            if (!errors.isEmpty()) {
                sb.append("ERRORS (prevent loading):\n");
                for (String error : errors) {
                    sb.append("- ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("WARNINGS (check your model):\n");
                for (String warning : warnings) {
                    sb.append("- ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}