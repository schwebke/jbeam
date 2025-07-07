# Architecture Decision Record: Math Library Testing Strategy

## Status

Proposed

## Context

The `com.schwebke.math` package contains fundamental mathematical routines that are critical to the correctness of the entire JBeam application. The structural analysis calculations, particularly the finite element method (FEM) solvers, depend heavily on the accuracy and robustness of the linear equation system (LES) solvers, matrix operations, and eigenvalue computations.

Currently, there is no dedicated, automated test suite for these core mathematical components. Verification has been implicit, relying on the overall application behavior and manual checks. To improve reliability, prevent regressions, and facilitate future development (such as refactoring or performance optimization), a systematic testing strategy is required.

## Decision

We will implement a comprehensive unit testing suite for the `com.schwebke.math` package using JUnit. The initial focus will be on the `Solver` class, which is responsible for solving linear equation systems.

### Testing Strategy for `com.schwebke.math.Solver`

1.  **Test Framework**: JUnit 5 will be the primary framework for writing and executing tests.
2.  **Test File Location**: Test classes will be located in the `src/test/java/` directory, mirroring the package structure of the source code. The test for the `Solver` class will be `src/test/java/com/schwebke/math/SolverTest.java`.
3.  **Test Case Design**:
    *   **Known Solutions**: Test cases will be designed around linear equation systems (`Ax = b`) where the solution vector `x` is known beforehand. This allows for direct verification of the solver's output.
    *   **Simple Systems**: We will start with small, well-behaved 2x2 or 3x3 systems that can be easily solved by hand or with trusted external tools.
    *   **Assertions**: Assertions will be made using `org.junit.jupiter.api.Assertions.assertArrayEquals` for arrays, with a specified tolerance (delta) to account for floating-point arithmetic inaccuracies.
    *   **Edge Cases**: We will include tests for edge cases, such as:
        *   A singular matrix `A` (non-invertible), where the solver is expected to throw an exception.
        *   An identity matrix `A`, where the solution `x` should be identical to `b`.

### Example Test Case Structure

A typical test method in `SolverTest.java` would look like this:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SolverTest {

    @Test
    void testSolveSimple2x2System() {
        // System:
        // 2x + 3y = 8
        // 5x - 1y = 3
        PMatrix a = new PMatrix(2, 2);
        a.mat[0][0] = 2; a.mat[0][1] = 3;
        a.mat[1][0] = 5; a.mat[1][1] = -1;

        double[] b = {8, 3};
        double[] expectedX = {1, 2}; // Known solution: x=1, y=2

        double[] actualX = Solver.solve(a, b);

        assertArrayEquals(expectedX, actualX, 1e-9);
    }
}
```

## Consequences

### Positive

*   **Increased Confidence**: Provides strong guarantees about the correctness of the core mathematical calculations.
*   **Regression Prevention**: Protects against accidental bugs introduced during future code changes.
*   **Improved Maintainability**: Makes it safer and easier to refactor or optimize the math library.
*   **Clear Documentation**: The tests will serve as executable documentation, showing how the math components are intended to be used.

### Negative

*   **Initial Effort**: Requires an initial investment of time to set up the testing infrastructure and write the first set of tests.
*   **Build Time**: Adds a small amount of time to the overall build process.

## Future Work

*   Expand test coverage to other classes in the `com.schwebke.math` package, including `Matrix`, `PMatrix`, and `Eigen`.
*   Introduce tests for more complex or larger systems.
*   Consider property-based testing to generate a wider range of test cases automatically.
