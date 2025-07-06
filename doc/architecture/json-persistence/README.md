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

This is currently a design specification. Implementation would follow the phased approach outlined in the main design document.