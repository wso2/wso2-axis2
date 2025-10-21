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


package org.apache.axis2.deployment.repository.util;

import org.apache.axis2.Constants;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipFile;

public class WSInfoList implements DeploymentConstants {

    private static final Log log = LogFactory.getLog(WSInfoList.class);
    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private List jarList = new ArrayList();

    /**
     * All the currently updated jars
     */
    public Map currentJars = new HashMap();

    private Boolean isServiceUnDeploymentAllowed = true;

    /**
     * Reference to DeploymentEngine to make update
     */

    private boolean locked = false;
    private final DeploymentEngine deploymentEngine;

    public WSInfoList(DeploymentEngine deploy_engine) {
        deploymentEngine = deploy_engine;
    }

    public void setServiceUnDeploymentAllowed(Boolean isServiceUnDeploymentAllowed) {
        this.isServiceUnDeploymentAllowed = isServiceUnDeploymentAllowed;
    }

    /**
     * First checks whether the file is already available by the
     * system call fileExists. If it is not deployed yet then adds to the jarList
     * and to the deployment engine as a new service or module.
     * While adding new item to jarList, first creates the WSInfo object and
     * then adds to the jarlist and actual jar file is added to DeploymentEngine.
     * <p/>
     * If the files already exists, then checks whether it has been updated
     * then changes the last update date of the wsInfo and adds two entries to
     * DeploymentEngine - one for new deployment and other for undeployment.
     *
     * @param file actual jar files for either Module or service
     */
    public synchronized void addWSInfoItem(File file, Deployer deployer , int type) {
        WSInfo info = (WSInfo) currentJars.get(file.getAbsolutePath());
        if (info != null) {
            if (deploymentEngine.isHotUpdate() && isModified(file, info)) {
                WSInfo wsInfo = new WSInfo(info.getFileName(), info.getLastModifiedDate(), deployer,type);
                deploymentEngine.addWSToUndeploy(wsInfo);           // add entry to undeploy list
                DeploymentFileData deploymentFileData = new DeploymentFileData(file, deployer);
                deploymentEngine.addWSToDeploy(deploymentFileData);    // add entry to deploylist
                if (Utils.isFatCAR(file.getPath())) {
                    addDependentCARsToDeploymentList(file, deployer, type);
                }
            }
        } else {
            info = getFileItem(file, deployer, type);
            setLastModifiedDate(file, info);
        }

        jarList.add(info.getFileName());
    }

    /**
     * Checks undeployed Services. Checks old jars files and current jars.
     * If name of the old jar file does not exist in the current jar
     * list then it is assumed that the jar file has been removed
     * and that is hot undeployment.
     */
    private synchronized void checkForUndeployedServices() {
        if(!locked) {
            locked = true;
        } else{
            return;
        }
        if (isServiceUnDeploymentAllowed) {
            Iterator infoItems = currentJars.keySet().iterator();
            List tobeRemoved = new ArrayList();
            while (infoItems.hasNext()) {
                String  fileName = (String) infoItems.next();
                WSInfo infoItem = (WSInfo) currentJars.get(fileName);
                if (infoItem.getType() == WSInfo.TYPE_MODULE) {
                    continue;
                }
                //seems like someone has deleted the file , so need to undeploy
                boolean found = false;
                for (int i = 0; i < jarList.size(); i++) {
                    String s = (String) jarList.get(i);
                    if(fileName.equals(s)){
                        found = true;
                    }
                }
                if (!found) {
                    int index = fileName.lastIndexOf(File.separator);
                    String filepath = fileName.substring(0, index);
                    File dir = new File(filepath);
                    if (dir.exists()) {
                        tobeRemoved.add(fileName);
                        log.debug(fileName + " was added to undeployable list");
                        deploymentEngine.addWSToUndeploy(infoItem);
                    }
                }
            }

            for (int i = 0; i < tobeRemoved.size(); i++) {
                String fileName = (String) tobeRemoved.get(i);
                currentJars.remove(fileName);
            }
            tobeRemoved.clear();
            jarList.clear();
        } else {
            log.warn("Service un-deployment has been stopped");
        }
        locked = false;
    }

    /**
     * Clears the jarlist.
     */
    public void init() {
        jarList.clear();
    }

    /**
     *
     */
    public void update() {
        synchronized (deploymentEngine) {
            checkForUndeployedServices();
            deploymentEngine.unDeploy();
            deploymentEngine.doDeploy();
        }
    }

    /**
     * Gets the WSInfo object related to a file if it exists, null otherwise.
     *
     */
    private WSInfo getFileItem(File file , Deployer deployer , int type) {
        String fileName = file.getName();
        WSInfo info = (WSInfo) currentJars.get(fileName);
        if(info==null){
            info = new WSInfo(file.getAbsolutePath(), file.lastModified(), deployer ,type);
            currentJars.put(file.getAbsolutePath(), info);
            DeploymentFileData fileData = new DeploymentFileData(file, deployer);
            deploymentEngine.addWSToDeploy(fileData);
            if (Utils.isFatCAR(file.getPath())) {
                addDependentCARsToDeploymentList(file, deployer, type);
            }
        }
        return info;
    }

    private void addDependentCARsToDeploymentList(File parentCAR, Deployer deployer, int type) {

        log.info("A Fat Carbon Application has been detected : " + parentCAR.getName() +
                ". All dependent Carbon Applications will be deployed.");
        // Scan for nested CARs under dependencies/
        try (ZipFile zipFile = new ZipFile(parentCAR)) {
            zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> entry.getName().startsWith("dependencies/"))
                    .filter(entry -> entry.getName().toLowerCase(Locale.ROOT).endsWith(Constants.CAR_EXTENSION))
                    .forEach(entry -> {
                        File dependencyFile = new File(parentCAR.getPath(), entry.getName());
                        WSInfo dependencyInfo = new WSInfo(dependencyFile.getAbsolutePath(), dependencyFile.lastModified(), deployer, type);
                        currentJars.put(dependencyFile.getAbsolutePath(), dependencyInfo);
                        DeploymentFileData dependencyDeploymentFileData = new DeploymentFileData(dependencyFile, deployer);
                        dependencyDeploymentFileData.setEmbeddedCAR(true);
                        deploymentEngine.addWSToDeploy(dependencyDeploymentFileData);
                        deploymentEngine.addParentCAppToDependency(dependencyFile.getName(), parentCAR.getName());
                    });
        } catch (IOException exception) {
            log.error("Error while processing Fat CAR file : " + parentCAR.getName(), exception);
        }
    }

    /**
     * Checks if a file has been modified by comparing the last update date of
     * both files and WSInfo. If they are different, the file is assumed to have
     * been modified.
     *
     * @param file
     * @param wsInfo
     */
    private boolean isModified(File file, WSInfo wsInfo) {
        long currentTimeStamp = wsInfo.getLastModifiedDate();

        setLastModifiedDate(file, wsInfo);

        return (currentTimeStamp != wsInfo.getLastModifiedDate());
    }

    /**
     * Obtains the newest (as compared with timestamp stored in wsInfo)
     * timestamp and stores it in WSInfo. 
     */
    private void setLastModifiedDate(File file, WSInfo wsInfo) {
        if(file.getName().startsWith(".")){
            //skip the hidden meta files and directories. (eg: .svn)
            return;
        }
        if (file.isDirectory()) {
            File files [] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File fileItem = files[i];
                if (fileItem.isDirectory()) {
                    setLastModifiedDate(fileItem, wsInfo);
                }
                else if(wsInfo.getLastModifiedDate() < fileItem.lastModified()) {
                    wsInfo.setLastModifiedDate(fileItem.lastModified());
                }
            }
        }
        else if(wsInfo.getLastModifiedDate() != file.lastModified()) {
            wsInfo.setLastModifiedDate(file.lastModified());
        }
    }
}
