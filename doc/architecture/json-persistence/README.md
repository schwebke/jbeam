# JSON Persistence for JBeam

This directory contains the architectural design and specifications for adding JSON import/export capabilities to JBeam as an alternative to the existing Java serialization persistence mechanism.

## Files

- **[json-persistence-design.md](json-persistence-design.md)** - Complete architectural design document with implementation details, rationale, and technical specifications
- **[jbeam-model-schema.json](jbeam-model-schema.json)** - JSON Schema specification for JBeam 2D structural models
- **[example-model.json](example-model.json)** - Example JBeam model in JSON format demonstrating the schema

## Overview

The design implements a Strategy Pattern to support multiple persistence formats while maintaining backward compatibility with existing `.jbm` files. The JSON format is specifically designed for JBeam's 2D plane frame analysis capabilities.

### Key Features

- **2D Structure**: Properly reflects JBeam's plane frame analysis (X-Z coordinates, not 3D)
- **Complete Model Data**: Includes nodes, beams, constraints, loads, distributed loads, and internal hinges
- **Type Safety**: JSON Schema with proper validation for all element types
- **Extensible Architecture**: Easy to add additional persistence formats in the future

### Supported Element Types

1. **Nodes**: 2D coordinates, constraints (X, Z, rotation), and nodal loads
2. **Truss Elements**: Axial-only elements with EA material properties
3. **Euler-Bernoulli Beams**: Full beam elements with EA/EI properties, distributed loads, and internal hinges
4. **EBS Beams**: Beams with shear deformation (EA/EI/GA properties)

### Benefits

- **Human Readable**: JSON files can be viewed and edited in text editors
- **Version Control Friendly**: Meaningful diffs for structural changes
- **Interoperable**: Other tools can consume/produce JBeam JSON files
- **Portable**: Cross-platform without Java serialization dependencies

## Implementation Status

**COMPLETED** - JSON persistence is fully implemented and integrated in JBeam v4.1.0.

### Features Implemented:
- ✅ Strategy Pattern persistence architecture with PersistenceManager
- ✅ Complete JSON import/export functionality with Jackson ObjectMapper
- ✅ Comprehensive DTO classes for all beam types (Truss, EBBeam, EBSBeam)
- ✅ JSON Schema validation with detailed error reporting
- ✅ Model integrity validation with smart error/warning classification
- ✅ UI integration with both .jbm and .json file format support
- ✅ JSON set as default file format with backward compatibility
- ✅ User-friendly warning dialogs for model issues

### User Experience:
- Save/Open dialogs default to JSON format
- Status bar shows validation warning counts
- Detailed warning dialogs for model integrity issues
- Seamless switching between file formats
- All existing .jbm files remain fully compatible