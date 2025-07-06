# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JBeam is a Java-based structural analysis application for calculating plane frame and truss systems. It's a desktop application using Java Swing for the GUI and provides both standalone and applet execution modes.

**Version 4.1** introduces comprehensive JSON persistence capabilities as an alternative to Java serialization, providing human-readable, version control friendly file formats.

## Build Commands

### Build the application
```bash
mvn clean package
```

### Run the application
```bash
java -jar target/jbeam-4.1.0-jar-with-dependencies.jar
```

### Compile only
```bash
mvn compile
```

## Architecture

### Model-View-Controller Pattern
- **Model**: `com.schwebke.jbeam.model` - Contains the structural analysis core
  - `Model.java` - Main model class handling geometry, topology, and calculations
  - `SelectableModel.java` - Extends Model with selection capabilities for the UI
  - `Node.java` - Represents structural nodes with coordinates and constraints
  - `Beam.java` - Abstract base class for structural elements
  - `Truss.java`, `EBBeam.java`, `EBSBeam.java` - Concrete beam implementations

- **View**: `com.schwebke.jbeam.view` - Graphical display components
  - `View.java` - Main drawing canvas with zoom, pan, and rendering
  - Renderer classes for different element types
  - `TextView.java`, `HtmlView.java` - Result output formatting

- **Controller**: `com.schwebke.jbeam` - Application control and coordination
  - `JBeam.java` - Main controller class handling UI events and model updates
  - `JBeamApplication.java` - Application entry point
  - Dialog classes (`DlgNode.java`, `DlgEBBeam.java`, etc.) - Property editors

### Key Components

#### Mathematical Core (`com.schwebke.math`)
- Matrix operations, eigenvalue computation, linear equation solving
- Handles the finite element method calculations

#### Plugin System (`com.schwebke.jbeam.plugin`)
- Interfaces for extending the application (`IHost.java`, `IModel.java`)

#### Persistence System (`com.schwebke.jbeam.persistence`)
- Strategy pattern supporting multiple file formats
- JSON format (`.json`) with Schema validation - default format (v4.1+)
- Java serialization format (`.jbm`) for backward compatibility
- Comprehensive model validation with error/warning reporting
- Text export: ASCII and HTML result output

## Development Notes

### Java Version
- Requires Java 11 (configured in pom.xml)
- Uses legacy Swing components

### Localization
- German and English language support
- Locale files in `resourcen/` directory

### Key Entry Points
- Main class: `com.schwebke.jbeam.JBeamApplication`
- Applet class: `com.schwebke.jbeam.applet.JBeamApplet`

### Testing
No automated tests are present in this codebase. Testing would typically involve:
- Running the application and testing structural analysis calculations
- Verifying file save/load operations for both JSON and JBM formats
- Testing JSON model validation with various error conditions
- Testing the GUI components

### Code Style
- German comments and variable names throughout
- Mixed German/English in code
- Uses older Java patterns (pre-generics era originally, modernized to some extent)

## Documentation

### Architecture Documentation
- **Location**: `doc/architecture/`
- **JSON Persistence Design**: `doc/architecture/json-persistence/` - Complete design and implementation of JSON import/export capabilities

### User Documentation  
- **German**: `doc/user/de/` - German user documentation and tutorials
- **English**: `doc/user/en/` - English user documentation and tutorials