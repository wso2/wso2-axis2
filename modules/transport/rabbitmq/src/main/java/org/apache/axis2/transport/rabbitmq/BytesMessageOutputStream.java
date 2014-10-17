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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * Output stream wrapper that writes the message content to RabbitMQ out channel
 */
public class BytesMessageOutputStream extends OutputStream {

    private final ByteArrayOutputStream bos = null;
    private final Channel channel;
    private final String exchangeName;
    private final String queueName;
    private final AMQP.BasicProperties basicProperties;

    public BytesMessageOutputStream(Channel channel, String queueName, String exchangeName,
                                    AMQP.BasicProperties basicProperties) {
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.basicProperties = basicProperties;
    }

    @Override
    public void write(int i) throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        bos.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        bos.write(b);
    }
}
