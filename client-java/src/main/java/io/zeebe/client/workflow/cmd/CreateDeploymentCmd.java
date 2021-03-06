/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.client.workflow.cmd;

import java.io.InputStream;

import io.zeebe.client.cmd.ClientCommand;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public interface CreateDeploymentCmd extends ClientCommand<DeploymentResult>
{
    /**
     * Add the given workflow XML as bytes to the deployment.
     */
    CreateDeploymentCmd resourceBytes(byte[] resourceBytes);

    /**
     * Add the given workflow XML as String to the deployment.
     */
    CreateDeploymentCmd resourceString(String resourceString);

    /**
     * Add the given workflow XML as stream to the deployment.
     */
    CreateDeploymentCmd resourceStream(InputStream resourceStream);

    /**
     * Add the given workflow XML as classpath resource to the deployment.
     */
    CreateDeploymentCmd resourceFromClasspath(String classpathResource);

    /**
     * Add the given workflow XML as file to the deployment.
     */
    CreateDeploymentCmd resourceFile(String filename);

    /**
     * Add the given workflow as model instance to the deployment.
     */
    CreateDeploymentCmd bpmnModelInstance(BpmnModelInstance modelInstance);
}
