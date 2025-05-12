package org.apache.axis2.deployment.util;

import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilsTest extends TestCase {

    public void testGetCAppDependencies() {
        File testCarFile = new File("test-resources/carbonapps/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"); // Replace with a valid test CAR file path
        List<String> cAppDependencies = Utils.getCAppDependencies(testCarFile);
        assertNotNull(cAppDependencies);
        assertTrue(cAppDependencies.contains("TestProjectConfigs1_1.0.0-SNAPSHOT.car")); // Replace with expected dependency names
        assertTrue(cAppDependencies.contains("TestProjectConfigs2_1.0.0-SNAPSHOT.car")); // Replace with expected dependency names
    }

    public void testCreateCAppDependencyGraph() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"), // Replace with valid test CAR file paths
            new File("test-resources/carbonapps/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
        };

        Map<String, List<String>> dependencyGraph = Utils.createCAppDependencyGraph(testCarFiles);
        assertNotNull(dependencyGraph);

        assertTrue(dependencyGraph.containsKey("TestProjectConfigs1_1.0.0-SNAPSHOT.car"));
        assertTrue(dependencyGraph.containsKey("TestProjectConfigs2_1.0.0-SNAPSHOT.car"));
        assertTrue(dependencyGraph.containsKey("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"));
        assertTrue(dependencyGraph.get("TestProjectConfigs1_1.0.0-SNAPSHOT.car")
                .contains("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"));
        assertTrue(dependencyGraph.get("TestProjectConfigs2_1.0.0-SNAPSHOT.car")
                .contains("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"));
    }

    public void testGetDependencyGraphProcessingOrder() {
        Map<String, List<String>> dependencyGraph = Utils.createCAppDependencyGraph(new File[]{
            new File("test-resources/carbonapps/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
        });

        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertEquals(3, processingOrder.size());
        assertTrue(processingOrder.get(0).equals("TestProjectConfigs1_1.0.0-SNAPSHOT.car"));
        assertTrue(processingOrder.get(1).equals("TestProjectConfigs2_1.0.0-SNAPSHOT.car"));
        assertTrue(processingOrder.get(2).equals("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"));
    }

    public void testComplexDependencyGraphProcessingOrder() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("A", Arrays.asList("B", "C"));
        dependencyGraph.put("B", Collections.singletonList("D"));
        dependencyGraph.put("C", Arrays.asList("D", "E"));
        dependencyGraph.put("D", Collections.emptyList());
        dependencyGraph.put("E", Collections.emptyList());

        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertEquals(5, processingOrder.size());
        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), processingOrder);
    }

    public void testMultipleSeparatedGraphsProcessingOrder() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        // First graph
        dependencyGraph.put("A", Arrays.asList("B", "C"));
        dependencyGraph.put("B", Collections.singletonList("D"));
        dependencyGraph.put("C", Collections.emptyList());
        dependencyGraph.put("D", Collections.emptyList());

        // Second graph
        dependencyGraph.put("X", Arrays.asList("Y", "Z"));
        dependencyGraph.put("Y", Collections.emptyList());
        dependencyGraph.put("Z", Collections.emptyList());

        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertEquals(7, processingOrder.size());
        assertEquals(Arrays.asList("A", "X", "B", "C", "Y", "Z", "D"), processingOrder);
    }

    public void testMultipleDependenciesPointingToSameRootProject() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("A", Arrays.asList("B", "D"));
        dependencyGraph.put("B", Collections.singletonList("C"));
        dependencyGraph.put("E", Arrays.asList("F", "G"));
        dependencyGraph.put("F", Collections.singletonList("C"));
        dependencyGraph.put("C", Collections.emptyList());
        dependencyGraph.put("D", Collections.emptyList());
        dependencyGraph.put("G", Collections.emptyList());

        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertEquals(7, processingOrder.size());
        assertEquals(Arrays.asList("A", "E", "B", "D", "F", "G", "C"), processingOrder);
    }

    public void testEmptyDependencyGraphProcessingOrder() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertTrue(processingOrder.isEmpty());
    }

    public void testMultipleProjectsWithSameDependency() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("I", Arrays.asList("A", "B", "C", "D", "E"));
        dependencyGraph.put("J", Arrays.asList("I", "K"));
        dependencyGraph.put("A", Collections.emptyList());
        dependencyGraph.put("B", Collections.emptyList());
        dependencyGraph.put("C", Collections.emptyList());
        dependencyGraph.put("D", Collections.emptyList());
        dependencyGraph.put("E", Collections.emptyList());
        dependencyGraph.put("K", Collections.emptyList());

        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertEquals(8, processingOrder.size());
        assertEquals(Arrays.asList("J", "I", "K", "A", "B", "C", "D", "E"), processingOrder);
    }

    public void testCyclicDependencyGraphProcessingOrder() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("Exporter.car", Collections.singletonList("Config1.car"));
        dependencyGraph.put("Config1.car", Collections.singletonList("Config2.car"));
        dependencyGraph.put("Config2.car", Collections.singletonList("Exporter.car")); // Cyclic dependency
        try {
            Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        } catch (IllegalArgumentException e) {
            assertEquals("Cycle detected in the dependency graph", e.getMessage());
        }
    }

    public void testInnerCyclicDependencyGraphProcessingOrder() {
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("A", Arrays.asList("B", "C"));
        dependencyGraph.put("B", Arrays.asList("C", "D"));
        dependencyGraph.put("C", Collections.singletonList("E"));
        dependencyGraph.put("D", Collections.singletonList("B")); // Cyclic dependency
        dependencyGraph.put("E", Collections.emptyList());

        try {
            Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        } catch (IllegalArgumentException e) {
            assertEquals("Cycle detected in the dependency graph", e.getMessage());
        }
    }

    public void testGetCAppProcessingOrder() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"), // Replace with valid test CAR file paths
            new File("test-resources/carbonapps/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
        };

        File[] orderedFiles = Utils.getCAppProcessingOrder(testCarFiles);
        assertNotNull(orderedFiles);
        assertEquals(3, orderedFiles.length);
        assertEquals("TestProjectConfigs1_1.0.0-SNAPSHOT.car", orderedFiles[0].getName());
        assertEquals("TestProjectConfigs2_1.0.0-SNAPSHOT.car", orderedFiles[1].getName());
        assertEquals("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car", orderedFiles[2].getName());
    }

    public void testGetCAppProcessingOrderWithoutDependencies() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/TestProjectConfigs1_1.0.0-SNAPSHOT.car"), // Replace with valid independent CAR file paths
            new File("test-resources/carbonapps/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
        };

        File[] orderedFiles = Utils.getCAppProcessingOrder(testCarFiles);
        assertNotNull(orderedFiles);
        assertEquals(2, orderedFiles.length);
        assertEquals("TestProjectConfigs1_1.0.0-SNAPSHOT.car", orderedFiles[0].getName());
        assertEquals("TestProjectConfigs2_1.0.0-SNAPSHOT.car", orderedFiles[1].getName());
    }

    public void testMissingCAppDependency() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"), // Replace with valid test CAR file paths
            new File("test-resources/carbonapps/TestProjectConfigs1_1.0.0-SNAPSHOT.car")
        };

        try {
            Utils.getCAppProcessingOrder(testCarFiles);
        } catch (IllegalArgumentException e) {
            assertEquals("No cAppFile found for fileName: TestProjectConfigs2_1.0.0-SNAPSHOT.car", e.getMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        File extractedDir = new File("test-resources/carbonapps/extracted/");
        if (extractedDir.exists()) {
            deleteDirectory(extractedDir);
        }
        super.tearDown();
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}
