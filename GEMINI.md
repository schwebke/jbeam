# GEMINI.md

This file provides guidance to Gemini when working with code in this repository.

## Project Overview

JBeam is a Java-based structural analysis application for calculating plane frame and truss systems. It's a desktop application using Java Swing for the GUI and provides both standalone and applet execution modes.

**Version 4.1** introduces comprehensive JSON persistence capabilities as an alternative to Java serialization, providing human-readable, version control friendly file formats. Also adds command line interface for headless structural analysis.

## Build Commands

### Build the application
```bash
mvn clean package
```

### Run the GUI application
```bash
java -jar target/jbeam-4.1.0-jar-with-dependencies.jar
```

### Run the CLI application
```bash
# Using the convenience script
./jbeam-cli [OPTIONS] INPUT_FILE

# Or directly with Java
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI [OPTIONS] INPUT_FILE
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

- **View**: `com.schwebke.jbeam.view` - Graphical display and output components
  - `View.java` - Main drawing canvas with zoom, pan, and rendering
  - Renderer classes for different element types
  - `TextView.java`, `HtmlView.java` - Result output formatting with optional JSON-style ID display

- **Controller**: `com.schwebke.jbeam` - Application control and coordination
  - `JBeam.java` - Main controller class handling UI events and model updates
  - `JBeamApplication.java` - GUI application entry point
  - `JBeamCLI.java` - Command line interface for headless analysis
  - `IController.java` - Interface for decoupling views from specific controllers
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
- GUI Main class: `com.schwebke.jbeam.JBeamApplication`
- CLI Main class: `com.schwebke.jbeam.JBeamCLI`
- Applet class: `com.schwebke.jbeam.applet.JBeamApplet`

### Testing
Testing can be performed through:
- **CLI Testing**: Use `JBeamCLI` for automated testing of structural analysis calculations
- **File Operations**: Verify save/load operations for both JSON and JBM formats
- **Validation Testing**: Test JSON model validation with various error conditions
- **GUI Testing**: Manual testing of GUI components
- **Result Verification**: Use `--show-all` CLI option to get complete output including unlabeled elements

### Code Style
- **Internationalization**: German comments being translated to English (see `doc/architecture/translation-status.md`)
- Mixed German/English in code (legacy)
- Uses older Java patterns (pre-generics era originally, modernized to some extent)
- New code follows modern Java practices

## Documentation

### Architecture Documentation
- **Location**: `doc/architecture/`
- **JSON Persistence Design**: `doc/architecture/json-persistence/` - Complete design and implementation of JSON import/export capabilities
- **Translation Status**: `doc/architecture/translation-status.md` - Tracks German-to-English comment translation progress

### User Documentation  
- **German**: `doc/user/de/` - German user documentation and tutorials
- **English**: `doc/user/en/` - English user documentation and tutorials

## Command Line Interface (CLI)

JBeam includes a comprehensive CLI for headless structural analysis, perfect for automated testing and JavaScript porting preparation.

### Basic Usage
```bash
# Simple usage with convenience script
./jbeam-cli [OPTIONS] INPUT_FILE

# Full Java command
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI [OPTIONS] INPUT_FILE
```

### CLI Options
- `-i, --input FILE` - Input JSON model file
- `-o, --output FILE` - Output results file (default: console)
- `-f, --format FORMAT` - Output format: text|html (default: text)
- `-a, --analysis TYPE` - Analysis type: static|modal (default: static)
- `-s, --show-all` - Show all items including unlabeled ones with JSON IDs
- `-v, --version` - Show version information
- `-h, --help` - Show help message

### Key Features
- **Headless Operation**: No GUI dependencies for analysis
- **JSON Model Support**: Load and analyze JSON models
- **Multiple Output Formats**: Text and HTML result export
- **Complete Element Coverage**: `--show-all` option displays all elements using JSON-style IDs (`node-1`, `beam-1`, etc.)
- **Static and Modal Analysis**: Both analysis types supported
- **Error Handling**: Comprehensive validation with user-friendly error messages

### Examples
```bash
# Basic analysis with console output
./jbeam-cli model.json

# Complete analysis with all elements shown
./jbeam-cli --show-all model.json

# Modal analysis with HTML output
./jbeam-cli -i model.json -o results.html -f html -a modal

# Static analysis with text output to file
./jbeam-cli -i model.json -o results.txt -f text -a static --show-all

# Help and version info
./jbeam-cli --help
./jbeam-cli --version
```

### JSON ID System
The CLI uses the same ID generation scheme as JSON export:
- **Nodes**: `node-1`, `node-2`, `node-3`, etc.
- **Beams**: `beam-1`, `beam-2`, `beam-3`, etc.

This ensures consistency between file formats and provides reliable element identification for testing and debugging.
