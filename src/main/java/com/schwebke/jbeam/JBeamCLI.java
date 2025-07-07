package com.schwebke.jbeam;

import com.schwebke.jbeam.model.SelectableModel;
import com.schwebke.jbeam.persistence.JsonPersistence;
import com.schwebke.jbeam.view.TextView;
import com.schwebke.jbeam.view.HtmlView;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Command Line Interface for JBeam structural analysis.
 * Provides headless operation for loading JSON models, performing analysis,
 * and exporting results in text or HTML format.
 * 
 * This CLI is designed to support automated testing and future JavaScript porting.
 */
public class JBeamCLI {
    
    private static final String VERSION = "4.1.0";
    private NumberFormat numberFormat;
    
    public JBeamCLI() {
        // Initialize number formatting (similar to JBeam GUI)
        numberFormat = new DecimalFormat("0.000");
    }
    
    public static void main(String[] args) {
        JBeamCLI cli = new JBeamCLI();
        
        if (args.length == 0) {
            cli.showUsage();
            System.exit(1);
        }
        
        try {
            cli.run(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void run(String[] args) throws Exception {
        String inputFile = null;
        String outputFile = null;
        String outputFormat = "text"; // default to text
        String analysisType = "static"; // default to static analysis
        boolean showVersion = false;
        boolean showHelp = false;
        boolean showAllItems = false; // default to labeled items only
        boolean dumpMatrices = false; // default to no matrix dumping
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-h":
                case "--help":
                    showHelp = true;
                    break;
                case "-v":
                case "--version":
                    showVersion = true;
                    break;
                case "-i":
                case "--input":
                    if (i + 1 < args.length) {
                        inputFile = args[++i];
                    } else {
                        throw new IllegalArgumentException("Missing input file after " + arg);
                    }
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        outputFile = args[++i];
                    } else {
                        throw new IllegalArgumentException("Missing output file after " + arg);
                    }
                    break;
                case "-f":
                case "--format":
                    if (i + 1 < args.length) {
                        outputFormat = args[++i];
                        if (!outputFormat.equals("text") && !outputFormat.equals("html")) {
                            throw new IllegalArgumentException("Invalid output format: " + outputFormat + ". Use 'text' or 'html'");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing output format after " + arg);
                    }
                    break;
                case "-a":
                case "--analysis":
                    if (i + 1 < args.length) {
                        analysisType = args[++i];
                        if (!analysisType.equals("static") && !analysisType.equals("modal")) {
                            throw new IllegalArgumentException("Invalid analysis type: " + analysisType + ". Use 'static' or 'modal'");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing analysis type after " + arg);
                    }
                    break;
                case "-s":
                case "--show-all":
                    showAllItems = true;
                    break;
                case "-d":
                case "--dump-matrices":
                    dumpMatrices = true;
                    break;
                default:
                    // If no flag specified, assume it's the input file
                    if (inputFile == null && !arg.startsWith("-")) {
                        inputFile = arg;
                    } else {
                        throw new IllegalArgumentException("Unknown argument: " + arg);
                    }
                    break;
            }
        }
        
        if (showVersion) {
            System.out.println("JBeam CLI v" + VERSION);
            return;
        }
        
        if (showHelp) {
            showUsage();
            return;
        }
        
        if (inputFile == null) {
            throw new IllegalArgumentException("No input file specified");
        }
        
        // Load the model
        System.out.println("Loading model from: " + inputFile);
        SelectableModel model = loadModel(inputFile);
        
        // Perform analysis
        System.out.println("Performing " + analysisType + " analysis...");
        performAnalysis(model, analysisType);
        
        // Export results
        if (outputFile != null) {
            System.out.println("Exporting results to: " + outputFile + " (format: " + outputFormat + ")");
            exportResults(model, outputFile, outputFormat, showAllItems, dumpMatrices);
        } else {
            System.out.println("Exporting results to console (format: " + outputFormat + ")");
            exportResults(model, null, outputFormat, showAllItems, dumpMatrices);
        }
        
        System.out.println("Analysis completed successfully.");
    }
    
    private SelectableModel loadModel(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Input file not found: " + filePath);
        }
        
        if (!filePath.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Only JSON files are supported in CLI mode");
        }
        
        JsonPersistence persistence = new JsonPersistence();
        
        try (FileInputStream fis = new FileInputStream(file)) {
            SelectableModel model = persistence.load(fis);
            
            // Report any validation warnings
            if (JsonPersistence.lastValidationResult != null && 
                !JsonPersistence.lastValidationResult.getWarnings().isEmpty()) {
                System.out.println("Warning: Model loaded with " + 
                    JsonPersistence.lastValidationResult.getWarnings().size() + " warning(s):");
                for (String warning : JsonPersistence.lastValidationResult.getWarnings()) {
                    System.out.println("  - " + warning);
                }
            }
            
            return model;
        }
    }
    
    private void performAnalysis(SelectableModel model, String analysisType) {
        switch (analysisType) {
            case "static":
                model.calculate();
                break;
            case "modal":
                model.calculateModal();
                break;
            default:
                throw new IllegalArgumentException("Unknown analysis type: " + analysisType);
        }
        
        if (!model.getValidCalculation()) {
            throw new RuntimeException("Analysis failed - check model for errors");
        }
    }
    
    private void exportResults(SelectableModel model, String outputFile, String format, boolean showAllItems, boolean dumpMatrices) throws Exception {
        PrintWriter writer;
        
        if (outputFile != null) {
            writer = new PrintWriter(new FileWriter(outputFile));
        } else {
            writer = new PrintWriter(System.out);
        }
        
        try {
            // Create a minimal controller-like object to provide number formatting
            IController controller = new JBeamCLIController(numberFormat);
            
            switch (format) {
                case "text":
                    TextView textView = new TextView(model, controller, showAllItems, dumpMatrices);
                    textView.write(writer);
                    break;
                case "html":
                    HtmlView htmlView = new HtmlView(model, controller, showAllItems, dumpMatrices);
                    htmlView.write(writer);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown output format: " + format);
            }
        } finally {
            if (outputFile != null) {
                writer.close();
            } else {
                writer.flush();
            }
        }
    }
    
    private void showUsage() {
        System.out.println("JBeam CLI v" + VERSION);
        System.out.println("Usage: java -jar jbeam-cli.jar [OPTIONS] INPUT_FILE");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  -i, --input FILE       Input JSON model file");
        System.out.println("  -o, --output FILE      Output results file (default: console)");
        System.out.println("  -f, --format FORMAT    Output format: text|html (default: text)");
        System.out.println("  -a, --analysis TYPE    Analysis type: static|modal (default: static)");
        System.out.println("  -s, --show-all         Show all items including unlabeled ones with JSON IDs");
        System.out.println("  -d, --dump-matrices    Dump element matrices for debugging");
        System.out.println("  -v, --version          Show version information");
        System.out.println("  -h, --help             Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar jbeam-cli.jar model.json");
        System.out.println("  java -jar jbeam-cli.jar -i model.json -o results.txt -f text -a static");
        System.out.println("  java -jar jbeam-cli.jar -i model.json -o results.html -f html -a modal");
        System.out.println("  java -jar jbeam-cli.jar --show-all model.json  # Include unlabeled items");
        System.out.println("  java -jar jbeam-cli.jar --dump-matrices model.json  # Dump element matrices");
    }
    
    /**
     * Minimal controller implementation to provide number formatting for views.
     * This replaces the dependency on the full JBeam GUI controller.
     */
    private static class JBeamCLIController implements IController {
        private NumberFormat numberFormat;
        
        public JBeamCLIController(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
        }
        
        @Override
        public NumberFormat getNumberFormat() {
            return numberFormat;
        }
    }
}