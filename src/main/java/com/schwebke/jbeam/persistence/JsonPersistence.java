package com.schwebke.jbeam.persistence;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.schwebke.jbeam.model.*;
import com.schwebke.jbeam.persistence.dto.*;
import org.everit.json.schema.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * JSON persistence implementation using Jackson ObjectMapper.
 * Converts between JBeam domain objects and JSON DTOs.
 */
public class JsonPersistence implements ModelPersistence {
    
    private final ObjectMapper objectMapper;
    private JsonSchemaValidator schemaValidator;
    private final boolean validateOnLoad;
    
    // Static field to store last validation result for status reporting
    public static ModelValidator.ValidationResult lastValidationResult;
    
    public JsonPersistence() {
        this(true); // Enable validation by default
    }
    
    public JsonPersistence(boolean validateOnLoad) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Configure Jackson to only use annotated properties
        this.objectMapper.setVisibility(this.objectMapper.getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        
        this.validateOnLoad = validateOnLoad;
        
        if (validateOnLoad) {
            try {
                this.schemaValidator = new JsonSchemaValidator();
            } catch (IOException e) {
                System.err.println("Warning: Could not load JSON schema, validation disabled: " + e.getMessage());
                this.schemaValidator = null;
            }
        }
    }
    
    @Override
    public void save(SelectableModel model, OutputStream outputStream) throws IOException {
        ModelDto modelDto = convertToDto(model);
        objectMapper.writeValue(outputStream, modelDto);
    }
    
    @Override
    public SelectableModel load(InputStream inputStream) throws IOException, ClassNotFoundException {
        // Read JSON as string first for optional validation
        String jsonContent = new String(inputStream.readAllBytes());
        
        // Validate if schema validator is available
        if (validateOnLoad && schemaValidator != null) {
            try {
                schemaValidator.validate(jsonContent);
            } catch (ValidationException e) {
                throw new IOException("JSON validation failed: " + e.getMessage(), e);
            }
        }
        
        // Parse JSON to DTO
        ModelDto modelDto = objectMapper.readValue(jsonContent, ModelDto.class);
        
        // Validate model integrity
        ModelValidator validator = new ModelValidator();
        ModelValidator.ValidationResult validationResult = validator.validate(modelDto);
        
        if (!validationResult.isValid()) {
            throw new IOException("Model validation failed:\n" + validationResult.getFormattedMessage());
        }
        
        SelectableModel model = convertFromDto(modelDto);
        
        // Store validation result as a static field for status reporting
        JsonPersistence.lastValidationResult = validationResult;
        
        return model;
    }
    
    @Override
    public String getFileExtension() {
        return "json";
    }
    
    @Override
    public String getFormatDescription() {
        return "JBeam JSON Files";
    }
    
    /**
     * Convert SelectableModel to ModelDto for JSON serialization.
     */
    private ModelDto convertToDto(SelectableModel model) {
        List<NodeDto> nodeDtos = new ArrayList<>();
        List<BeamDto> beamDtos = new ArrayList<>();
        
        // Convert nodes
        Map<Node, String> nodeIdMap = new HashMap<>();
        int nodeCounter = 1;
        
        for (Node node : model.getNodeIterator()) {
            String nodeId = "node-" + nodeCounter++;
            nodeIdMap.put(node, nodeId);
            
            NodeDto nodeDto = new NodeDto();
            nodeDto.setId(nodeId);
            nodeDto.setLabel(node.getLabel() != null ? node.getLabel() : "");
            nodeDto.setCoordinates(new NodeDto.Coordinates2D(node.getX(), node.getZ()));
            nodeDto.setConstraints(new NodeDto.Constraints2D(node.getCX(), node.getCZ(), node.getCR()));
            nodeDto.setLoads(new NodeDto.NodalLoads2D(node.getFx(), node.getFz(), node.getM()));
            
            nodeDtos.add(nodeDto);
        }
        
        // Convert beams
        int beamCounter = 1;
        for (Beam beam : model.getBeamIterator()) {
            String beamId = "beam-" + beamCounter++;
            List<String> nodeIds = Arrays.asList(
                nodeIdMap.get(beam.getN1()),
                nodeIdMap.get(beam.getN2())
            );
            
            BeamDto beamDto = convertBeamToDto(beam, beamId, nodeIds);
            if (beamDto != null) {
                beamDtos.add(beamDto);
            }
        }
        
        return new ModelDto(nodeDtos, beamDtos);
    }
    
    /**
     * Convert individual beam to appropriate DTO.
     */
    private BeamDto convertBeamToDto(Beam beam, String beamId, List<String> nodeIds) {
        if (beam instanceof EBSBeam) {
            EBSBeam ebsBeam = (EBSBeam) beam;
            EBSBeamDto.EBSMaterial material = new EBSBeamDto.EBSMaterial(
                ebsBeam.getEA(), ebsBeam.getEI(), ebsBeam.getGAs()
            );
            
            EBSBeamDto dto = new EBSBeamDto(beamId, nodeIds, beam.getM(), material);
            dto.setLabel(beam.getLabel() != null ? beam.getLabel() : "");
            dto.setDistributedLoads(new EBBeamDto.DistributedLoads(
                ebsBeam.getVi(), ebsBeam.getVk(), ebsBeam.getNi(), ebsBeam.getNk()
            ));
            dto.setInternalHinges(convertHinges(ebsBeam));
            return dto;
            
        } else if (beam instanceof EBBeam) {
            EBBeam ebBeam = (EBBeam) beam;
            EBBeamDto.EBMaterial material = new EBBeamDto.EBMaterial(
                ebBeam.getEA(), ebBeam.getEI()
            );
            
            EBBeamDto dto = new EBBeamDto(beamId, nodeIds, beam.getM(), material);
            dto.setLabel(beam.getLabel() != null ? beam.getLabel() : "");
            dto.setDistributedLoads(new EBBeamDto.DistributedLoads(
                ebBeam.getVi(), ebBeam.getVk(), ebBeam.getNi(), ebBeam.getNk()
            ));
            dto.setInternalHinges(convertHinges(ebBeam));
            return dto;
            
        } else if (beam instanceof Truss) {
            Truss truss = (Truss) beam;
            TrussBeamDto.TrussMaterial material = new TrussBeamDto.TrussMaterial(truss.getEA());
            
            TrussBeamDto dto = new TrussBeamDto(beamId, nodeIds, beam.getM(), material);
            dto.setLabel(beam.getLabel() != null ? beam.getLabel() : "");
            return dto;
        }
        
        return null;
    }
    
    /**
     * Convert beam hinge conditions to internal hinges DTO.
     */
    private EBBeamDto.InternalHinges convertHinges(EBBeam beam) {
        return new EBBeamDto.InternalHinges(
            beam.getHinge(0), beam.getHinge(1), beam.getHinge(2), // ni, vi, mi
            beam.getHinge(3), beam.getHinge(4), beam.getHinge(5)  // nk, vk, mk
        );
    }
    
    /**
     * Convert ModelDto to SelectableModel for loading.
     */
    private SelectableModel convertFromDto(ModelDto modelDto) {
        SelectableModel model = new SelectableModel();
        
        // Convert nodes
        Map<String, Node> nodeMap = new HashMap<>();
        for (NodeDto nodeDto : modelDto.getNodes()) {
            Node node = new Node(
                nodeDto.getCoordinates().getX(),
                nodeDto.getCoordinates().getZ()
            );
            
            // Set constraints
            node.setCX(nodeDto.getConstraints().isX());
            node.setCZ(nodeDto.getConstraints().isZ());
            node.setCR(nodeDto.getConstraints().isR());
            
            // Set loads
            node.setFx(nodeDto.getLoads().getFx());
            node.setFz(nodeDto.getLoads().getFz());
            node.setM(nodeDto.getLoads().getM());
            
            // Set label
            if (nodeDto.getLabel() != null && !nodeDto.getLabel().isEmpty()) {
                node.setLabel(nodeDto.getLabel());
            }
            
            nodeMap.put(nodeDto.getId(), node);
            model.addNode(node);
        }
        
        // Convert beams
        for (BeamDto beamDto : modelDto.getBeams()) {
            if (beamDto.getNodeIds().size() != 2) {
                continue; // Skip invalid beams
            }
            
            Node n1 = nodeMap.get(beamDto.getNodeIds().get(0));
            Node n2 = nodeMap.get(beamDto.getNodeIds().get(1));
            
            if (n1 == null || n2 == null) {
                continue; // Skip beams with invalid node references
            }
            
            Beam beam = convertBeamFromDto(beamDto, n1, n2);
            if (beam != null) {
                if (beamDto.getLabel() != null && !beamDto.getLabel().isEmpty()) {
                    beam.setLabel(beamDto.getLabel());
                }
                model.addBeam(beam);
            }
        }
        
        return model;
    }
    
    /**
     * Convert BeamDto to appropriate Beam instance.
     */
    private Beam convertBeamFromDto(BeamDto beamDto, Node n1, Node n2) {
        if (beamDto instanceof EBSBeamDto) {
            EBSBeamDto dto = (EBSBeamDto) beamDto;
            EBSBeam beam = new EBSBeam(n1, n2, 
                dto.getMaterial().getEI(),
                dto.getMaterial().getEA(),
                dto.getMaterial().getGA(),
                dto.getMass()
            );
            
            if (dto.getDistributedLoads() != null) {
                beam.setVi(dto.getDistributedLoads().getVi());
                beam.setVk(dto.getDistributedLoads().getVk());
                beam.setNi(dto.getDistributedLoads().getNi());
                beam.setNk(dto.getDistributedLoads().getNk());
            }
            
            if (dto.getInternalHinges() != null) {
                setHingesFromDto(beam, dto.getInternalHinges());
            }
            
            return beam;
            
        } else if (beamDto instanceof EBBeamDto) {
            EBBeamDto dto = (EBBeamDto) beamDto;
            EBBeam beam = new EBBeam(n1, n2, 
                dto.getMaterial().getEI(),
                dto.getMaterial().getEA(),
                dto.getMass()
            );
            
            if (dto.getDistributedLoads() != null) {
                beam.setVi(dto.getDistributedLoads().getVi());
                beam.setVk(dto.getDistributedLoads().getVk());
                beam.setNi(dto.getDistributedLoads().getNi());
                beam.setNk(dto.getDistributedLoads().getNk());
            }
            
            if (dto.getInternalHinges() != null) {
                setHingesFromDto(beam, dto.getInternalHinges());
            }
            
            return beam;
            
        } else if (beamDto instanceof TrussBeamDto) {
            TrussBeamDto dto = (TrussBeamDto) beamDto;
            return new Truss(n1, n2, dto.getMaterial().getEA(), dto.getMass());
        }
        
        return null;
    }
    
    /**
     * Set internal hinges from DTO using individual setHinge calls.
     */
    private void setHingesFromDto(EBBeam beam, EBBeamDto.InternalHinges hinges) {
        beam.setHinge(0, hinges.isNi()); // hNi
        beam.setHinge(1, hinges.isVi()); // hVi
        beam.setHinge(2, hinges.isMi()); // hMi
        beam.setHinge(3, hinges.isNk()); // hNk
        beam.setHinge(4, hinges.isVk()); // hVk
        beam.setHinge(5, hinges.isMk()); // hMk
    }
}