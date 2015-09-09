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

package org.apache.axis2.testutils;

import java.io.File;
import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class UtilServer {
    private static SimpleHTTPServer receiver;

    public static synchronized void deployService(AxisService service)
            throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().addService(
                service);
    }

    public static synchronized void unDeployService(QName service)
            throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration()
                .removeService(service.getLocalPart());
    }

    public static synchronized void start(int testingPort, String repository) throws Exception {
        start(testingPort, repository, null);
    }

    public static synchronized void start(int testingPort, String repository, String axis2xml) throws Exception {
        if (receiver != null) {
            System.out.println("Server already started !!");
            throw new IllegalStateException("Server already started");
        }
        ConfigurationContext er = getNewConfigurationContext(repository, axis2xml);

        receiver = new SimpleHTTPServer(er, testingPort);

        try {
            receiver.start();
        } catch (AxisFault e) {
            System.out.println("Error occurred starting the server. " + e.getMessage());
            throw e;
        }
        System.out.print("Server started on port " + testingPort + ".....");
    }

    public static ConfigurationContext getNewConfigurationContext(
            String repository, String axis2xml) throws Exception {
        File file = new File(repository);
        if (!file.exists()) {
            throw new Exception("repository directory "
                    + file.getAbsolutePath() + " does not exists");
        }
        if (axis2xml == null) {
            axis2xml = file.getAbsolutePath() + "/conf/axis2.xml";
        }
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(file.getAbsolutePath(), axis2xml);
    }

    public static synchronized void stop() throws AxisFault {
        if (receiver == null) {
            throw new IllegalStateException("Server not started");
        }
        receiver.stop();
        waitUntilStopped();
        // tp.doStop();
        System.out.print("Server stopped .....");
        ListenerManager listenerManager =
                receiver.getConfigurationContext().getListenerManager();
        if (listenerManager != null) {
            listenerManager.stop();
        }
        receiver = null;
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getConfigurationContext();
    }

    public static boolean waitUntilStopped() {
        System.out.println("Waiting until receiver stopped ..");
        while (receiver != null && receiver.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                //ignore
            }
        }
        return true;
    }
}
