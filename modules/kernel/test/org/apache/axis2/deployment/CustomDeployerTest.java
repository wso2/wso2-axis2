/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.deployment;

import junit.framework.TestCase;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.deployers.CustomDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomDeployerTest extends TestCase {
    public void testCustomDeployer() throws Exception {
        String filename =
                AbstractTestCase.basedir + "/test-resources/deployment/CustomDeployerRepo";
        AxisConfiguration axisConfig = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(filename)
                .getAxisConfiguration();

        // OK, let's see what we've got here...
        assertTrue("Init was not called", CustomDeployer.initCalled);
        assertEquals("Wrong directory", "widgets", CustomDeployer.directory);
        assertEquals("Wrong extension", "svc", CustomDeployer.extension);
        assertEquals("Wrong number of deployed items", 1, CustomDeployer.deployedItems);
        assertTrue("Mary wasn't found", CustomDeployer.maryDeployed);

        assertEquals("Parameter not set correctly",
                     CustomDeployer.PARAM_VAL,
                     axisConfig.getParameterValue(CustomDeployer.PARAM_NAME));
    }

    public void testSortCompleteList() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("zeta.svc"), null));
        files.add(new DeploymentFileData(new File("alpha.svc"), null));
        files.add(new DeploymentFileData(new File("beta.svc"), null));

        deployer.sort(files, 0, files.size());

        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
    }

    public void testSortEmptyList() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        deployer.sort(files, 0, files.size());
        assertTrue(files.isEmpty());
    }

    public void testSortSingleElement() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("single.svc"), null));
        deployer.sort(files, 0, files.size());
        assertEquals("single.svc", files.get(0).getFile().getName());
    }

    public void testSortAlreadySorted() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("alpha.svc"), null));
        files.add(new DeploymentFileData(new File("beta.svc"), null));
        files.add(new DeploymentFileData(new File("zeta.svc"), null));
        deployer.sort(files, 0, files.size());
        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
    }

    public void testSortReverseOrder() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("zeta.svc"), null));
        files.add(new DeploymentFileData(new File("beta.svc"), null));
        files.add(new DeploymentFileData(new File("alpha.svc"), null));
        deployer.sort(files, 0, files.size());
        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
    }

    public void testSortMiddleSublist() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("gamma.svc"), null));
        files.add(new DeploymentFileData(new File("zeta.svc"), null));
        files.add(new DeploymentFileData(new File("alpha.svc"), null));
        files.add(new DeploymentFileData(new File("beta.svc"), null));
        files.add(new DeploymentFileData(new File("omega.svc"), null));

        // Sort only the middle three elements (indices 1 to 3)
        deployer.sort(files, 1, 4);

        assertEquals("gamma.svc", files.get(0).getFile().getName());
        assertEquals("alpha.svc", files.get(1).getFile().getName());
        assertEquals("beta.svc", files.get(2).getFile().getName());
        assertEquals("zeta.svc", files.get(3).getFile().getName());
        assertEquals("omega.svc", files.get(4).getFile().getName());
    }

    public void testSortSublistAtStart() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("zeta.svc"), null));
        files.add(new DeploymentFileData(new File("alpha.svc"), null));
        files.add(new DeploymentFileData(new File("beta.svc"), null));
        files.add(new DeploymentFileData(new File("omega.svc"), null));

        // Sort only the first three elements (indices 0 to 2)
        deployer.sort(files, 0, 3);

        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
        assertEquals("omega.svc", files.get(3).getFile().getName());
    }

    public void testSortSublistAtEnd() {
        Deployer deployer = new CustomDeployer();
        List<DeploymentFileData> files = new ArrayList<>();
        files.add(new DeploymentFileData(new File("gamma.svc"), null));
        files.add(new DeploymentFileData(new File("omega.svc"), null));
        files.add(new DeploymentFileData(new File("zeta.svc"), null));
        files.add(new DeploymentFileData(new File("alpha.svc"), null));
        files.add(new DeploymentFileData(new File("beta.svc"), null));

        // Sort only the last three elements (indices 2 to 4)
        deployer.sort(files, 2, 5);

        assertEquals("gamma.svc", files.get(0).getFile().getName());
        assertEquals("omega.svc", files.get(1).getFile().getName());
        assertEquals("alpha.svc", files.get(2).getFile().getName());
        assertEquals("beta.svc", files.get(3).getFile().getName());
        assertEquals("zeta.svc", files.get(4).getFile().getName());
    }
}
