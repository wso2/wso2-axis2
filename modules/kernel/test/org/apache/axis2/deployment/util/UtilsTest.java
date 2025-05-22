/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.deployment.util;

import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilsTest extends TestCase {

    public void testGetCAppDescriptor() {
        File testCarFile = new File("test-resources/carbonapps/with-descriptor/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"); // Replace with a valid test CAR file path
        CAppDescriptor cAppDescriptor = new CAppDescriptor(testCarFile);
        assertTrue(cAppDescriptor.getCAppId().equals("com.example_TestProjectCompositeExporter1_1.0.0-SNAPSHOT")); // Replace with expected CApp ID
        assertNotNull(cAppDescriptor.getCAppDependencies());
        assertTrue(cAppDescriptor.getCAppDependencies().contains("com.example_TestProjectConfigs1_1.0.0-SNAPSHOT")); // Replace with expected dependency names
        assertTrue(cAppDescriptor.getCAppDependencies().contains("com.example_TestProjectConfigs2_1.0.0-SNAPSHOT")); // Replace with expected dependency names
    }

    public void testCreateCAppDependencyGraph() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/with-descriptor/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"), // Replace with valid test CAR file paths
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
        };

        List<CAppDescriptor> cAppDescriptors = Utils.getCAppDescriptors(testCarFiles);

        Map<String, List<String>> dependencyGraph = Utils.createCAppDependencyGraph(cAppDescriptors);
        assertNotNull(dependencyGraph);

        assertTrue(dependencyGraph.containsKey("com.example_TestProjectConfigs1_1.0.0-SNAPSHOT"));
        assertTrue(dependencyGraph.containsKey("com.example_TestProjectConfigs2_1.0.0-SNAPSHOT"));
        assertTrue(dependencyGraph.containsKey("com.example_TestProjectCompositeExporter1_1.0.0-SNAPSHOT"));
        assertTrue(dependencyGraph.get("com.example_TestProjectConfigs1_1.0.0-SNAPSHOT")
                .contains("com.example_TestProjectCompositeExporter1_1.0.0-SNAPSHOT"));
        assertTrue(dependencyGraph.get("com.example_TestProjectConfigs2_1.0.0-SNAPSHOT")
                .contains("com.example_TestProjectCompositeExporter1_1.0.0-SNAPSHOT"));
    }

    public void testGetDependencyGraphProcessingOrder() {

        File[] testCarFiles = {
                new File("test-resources/carbonapps/with-descriptor/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"),
                new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
                new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
        };

        List<CAppDescriptor> cAppDescriptors = Utils.getCAppDescriptors(testCarFiles);

        Map<String, List<String>> dependencyGraph = Utils.createCAppDependencyGraph(cAppDescriptors);
        List<String> processingOrder = Utils.getDependencyGraphProcessingOrder(dependencyGraph);
        assertNotNull(processingOrder);
        assertEquals(3, processingOrder.size());
        assertTrue(processingOrder.get(0).equals("com.example_TestProjectConfigs1_1.0.0-SNAPSHOT"));
        assertTrue(processingOrder.get(1).equals("com.example_TestProjectConfigs2_1.0.0-SNAPSHOT"));
        assertTrue(processingOrder.get(2).equals("com.example_TestProjectCompositeExporter1_1.0.0-SNAPSHOT"));
    }

    public void testGetDependentCAppProcessingOrder() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs2_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/with-descriptor/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car")
        };

        File[] orderedFiles = Utils.getCAppProcessingOrder(testCarFiles);
        assertNotNull(orderedFiles);
        assertEquals(3, orderedFiles.length);
        assertEquals("TestProjectConfigs1_1.0.0-SNAPSHOT.car", orderedFiles[0].getName());
        assertEquals("TestProjectConfigs2_1.0.0-SNAPSHOT.car", orderedFiles[1].getName());
        assertEquals("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car", orderedFiles[2].getName());
    }

    public void testGetRenamedCAppProcessingOrder() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/renamed/00.car"),
            new File("test-resources/carbonapps/renamed/01.car"),
            new File("test-resources/carbonapps/renamed/02.car")
        };

        File[] orderedFiles = Utils.getCAppProcessingOrder(testCarFiles);
        assertNotNull(orderedFiles);
        assertEquals(3, orderedFiles.length);
        assertEquals("01.car", orderedFiles[0].getName());
        assertEquals("02.car", orderedFiles[1].getName());
        assertEquals("00.car", orderedFiles[2].getName());
    }

    public void testGetWithOutDescriptorCAppProcessingOrder() {
        File[] testCarFiles = new File("test-resources/carbonapps/without-descriptor")
            .listFiles((dir, name) -> name.endsWith(".car"));
        File[] orderedFiles = Utils.getCAppProcessingOrder(testCarFiles);
        assertNotNull(orderedFiles);
        assertEquals(5, orderedFiles.length);
        assertEquals("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car", orderedFiles[0].getName());
        assertEquals("TestProjectConfigs1_1.0.0-SNAPSHOT.car", orderedFiles[1].getName());
        assertEquals("TestProjectConfigs2_1.0.0-SNAPSHOT.car", orderedFiles[2].getName());
        assertEquals("TestProjectConnectorExporter1_1.0.0-SNAPSHOT.car", orderedFiles[3].getName());
        assertEquals("TestProjectRegistryResources1_1.0.0-SNAPSHOT.car", orderedFiles[4].getName());
    }

    public void testGetWithAndWithoutDescriptorCAppProcessingOrder() {
        File[] testCarFiles = {
            new File("test-resources/carbonapps/with-descriptor/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs2_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/without-descriptor/TestProjectRegistryResources1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/without-descriptor/TestProjectConnectorExporter1_1.0.0-SNAPSHOT.car")
        };

        File[] orderedFiles = Utils.getCAppProcessingOrder(testCarFiles);
        assertNotNull(orderedFiles);
        assertEquals(5, orderedFiles.length);
        assertEquals("TestProjectConfigs1_1.0.0-SNAPSHOT.car", orderedFiles[0].getName());
        assertEquals("TestProjectConfigs2_1.0.0-SNAPSHOT.car", orderedFiles[1].getName());
        assertEquals("TestProjectConnectorExporter1_1.0.0-SNAPSHOT.car", orderedFiles[2].getName());
        assertEquals("TestProjectRegistryResources1_1.0.0-SNAPSHOT.car", orderedFiles[3].getName());
        assertEquals("TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car", orderedFiles[4].getName());
    }

    public void testGetCyclicCAppProcessingOrder() {
        File[] testCarFiles = new File("test-resources/carbonapps/cyclic").listFiles((dir, name) -> name.endsWith(".car"));

        try {
            Utils.getCAppProcessingOrder(testCarFiles);
            fail("Expected IllegalArgumentException due to cyclic dependency");
        } catch (IllegalArgumentException e) {
            assertEquals("Cycle detected in the dependency graph", e.getMessage());
        }
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
            new File("test-resources/carbonapps/with-descriptor/TestProjectCompositeExporter1_1.0.0-SNAPSHOT.car"), // Replace with valid test CAR file paths
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs1_1.0.0-SNAPSHOT.car"),
            new File("test-resources/carbonapps/with-descriptor/TestProjectConfigs2_1.0.0-SNAPSHOT.car")
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
            assertEquals("No cAppFile found for file identifier: com.example_TestProjectConfigs2_1.0.0-SNAPSHOT", e.getMessage());
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
