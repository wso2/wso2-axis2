/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.axis2.description;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * Represent a resource definition exposed from a service.
 * Provide similar representation to a resource in an API.
 */
public class AxisResource {

    private Set<String> methods = new HashSet<>(4);
    private Map<String, List<AxisResourceParameter>> resourceParametersMap = new HashMap<>();

    public Set<String> getMethods() {
        return methods;
    }

    public void setMethod(String method) {
        methods.add(method);
    }

    public void addResourceParameter(String method, List<AxisResourceParameter> resourceParameterList) {
        resourceParametersMap.put(method, resourceParameterList);
    }

    public List<AxisResourceParameter> getResourceParameterList(String method) {
        return resourceParametersMap.get(method);
    }
}
