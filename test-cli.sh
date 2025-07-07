#!/bin/bash
# Test script for JBeam CLI
# Demonstrates various CLI capabilities

echo "=== JBeam CLI Test Suite ==="
echo

# Test 1: Help
echo "Test 1: CLI Help"
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI -h
echo

# Test 2: Version
echo "Test 2: Version"
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI -v
echo

# Test 3: Static analysis with text output
echo "Test 3: Static Analysis (Text Output)"
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI beam1.json -f text
echo

# Test 4: Static analysis with HTML output
echo "Test 4: Static Analysis (HTML Output)"
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI beam1.json -f html -o test_results.html
echo "HTML output saved to test_results.html"
echo

# Test 5: Modal analysis
echo "Test 5: Modal Analysis"
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI beam1.json -a modal -f text
echo

# Test 6: Error handling
echo "Test 6: Error Handling (Invalid Model)"
java -cp target/jbeam-4.1.0-jar-with-dependencies.jar com.schwebke.jbeam.JBeamCLI truss1-err.json 2>&1 | head -5
echo

echo "=== All tests completed ==="