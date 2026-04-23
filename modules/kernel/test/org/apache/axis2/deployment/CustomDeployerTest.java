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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.deployers.CustomDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomDeployerTest extends TestCase {

    private static final Deployer TEST_DEPLOYER = new Deployer() {
        public void init(ConfigurationContext configCtx) {}
        public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {}
        public void setDirectory(String directory) {}
        public void setExtension(String extension) {}
        public void undeploy(String fileName) throws DeploymentException {}
        public void cleanup() throws DeploymentException {}
    };

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

    private static List<DeploymentFileData> fileList(String... names) {
        List<DeploymentFileData> files = new ArrayList<>();
        for (String name : names) {
            files.add(new DeploymentFileData(new File(name), null));
        }
        return files;
    }

    public void testSortCompleteList() {
        List<DeploymentFileData> files = fileList("zeta.svc", "alpha.svc", "beta.svc");

        TEST_DEPLOYER.sort(files, 0, files.size());

        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
    }

    public void testSortEmptyList() {
        List<DeploymentFileData> files = fileList();
        TEST_DEPLOYER.sort(files, 0, files.size());
        assertTrue(files.isEmpty());
    }

    public void testSortSingleElement() {
        List<DeploymentFileData> files = fileList("single.svc");
        TEST_DEPLOYER.sort(files, 0, files.size());
        assertEquals("single.svc", files.get(0).getFile().getName());
    }

    public void testSortAlreadySorted() {
        List<DeploymentFileData> files = fileList("alpha.svc", "beta.svc", "zeta.svc");
        TEST_DEPLOYER.sort(files, 0, files.size());
        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
    }

    public void testSortReverseOrder() {
        List<DeploymentFileData> files = fileList("zeta.svc", "beta.svc", "alpha.svc");
        TEST_DEPLOYER.sort(files, 0, files.size());
        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
    }

    public void testSortMiddleSublist() {
        List<DeploymentFileData> files = fileList("gamma.svc", "zeta.svc", "alpha.svc", "beta.svc", "omega.svc");

        TEST_DEPLOYER.sort(files, 1, 4);

        assertEquals("gamma.svc", files.get(0).getFile().getName());
        assertEquals("alpha.svc", files.get(1).getFile().getName());
        assertEquals("beta.svc", files.get(2).getFile().getName());
        assertEquals("zeta.svc", files.get(3).getFile().getName());
        assertEquals("omega.svc", files.get(4).getFile().getName());
    }

    public void testSortSublistAtStart() {
        List<DeploymentFileData> files = fileList("zeta.svc", "alpha.svc", "beta.svc", "omega.svc");

        TEST_DEPLOYER.sort(files, 0, 3);

        assertEquals("alpha.svc", files.get(0).getFile().getName());
        assertEquals("beta.svc", files.get(1).getFile().getName());
        assertEquals("zeta.svc", files.get(2).getFile().getName());
        assertEquals("omega.svc", files.get(3).getFile().getName());
    }

    public void testSortSublistAtEnd() {
        List<DeploymentFileData> files = fileList("gamma.svc", "omega.svc", "zeta.svc", "alpha.svc", "beta.svc");

        TEST_DEPLOYER.sort(files, 2, 5);

        assertEquals("gamma.svc", files.get(0).getFile().getName());
        assertEquals("omega.svc", files.get(1).getFile().getName());
        assertEquals("alpha.svc", files.get(2).getFile().getName());
        assertEquals("beta.svc", files.get(3).getFile().getName());
        assertEquals("zeta.svc", files.get(4).getFile().getName());
    }
}
