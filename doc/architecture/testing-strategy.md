# Testing Strategy for JBeam Mathematical Functions

## Overview

JBeam's structural analysis relies heavily on complex mathematical computations including linear equation solving, matrix operations, eigenvalue computation, and finite element method calculations. This document outlines a comprehensive testing strategy to ensure the correctness and reliability of these mathematical functions.

## Testing Philosophy

### Verification Hierarchy
1. **Unit Level**: Individual mathematical functions (matrix operations, solvers)
2. **Integration Level**: Element matrix generation and assembly
3. **System Level**: Complete structural analysis workflows
4. **Validation Level**: Comparison against analytical solutions and reference implementations

### Test Categories

#### 1. Mathematical Function Tests
**Purpose**: Verify core mathematical operations  
**Location**: `src/test/java/com/schwebke/math/`

- **Matrix Operations** (`MatrixTest.java`)
  - Basic operations (addition, multiplication, transpose, inverse)
  - Numerical stability and accuracy
  - Edge cases (singular matrices, zero matrices)
  
- **Linear Equation Solvers** (`LinearSolverTest.java`)
  - Known solution verification
  - Solver consistency across different methods
  - Conditioning and stability tests
  - Performance benchmarks

- **Eigenvalue Computation** (`EigenvalueTest.java`)
  - Modal analysis verification
  - Comparison with analytical eigenvalues
  - Convergence testing

#### 2. Finite Element Tests
**Purpose**: Verify element behavior and matrix generation  
**Location**: `src/test/java/com/schwebke/jbeam/model/`

- **Element Matrix Tests** (`BeamElementTest.java`)
  - Local stiffness matrix verification
  - Mass matrix validation
  - Transformation matrix accuracy
  - Load vector computation

- **Assembly Tests** (`AssemblyTest.java`)
  - Global matrix assembly correctness
  - DOF mapping verification
  - Boundary condition application

- **Solution Tests** (`SolutionTest.java`)
  - End-to-end analysis verification
  - Displacement and force recovery
  - Static condensation validation (for inner hinges)

#### 3. Analytical Verification Tests
**Purpose**: Compare against known analytical solutions  
**Location**: `src/test/resources/analytical/`

Test cases with closed-form solutions:

- **Simply Supported Beam**
  ```
  Deflection: w = qL⁴/(384EI)
  Moment: M = qL²/8 (center)
  ```

- **Cantilever Beam**
  ```
  Tip deflection: δ = PL³/(3EI)
  Tip moment: M = PL
  ```

- **Truss Elements**
  ```
  Axial displacement: δ = PL/(EA)
  Axial force: N = P
  ```

- **Modal Analysis**
  ```
  Beam frequencies: ωₙ = (nπ)²√(EI/mL⁴)
  ```

#### 4. Matrix Property Tests
**Purpose**: Verify mathematical properties of assembled matrices

```java
@Test
public void testGlobalStiffnessMatrixProperties() {
    // Symmetry: K = Kᵀ
    assertTrue(isSymmetric(globalK, 1e-12));
    
    // Positive semi-definiteness (stable structures)
    assertTrue(isPositiveSemiDefinite(globalK));
    
    // Rank deficiency equals rigid body modes
    // 2D structures: 3 rigid body modes per unconstrained component
    assertEquals(expectedRigidBodyModes, getRankDeficiency(globalK));
}
```

## Test Data Management

### Test Model Library
**Location**: `src/test/resources/test-models/`

Standardized JSON models for different test scenarios:

```
test-models/
├── unit/
│   ├── single-truss.json         # Basic truss element
│   ├── single-beam.json          # Basic beam element
│   └── beam-with-hinges.json     # Static condensation test
├── analytical/
│   ├── cantilever-point-load.json
│   ├── simply-supported-uniform.json
│   └── truss-bridge.json
├── regression/
│   ├── complex-frame.json        # Multi-element structure
│   └── modal-analysis.json       # Eigenvalue test case
└── benchmark/
    ├── large-truss.json          # Performance testing
    └── dense-frame.json          # Solver stress test
```

### Reference Solutions
**Location**: `src/test/resources/reference/`

Pre-computed correct results for regression testing:

```
reference/
├── v4.1.0/                      # Version-specific baselines
│   ├── cantilever_results.txt
│   ├── cantilever_matrices.txt
│   └── truss_bridge_results.txt
├── analytical/                  # Hand-calculated solutions
│   ├── beam_deflections.csv
│   └── modal_frequencies.csv
└── external/                    # Third-party verification
    ├── nastran_comparison.txt
    └── matlab_verification.csv
```

## CLI Integration for Testing

### Matrix Dumping for Verification
```bash
# Generate matrix dumps for verification
./jbeam-cli --dump-matrices test-cantilever.json > matrices/cantilever.txt

# Automated verification against external tools
python scripts/verify-against-matlab.py matrices/cantilever.txt
```

### Regression Testing Pipeline
```bash
#!/bin/bash
# test-regression.sh

echo "Running JBeam regression tests..."

# Test against known solutions
for model in test-models/regression/*.json; do
    echo "Testing $model..."
    
    # Generate current results
    ./jbeam-cli "$model" > "current/$(basename $model .json).txt"
    
    # Compare with reference
    if ! diff -q "reference/$(basename $model .json).txt" \
                 "current/$(basename $model .json).txt"; then
        echo "FAIL: $model results changed"
        exit 1
    fi
done

echo "All regression tests passed"
```

### Performance Benchmarking
```bash
# Measure solver performance
./jbeam-cli --dump-matrices large-structure.json 2>&1 | \
    grep "Analysis time" > performance/solver_timing.log
```

## Cross-Verification Strategy

### External Tool Comparison
Compare JBeam results against established FEM tools:

- **MATLAB Structural Toolbox**: For analytical verification
- **OpenSees**: For complex nonlinear cases (future)
- **SAP2000/ETABS**: For industry standard comparison
- **Hand calculations**: For simple cases

### Implementation
```python
# scripts/verify-against-matlab.py
def compare_matrices(jbeam_file, matlab_file):
    """Compare JBeam matrix dump with MATLAB results"""
    jbeam_matrices = parse_jbeam_matrices(jbeam_file)
    matlab_matrices = load_matlab_matrices(matlab_file)
    
    for matrix_name in jbeam_matrices:
        diff = numpy.abs(jbeam_matrices[matrix_name] - 
                        matlab_matrices[matrix_name])
        max_error = numpy.max(diff)
        
        assert max_error < 1e-10, f"{matrix_name} differs by {max_error}"
```

## Test Automation

### Build Integration
```xml
<!-- pom.xml - Maven integration -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
        <systemPropertyVariables>
            <jbeam.test.data.path>${project.basedir}/src/test/resources</jbeam.test.data.path>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

### Continuous Integration
```yaml
# .github/workflows/math-tests.yml
name: Mathematical Function Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        
    - name: Run unit tests
      run: mvn test
      
    - name: Run regression tests
      run: |
        mvn package -q
        ./scripts/test-regression.sh
        
    - name: Verify against analytical solutions
      run: python scripts/verify-analytical.py
```

## Error Tolerance and Accuracy

### Numerical Precision Guidelines
- **Matrix operations**: `1e-12` relative tolerance
- **Displacement results**: `1e-10` absolute tolerance
- **Force/moment results**: `1e-8` absolute tolerance
- **Eigenvalue computation**: `1e-6` relative tolerance

### Condition Number Monitoring
```java
@Test
public void testMatrixConditioning() {
    double conditionNumber = Matrix.condition(globalK);
    
    // Warn if matrix is ill-conditioned
    if (conditionNumber > 1e12) {
        System.out.println("Warning: Ill-conditioned matrix detected");
    }
    
    // Fail if extremely ill-conditioned
    assertTrue("Matrix too ill-conditioned", conditionNumber < 1e15);
}
```

## Documentation and Reporting

### Test Documentation
Each test case should include:
- **Purpose**: What is being tested
- **Expected behavior**: What should happen
- **Tolerance**: Acceptable error bounds
- **Reference**: Source of expected results

### Test Reports
Generate comprehensive test reports including:
- Matrix condition numbers
- Solver performance metrics
- Accuracy comparisons
- Regression test status

## Future Enhancements

### JavaScript Port Preparation
The testing framework should support validation of the future JavaScript port:
- Export test matrices in JSON format
- Create language-agnostic test specifications
- Develop comparison tools for cross-language verification

### Advanced Testing
- **Monte Carlo testing**: Random matrix generation
- **Stress testing**: Large systems and edge cases
- **Memory usage**: Profile matrix operations
- **Parallel performance**: Multi-threaded solver testing

## Implementation Priority

1. **Phase 1** (Immediate): Basic unit tests for Matrix class
2. **Phase 2** (Short-term): Element matrix verification tests
3. **Phase 3** (Medium-term): Analytical solution comparison
4. **Phase 4** (Long-term): Performance benchmarking and CI integration

This testing strategy ensures mathematical correctness while providing a foundation for future development and the planned JavaScript port.