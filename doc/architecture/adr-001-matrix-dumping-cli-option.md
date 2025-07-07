# ADR-001: Matrix Dumping CLI Option for Structural Analysis Debugging

## Status

Proposed

## Context

JBeam performs structural analysis using finite element methods, assembling element stiffness matrices into a global system matrix. The structural analysis calculations are complex, particularly for Euler-Bernoulli beams with inner hinges that use static condensation techniques.

Currently, there is no way to inspect the intermediate matrices used in calculations, making debugging difficult when:
- Verifying finite element implementation correctness
- Understanding static condensation behavior in beams with inner hinges
- Preparing for JavaScript port by validating matrix calculations
- Investigating numerical issues in structural analysis

The CLI interface provides an ideal platform for adding detailed debugging output without cluttering the GUI.

## Decision

We will add a `--dump-matrices` (`-d`) CLI option that outputs detailed element matrices for debugging structural analysis calculations.

### Implementation Details

1. **CLI Option**: Add `--dump-matrices` / `-d` flag to JBeamCLI argument parsing
2. **Beam Matrix Access**: Add getter methods to Beam class for protected matrices:
   - `getLg()` - global element load vector (6×1)
   - `getSl()` - local element stiffness matrix (6×6)
   - `getMl()` - local element mass matrix (6×6) 
   - `getTransformationMatrix()` - transformation matrix a (6×6)
3. **View Enhancement**: Extend TextView and HtmlView with `dumpMatrices` parameter
4. **Matrix Output**: For each beam element, output:
   - Element ID and basic properties (length, EI, EA)
   - Location vector (DOF mapping to global system)
   - Local stiffness matrix Sl (6×6)
   - Global stiffness matrix Sg (6×6)
   - Transformation matrix a (6×6)
   - Local mass matrix Ml (6×6)
   - Global mass matrix Mg (6×6)
   - Global load vector Lg (6×1)
   - Inner hinge information for EBBeam elements

### Timing Considerations

The matrices will be dumped after solving completes, as the beam objects retain their matrix data throughout the analysis process. This avoids timing concerns about when matrices are calculated or consumed by the global system assembly.

## Consequences

### Positive
- **Enhanced Debugging**: Detailed matrix inspection capabilities for structural analysis
- **Validation Support**: Enables verification of finite element calculations
- **JavaScript Port Preparation**: Provides reference data for validating ported calculations
- **Educational Value**: Helps understand complex structural analysis internals
- **Non-Intrusive**: Optional CLI flag doesn't affect normal operation

### Negative
- **Verbose Output**: Matrix dumps will be very large, especially for complex models
- **Performance Impact**: Additional matrix formatting and output operations
- **Code Complexity**: Adds getter methods and formatting logic to multiple classes

### Neutral
- **Maintenance**: New feature requires ongoing maintenance and documentation
- **Testing**: Additional test cases needed for matrix output validation

## Example Usage

```bash
# Basic matrix dumping
./jbeam-cli --dump-matrices model.json

# Matrix dumping with file output
./jbeam-cli -d -o debug_output.txt model.json

# Combined with other options
./jbeam-cli --dump-matrices --show-all model.json
```

## Notes

This ADR addresses the need for detailed structural analysis debugging capabilities while maintaining the clean separation between GUI and CLI interfaces. The matrix dumping feature will be particularly valuable for understanding static condensation behavior in complex beam elements.