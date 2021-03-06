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
package io.zeebe.test.broker.protocol.brokerapi;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.zeebe.test.broker.protocol.MsgPackHelper;
import io.zeebe.test.util.collection.MapFactoryBuilder;

public class ExecuteCommandResponseBuilder
{

    protected final Consumer<ResponseStub<ExecuteCommandRequest>> registrationFunction;
    protected final ExecuteCommandResponseWriter commandResponseWriter;
    protected final Predicate<ExecuteCommandRequest> activationFunction;

    public ExecuteCommandResponseBuilder(
            Consumer<ResponseStub<ExecuteCommandRequest>> registrationFunction,
            MsgPackHelper msgPackConverter,
            Predicate<ExecuteCommandRequest> activationFunction)
    {
        this.registrationFunction = registrationFunction;
        this.commandResponseWriter = new ExecuteCommandResponseWriter(msgPackConverter);
        this.activationFunction = activationFunction;
    }

    public ExecuteCommandResponseBuilder topicName(final String topicName)
    {
        return topicName((r) -> topicName);
    }


    public ExecuteCommandResponseBuilder topicName(Function<ExecuteCommandRequest, String> topicNameFunction)
    {
        commandResponseWriter.setTopicNameFunction(topicNameFunction);
        return this;
    }

    public ExecuteCommandResponseBuilder partitionId(final int partitionId)
    {
        return partitionId((r) -> partitionId);
    }


    public ExecuteCommandResponseBuilder partitionId(Function<ExecuteCommandRequest, Integer> partitionIdFunction)
    {
        commandResponseWriter.setPartitionIdFunction(partitionIdFunction);
        return this;
    }

    public ExecuteCommandResponseBuilder key(long l)
    {
        return key((r) -> l);
    }

    public ExecuteCommandResponseBuilder key(Function<ExecuteCommandRequest, Long> keyFunction)
    {
        commandResponseWriter.setKeyFunction(keyFunction);
        return this;
    }


    public ExecuteCommandResponseBuilder event(Map<String, Object> map)
    {
        commandResponseWriter.setEventFunction((re) -> map);
        return this;
    }

    public MapFactoryBuilder<ExecuteCommandRequest, ExecuteCommandResponseBuilder> event()
    {
        return new MapFactoryBuilder<>(this, commandResponseWriter::setEventFunction);
    }

    public void register()
    {
        registrationFunction.accept(new ResponseStub<>(activationFunction, commandResponseWriter));
    }
}
