/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.rabbitmq;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.base.threads.WorkerPool;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that links an Axis2 service to a RabbitMQ AMQP destination. Additionally, it contains
 * all the required information to process incoming AMQP messages and to inject them
 * into Axis2.
 */

public class RabbitMQEndpoint extends ProtocolEndpoint {

    private final WorkerPool workerPool;
    private final RabbitMQListener rabbitMQListener;

    private ConnectionFactory connectionFactory;
    private Set<EndpointReference> endpointReferences = new HashSet<EndpointReference>();
    private ServiceTaskManager serviceTaskManager;

    /**
     * Create a rabbit mq endpoint
     * @param rabbitMQListener listener listening for messages from this EP
     * @param workerPool worker pool to create service task manager
     */
    public RabbitMQEndpoint(RabbitMQListener rabbitMQListener, WorkerPool workerPool) {
        this.rabbitMQListener = rabbitMQListener;
        this.workerPool = workerPool;
    }

    @Override
    public EndpointReference[] getEndpointReferences(AxisService service, String ip) {
        return endpointReferences.toArray(new EndpointReference[endpointReferences.size()]);
    }

    /**
     * Get connection factory of the EP
     * @return connection factory set to this EP
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Get service task manager for this EP
     * @return service task manager set for this EP
     */
    public ServiceTaskManager getServiceTaskManager() {
        return serviceTaskManager;
    }

    /**
     * Set service task manager for the EP
     * @param serviceTaskManager  service task manager to be set
     */
    public void setServiceTaskManager(ServiceTaskManager serviceTaskManager) {
        this.serviceTaskManager = serviceTaskManager;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        // We only support endpoints configured at service level
        if (!(params instanceof AxisService)) {
            return false;
        }

        AxisService service = (AxisService) params;

        connectionFactory = rabbitMQListener.getConnectionFactory(service);
        if (connectionFactory == null) {
            return false;
        }

        serviceTaskManager = ServiceTaskManagerFactory.
                createTaskManagerForService(connectionFactory, service, workerPool);
        serviceTaskManager.setRabbitMQMessageReceiver(new RabbitMQMessageReceiver(rabbitMQListener,
                                                                                  connectionFactory, this));

        return true;
    }
}
