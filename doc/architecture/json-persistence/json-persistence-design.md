# JSON Persistence Design for JBeam

## Overview

This document outlines the architectural design for adding JSON import/export capabilities to JBeam as an alternative to the existing Java serialization persistence mechanism.

## Current State Analysis

### Existing Persistence Implementation

The current persistence implementation uses Java's native serialization:

- **Location**: `JBeam.java` lines 668-759
- **Format**: Binary `.jbm` files using `ObjectOutputStream`/`ObjectInputStream`
- **Scope**: Complete `SelectableModel` serialization including UI state
- **Pattern**: Direct object serialization with `serialVersionUID` versioning

### Key Serializable Classes

1. **Model Hierarchy**:
   - `Model.java` - Core structural analysis model
   - `SelectableModel.java` - Extends Model with UI selection state
   - `Node.java` - Structural nodes with coordinates and constraints
   - `Beam.java` - Abstract base class for structural elements

2. **Concrete Beam Types**:
   - `Truss.java` - Simple truss elements
   - `EBBeam.java` - Euler-Bernoulli beam elements
   - `EBSBeam.java` - Euler-Bernoulli beam with shear deformation

3. **Mathematical Support**:
   - `MVector.java` - Mathematical vector operations

## Design Challenges

### 1. Circular References
- Nodes reference beams through connectivity
- Beams reference nodes through their endpoints
- Java serialization handles this automatically, JSON requires explicit handling

### 2. Polymorphic Types
- Abstract `Beam` class with multiple concrete implementations
- Need type discrimination in JSON format
- Runtime type resolution during deserialization

### 3. Complex Data Structures
- Mathematical vectors and matrices
- Material property objects
- Constraint and load definitions

### 4. UI State Separation
- `SelectableModel` contains both structural and UI data
- JSON format should focus on structural data only
- UI state should be reconstructed on load

## Proposed Architecture

### 1. Strategy Pattern Implementation

```java
public interface ModelPersistence {
    void save(SelectableModel model, String filePath) throws IOException;
    SelectableModel load(String filePath) throws IOException, ClassNotFoundException;
    String[] getSupportedExtensions();
    String getFormatDescription();
}

public class PersistenceManager {
    private Map<String, ModelPersistence> persistenceStrategies = new HashMap<>();
    
    public void registerPersistence(ModelPersistence persistence) {
        for (String ext : persistence.getSupportedExtensions()) {
            persistenceStrategies.put(ext.toLowerCase(), persistence);
        }
    }
    
    public ModelPersistence getPersistence(String filePath) {
        String extension = getFileExtension(filePath);
        return persistenceStrategies.get(extension.toLowerCase());
    }
}
```

### 2. Concrete Persistence Implementations

#### Java Serialization (Existing Logic)
```java
public class JavaSerializationPersistence implements ModelPersistence {
    @Override
    public void save(SelectableModel model, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(model);
        }
    }
    
    @Override
    public SelectableModel load(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (SelectableModel) ois.readObject();
        }
    }
    
    @Override
    public String[] getSupportedExtensions() { return new String[]{"jbm"}; }
    
    @Override
    public String getFormatDescription() { return "JBeam Data Files"; }
}
```

#### JSON Persistence (New Implementation)
```java
public class JsonPersistence implements ModelPersistence {
    private final ObjectMapper objectMapper;
    
    public JsonPersistence() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Override
    public void save(SelectableModel model, String filePath) throws IOException {
        ModelDTO dto = convertToDTO(model);
        objectMapper.writeValue(new File(filePath), dto);
    }
    
    @Override
    public SelectableModel load(String filePath) throws IOException {
        ModelDTO dto = objectMapper.readValue(new File(filePath), ModelDTO.class);
        return convertFromDTO(dto);
    }
    
    @Override
    public String[] getSupportedExtensions() { return new String[]{"json"}; }
    
    @Override
    public String getFormatDescription() { return "JBeam JSON Files"; }
}
```

### 3. JSON Schema Design

#### Core Structure
```json
{
  "version": "1.0",
  "modelType": "structural",
  "nodes": [
    {
      "id": "node-1",
      "label": "Support Node",
      "coordinates": {
        "x": 0.0,
        "z": 0.0
      },
      "constraints": {
        "x": true,
        "z": true,
        "r": false
      },
      "loads": {
        "fx": 0.0,
        "fz": -1000.0,
        "m": 0.0
      }
    }
  ],
  "beams": [
    {
      "id": "beam-1",
      "type": "truss",
      "label": "Main Truss",
      "nodeIds": ["node-1", "node-2"],
      "material": {
        "EA": 200000.0
      },
      "mass": 100.0
    },
    {
      "id": "beam-2", 
      "type": "ebbeam",
      "label": "Main Beam",
      "nodeIds": ["node-2", "node-3"],
      "material": {
        "EA": 200000.0,
        "EI": 50000.0
      },
      "mass": 150.0,
      "distributedLoads": {
        "vi": -500.0,
        "vk": -500.0,
        "ni": 0.0,
        "nk": 0.0
      },
      "internalHinges": {
        "ni": false,
        "vi": false,
        "mi": false,
        "nk": false,
        "vk": false,
        "mk": false
      }
    }
  ]
}
```

#### Key Design Decisions

1. **ID-Based References**: 
   - Nodes and beams use string IDs for identification
   - References between objects use ID arrays instead of object references
   - Eliminates circular dependency issues

2. **Type Discrimination**: 
   - `"type"` field distinguishes beam implementations
   - Maps to concrete classes: `"truss"` → `Truss.java`, `"ebbeam"` → `EBBeam.java`

3. **Flat Structure**: 
   - No nested object references
   - Clear separation between structural and relational data

4. **Minimal Schema**: 
   - Only essential structural data
   - No UI state or calculated results
   - Focused on model definition, not analysis results

#### JSON Schema Specification

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "JBeam Model Schema",
  "description": "JSON Schema for JBeam 2D structural analysis models",
  "type": "object",
  "properties": {
    "version": {
      "type": "string",
      "description": "Schema version for compatibility",
      "enum": ["1.0"]
    },
    "modelType": {
      "type": "string",
      "description": "Type of structural model",
      "enum": ["structural"]
    },
    "nodes": {
      "type": "array",
      "description": "Array of structural nodes",
      "items": {
        "$ref": "#/definitions/Node"
      }
    },
    "beams": {
      "type": "array",
      "description": "Array of structural beams",
      "items": {
        "$ref": "#/definitions/Beam"
      }
    }
  },
  "required": ["version", "modelType", "nodes", "beams"],
  "definitions": {
    "Node": {
      "type": "object",
      "properties": {
        "id": {
          "type": "string",
          "description": "Unique identifier for the node"
        },
        "label": {
          "type": "string",
          "description": "Human-readable label for the node",
          "default": ""
        },
        "coordinates": {
          "$ref": "#/definitions/Coordinates2D"
        },
        "constraints": {
          "$ref": "#/definitions/Constraints2D"
        },
        "loads": {
          "$ref": "#/definitions/NodalLoads2D"
        }
      },
      "required": ["id", "coordinates", "constraints", "loads"],
      "additionalProperties": false
    },
    "Coordinates2D": {
      "type": "object",
      "description": "2D coordinates for plane frame analysis",
      "properties": {
        "x": {
          "type": "number",
          "description": "X coordinate"
        },
        "z": {
          "type": "number",
          "description": "Z coordinate (vertical in 2D plane)"
        }
      },
      "required": ["x", "z"],
      "additionalProperties": false
    },
    "Constraints2D": {
      "type": "object",
      "description": "2D constraints for plane frame analysis",
      "properties": {
        "x": {
          "type": "boolean",
          "description": "X translation constraint (cX)",
          "default": false
        },
        "z": {
          "type": "boolean",
          "description": "Z translation constraint (cZ)",
          "default": false
        },
        "r": {
          "type": "boolean",
          "description": "Rotation constraint (cR)",
          "default": false
        }
      },
      "required": ["x", "z", "r"],
      "additionalProperties": false
    },
    "NodalLoads2D": {
      "type": "object",
      "description": "2D nodal loads",
      "properties": {
        "fx": {
          "type": "number",
          "description": "Force in X direction",
          "default": 0.0
        },
        "fz": {
          "type": "number",
          "description": "Force in Z direction",
          "default": 0.0
        },
        "m": {
          "type": "number",
          "description": "Moment about Y axis",
          "default": 0.0
        }
      },
      "required": ["fx", "fz", "m"],
      "additionalProperties": false
    },
    "Beam": {
      "type": "object",
      "discriminator": {
        "propertyName": "type"
      },
      "oneOf": [
        {"$ref": "#/definitions/TrussBeam"},
        {"$ref": "#/definitions/EBBeam"},
        {"$ref": "#/definitions/EBSBeam"}
      ]
    },
    "TrussBeam": {
      "type": "object",
      "description": "Truss element (axial forces only)",
      "properties": {
        "id": {
          "type": "string",
          "description": "Unique identifier for the beam"
        },
        "type": {
          "type": "string",
          "enum": ["truss"]
        },
        "label": {
          "type": "string",
          "description": "Human-readable label for the beam",
          "default": ""
        },
        "nodeIds": {
          "type": "array",
          "description": "Array of node IDs [startNode, endNode]",
          "items": {
            "type": "string"
          },
          "minItems": 2,
          "maxItems": 2
        },
        "material": {
          "$ref": "#/definitions/TrussMaterial"
        },
        "mass": {
          "type": "number",
          "description": "Mass per unit length",
          "default": 0.0
        }
      },
      "required": ["id", "type", "nodeIds", "material"],
      "additionalProperties": false
    },
    "EBBeam": {
      "type": "object",
      "description": "Euler-Bernoulli beam element",
      "properties": {
        "id": {
          "type": "string",
          "description": "Unique identifier for the beam"
        },
        "type": {
          "type": "string",
          "enum": ["ebbeam"]
        },
        "label": {
          "type": "string",
          "description": "Human-readable label for the beam",
          "default": ""
        },
        "nodeIds": {
          "type": "array",
          "description": "Array of node IDs [startNode, endNode]",
          "items": {
            "type": "string"
          },
          "minItems": 2,
          "maxItems": 2
        },
        "material": {
          "$ref": "#/definitions/EBMaterial"
        },
        "mass": {
          "type": "number",
          "description": "Mass per unit length",
          "default": 0.0
        },
        "distributedLoads": {
          "$ref": "#/definitions/DistributedLoads"
        },
        "internalHinges": {
          "$ref": "#/definitions/InternalHinges"
        }
      },
      "required": ["id", "type", "nodeIds", "material"],
      "additionalProperties": false
    },
    "EBSBeam": {
      "type": "object",
      "description": "Euler-Bernoulli beam with shear deformation",
      "properties": {
        "id": {
          "type": "string",
          "description": "Unique identifier for the beam"
        },
        "type": {
          "type": "string",
          "enum": ["ebsbeam"]
        },
        "label": {
          "type": "string",
          "description": "Human-readable label for the beam",
          "default": ""
        },
        "nodeIds": {
          "type": "array",
          "description": "Array of node IDs [startNode, endNode]",
          "items": {
            "type": "string"
          },
          "minItems": 2,
          "maxItems": 2
        },
        "material": {
          "$ref": "#/definitions/EBSMaterial"
        },
        "mass": {
          "type": "number",
          "description": "Mass per unit length",
          "default": 0.0
        },
        "distributedLoads": {
          "$ref": "#/definitions/DistributedLoads"
        },
        "internalHinges": {
          "$ref": "#/definitions/InternalHinges"
        }
      },
      "required": ["id", "type", "nodeIds", "material"],
      "additionalProperties": false
    },
    "TrussMaterial": {
      "type": "object",
      "description": "Material properties for truss elements",
      "properties": {
        "EA": {
          "type": "number",
          "description": "Axial stiffness (E * A)",
          "minimum": 0
        }
      },
      "required": ["EA"],
      "additionalProperties": false
    },
    "EBMaterial": {
      "type": "object",
      "description": "Material properties for Euler-Bernoulli beams",
      "properties": {
        "EA": {
          "type": "number",
          "description": "Axial stiffness (E * A)",
          "minimum": 0
        },
        "EI": {
          "type": "number",
          "description": "Flexural stiffness (E * I)",
          "minimum": 0
        }
      },
      "required": ["EA", "EI"],
      "additionalProperties": false
    },
    "EBSMaterial": {
      "type": "object",
      "description": "Material properties for Euler-Bernoulli beams with shear",
      "properties": {
        "EA": {
          "type": "number",
          "description": "Axial stiffness (E * A)",
          "minimum": 0
        },
        "EI": {
          "type": "number",
          "description": "Flexural stiffness (E * I)",
          "minimum": 0
        },
        "GA": {
          "type": "number",
          "description": "Shear stiffness (G * A)",
          "minimum": 0
        }
      },
      "required": ["EA", "EI", "GA"],
      "additionalProperties": false
    },
    "DistributedLoads": {
      "type": "object",
      "description": "Distributed loads along beam element",
      "properties": {
        "vi": {
          "type": "number",
          "description": "Distributed load at start node (perpendicular to beam)",
          "default": 0.0
        },
        "vk": {
          "type": "number",
          "description": "Distributed load at end node (perpendicular to beam)",
          "default": 0.0
        },
        "ni": {
          "type": "number",
          "description": "Distributed load at start node (along beam axis)",
          "default": 0.0
        },
        "nk": {
          "type": "number",
          "description": "Distributed load at end node (along beam axis)",
          "default": 0.0
        }
      },
      "required": ["vi", "vk", "ni", "nk"],
      "additionalProperties": false
    },
    "InternalHinges": {
      "type": "object",
      "description": "Internal hinge conditions at beam ends",
      "properties": {
        "ni": {
          "type": "boolean",
          "description": "Normal force hinge at start node",
          "default": false
        },
        "vi": {
          "type": "boolean",
          "description": "Shear force hinge at start node",
          "default": false
        },
        "mi": {
          "type": "boolean",
          "description": "Moment hinge at start node",
          "default": false
        },
        "nk": {
          "type": "boolean",
          "description": "Normal force hinge at end node",
          "default": false
        },
        "vk": {
          "type": "boolean",
          "description": "Shear force hinge at end node",
          "default": false
        },
        "mk": {
          "type": "boolean",
          "description": "Moment hinge at end node",
          "default": false
        }
      },
      "required": ["ni", "vi", "mi", "nk", "vk", "mk"],
      "additionalProperties": false
    }
  }
}
```

### 4. Data Transfer Objects (DTOs)

```java
public class ModelDTO {
    public String version = "1.0";
    public String modelType = "structural";
    public List<NodeDTO> nodes = new ArrayList<>();
    public List<BeamDTO> beams = new ArrayList<>();
}

public class NodeDTO {
    public String id;
    public String label = "";
    public Coordinates2D coordinates;
    public Constraints2D constraints;
    public NodalLoads2D loads;
}

public class Coordinates2D {
    public double x;
    public double z;
}

public class Constraints2D {
    public boolean x = false;
    public boolean z = false;
    public boolean r = false;
}

public class NodalLoads2D {
    public double fx = 0.0;
    public double fz = 0.0;
    public double m = 0.0;
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TrussDTO.class, name = "truss"),
    @JsonSubTypes.Type(value = EBBeamDTO.class, name = "ebbeam"),
    @JsonSubTypes.Type(value = EBSBeamDTO.class, name = "ebsbeam")
})
public abstract class BeamDTO {
    public String id;
    public String label = "";
    public String[] nodeIds;
    public double mass = 0.0;
}

public class TrussDTO extends BeamDTO {
    public TrussMaterial material;
}

public class EBBeamDTO extends BeamDTO {
    public EBMaterial material;
    public DistributedLoads distributedLoads;
    public InternalHinges internalHinges;
}

public class EBSBeamDTO extends BeamDTO {
    public EBSMaterial material;
    public DistributedLoads distributedLoads;
    public InternalHinges internalHinges;
}

public class TrussMaterial {
    public double EA;
}

public class EBMaterial {
    public double EA;
    public double EI;
}

public class EBSMaterial {
    public double EA;
    public double EI;
    public double GA;
}

public class DistributedLoads {
    public double vi = 0.0;
    public double vk = 0.0;
    public double ni = 0.0;
    public double nk = 0.0;
}

public class InternalHinges {
    public boolean ni = false;
    public boolean vi = false;
    public boolean mi = false;
    public boolean nk = false;
    public boolean vk = false;
    public boolean mk = false;
}
```

### 5. JSON Library Selection

**Recommended: Jackson ObjectMapper**

**Advantages**:
- Excellent polymorphic type support via `@JsonTypeInfo` and `@JsonSubTypes`
- Mature ecosystem with extensive documentation
- Built-in support for complex object mapping
- Custom serializer/deserializer support
- Good performance characteristics

**Maven Dependency**:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

**Configuration**:
```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

### 6. UI Integration

#### Modified File Operations in JBeam.java

```java
public class JBeam {
    private PersistenceManager persistenceManager;
    
    public JBeam() {
        // Initialize persistence manager
        persistenceManager = new PersistenceManager();
        persistenceManager.registerPersistence(new JavaSerializationPersistence());
        persistenceManager.registerPersistence(new JsonPersistence());
    }
    
    // Modified save method
    if (cmd.equals("save")) {
        JFileChooser chooser = new JFileChooser();
        
        // Add multiple file filters
        chooser.addChoosableFileFilter(new ExampleFileFilter("jbm", "JBeam Data Files (Legacy)"));
        chooser.addChoosableFileFilter(new ExampleFileFilter("json", "JBeam JSON Files"));
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            ModelPersistence persistence = persistenceManager.getPersistence(filePath);
            
            if (persistence != null) {
                persistence.save(model, filePath);
            } else {
                showError("Unsupported file format");
            }
        }
    }
    
    // Modified load method  
    if (cmd.equals("open")) {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new ExampleFileFilter("jbm", "JBeam Data Files"));
        chooser.addChoosableFileFilter(new ExampleFileFilter("json", "JBeam JSON Files"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            ModelPersistence persistence = persistenceManager.getPersistence(filePath);
            
            if (persistence != null) {
                model = persistence.load(filePath);
                view.setModel(model);
            }
        }
    }
}
```

## Implementation Benefits

### Architecture Benefits

1. **Clean Separation**: Strategy pattern isolates format-specific logic
2. **Extensible**: Easy to add more formats (XML, CSV, etc.)
3. **Backward Compatible**: Existing `.jbm` files continue working unchanged
4. **Testable**: Each persistence strategy can be unit tested independently
5. **Maintainable**: Clear boundaries between persistence concerns

### JSON Format Benefits

1. **Human Readable**: Engineers can view/edit structural models in text editors
2. **Version Control Friendly**: Git diffs show meaningful changes to model structure
3. **Interoperable**: Other tools can consume/produce JBeam JSON files
4. **Portable**: Cross-platform without Java serialization dependencies
5. **Debuggable**: Easy to inspect and validate model data

### User Benefits

1. **Flexibility**: Choose appropriate format for use case
2. **Collaboration**: JSON files easier to share and review
3. **Integration**: JSON format enables tool ecosystem integration
4. **Future-Proof**: JSON format more stable across application versions

## Implementation Phases

### Phase 1: Core Architecture
- Implement `ModelPersistence` interface
- Create `PersistenceManager` class
- Wrap existing Java serialization logic

### Phase 2: JSON Foundation
- Add Jackson dependency to `pom.xml`
- Create basic `JsonPersistence` implementation
- Implement core DTO classes

### Phase 3: Complete JSON Mapping
- Implement all DTO classes for nodes and beams
- Create bidirectional mapping logic
- Handle polymorphic beam types

### Phase 4: UI Integration
- Modify `JBeam.java` file operations
- Update file chooser with multiple formats
- Add format detection logic

### Phase 5: Testing and Validation
- Create comprehensive test suite
- Validate round-trip serialization
- Test backward compatibility
- Performance benchmarking

## Risk Mitigation

### Data Integrity
- Implement comprehensive validation during JSON deserialization
- Version compatibility checks
- Graceful handling of malformed JSON

### Performance Considerations
- JSON parsing overhead compared to binary serialization
- Memory usage for large models
- Consider streaming for very large datasets

### Compatibility
- Maintain existing `.jbm` format support indefinitely
- Clear migration path for existing users
- Version handling for future JSON schema evolution

## Conclusion

This design provides a robust, extensible foundation for supporting multiple persistence formats in JBeam. The strategy pattern ensures clean separation of concerns while maintaining backward compatibility. The JSON format offers significant advantages for human readability, version control, and interoperability while the existing binary format remains available for users who prefer it.

The implementation can be done incrementally, allowing for thorough testing and validation at each phase. The architecture supports future expansion to additional formats with minimal changes to the core application logic.