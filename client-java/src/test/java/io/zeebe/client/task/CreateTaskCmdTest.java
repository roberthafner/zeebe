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
package io.zeebe.client.task;

import static io.zeebe.protocol.clientapi.EventType.TASK_EVENT;
import static io.zeebe.test.broker.protocol.clientapi.ClientApiRule.DEFAULT_PARTITION_ID;
import static io.zeebe.test.broker.protocol.clientapi.ClientApiRule.DEFAULT_TOPIC_NAME;
import static io.zeebe.util.StringUtil.getBytes;
import static io.zeebe.util.VarDataUtil.readBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.client.impl.ClientCommandManager;
import io.zeebe.client.impl.Topic;
import io.zeebe.client.impl.cmd.ClientResponseHandler;
import io.zeebe.client.impl.data.MsgPackConverter;
import io.zeebe.client.task.impl.*;
import io.zeebe.protocol.clientapi.*;
import org.agrona.ExpandableArrayBuffer;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class CreateTaskCmdTest
{
    protected static final String TOPIC_NAME = "test-topic";
    protected static final int PARTITION_ID = 1;

    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final ExecuteCommandRequestDecoder requestDecoder = new ExecuteCommandRequestDecoder();
    private final ExecuteCommandResponseEncoder responseEncoder = new ExecuteCommandResponseEncoder();
    private final MsgPackConverter msgPackConverter = new MsgPackConverter();

    private final ExpandableArrayBuffer writeBuffer = new ExpandableArrayBuffer();

    private CreateTaskCmdImpl createTaskCommand;
    private ObjectMapper objectMapper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup()
    {
        final ClientCommandManager commandManager = mock(ClientCommandManager.class);

        objectMapper = new ObjectMapper(new MessagePackFactory());

        createTaskCommand = new CreateTaskCmdImpl(commandManager, objectMapper, msgPackConverter, new Topic(TOPIC_NAME, PARTITION_ID));
    }

    @Test
    public void shouldWriteRequest() throws JsonParseException, JsonMappingException, IOException
    {
        // given
        createTaskCommand
            .taskType("foo")
            .retries(4)
            .addHeader("a", "b")
            .addHeader("c", "d")
            .payload("{ \"bar\" : 4 }");

        // when
        createTaskCommand.writeCommand(writeBuffer);

        // then
        headerDecoder.wrap(writeBuffer, 0);

        assertThat(headerDecoder.schemaId()).isEqualTo(requestDecoder.sbeSchemaId());
        assertThat(headerDecoder.version()).isEqualTo(requestDecoder.sbeSchemaVersion());
        assertThat(headerDecoder.templateId()).isEqualTo(requestDecoder.sbeTemplateId());
        assertThat(headerDecoder.blockLength()).isEqualTo(requestDecoder.sbeBlockLength());

        requestDecoder.wrap(writeBuffer, headerDecoder.encodedLength(), requestDecoder.sbeBlockLength(), requestDecoder.sbeSchemaVersion());

        assertThat(requestDecoder.eventType()).isEqualTo(TASK_EVENT);
        assertThat(requestDecoder.topicName()).isEqualTo(TOPIC_NAME);
        assertThat(requestDecoder.partitionId()).isEqualTo(PARTITION_ID);

        final byte[] command = readBytes(requestDecoder::getCommand, requestDecoder::commandLength);

        final TaskEvent taskEvent = objectMapper.readValue(command, TaskEvent.class);

        assertThat(taskEvent.getEventType()).isEqualTo(TaskEventType.CREATE);
        assertThat(taskEvent.getType()).isEqualTo("foo");
        assertThat(taskEvent.getRetries()).isEqualTo(4);
        assertThat(taskEvent.getHeaders()).hasSize(2).containsEntry("a", "b").containsEntry("c", "d");
        assertThat(taskEvent.getPayload()).isEqualTo(msgPackConverter.convertToMsgPack("{ \"bar\" : 4 }"));
    }

    @Test
    public void shouldWriteRequestWithPayloadAsStream() throws JsonParseException, JsonMappingException, IOException
    {
        // given
        final byte[] payload = getBytes("{ \"bar\" : 4 }");

        final Map<String, Object> headers = new HashMap<>();
        headers.put("a", "b");
        headers.put("c", "d");

        createTaskCommand
            .taskType("foo")
            .setHeaders(headers)
            .payload(new ByteArrayInputStream(payload));

        // when
        createTaskCommand.writeCommand(writeBuffer);

        // then
        requestDecoder.wrap(writeBuffer, headerDecoder.encodedLength(), requestDecoder.sbeBlockLength(), requestDecoder.sbeSchemaVersion());

        // skip topic name
        requestDecoder.topicName();

        final byte[] command = readBytes(requestDecoder::getCommand, requestDecoder::commandLength);

        final TaskEvent taskEvent = objectMapper.readValue(command, TaskEvent.class);

        assertThat(taskEvent.getHeaders()).hasSize(2).containsAllEntriesOf(headers);
        assertThat(taskEvent.getPayload()).isEqualTo(msgPackConverter.convertToMsgPack(new ByteArrayInputStream(payload)));
    }

    @Test
    public void shouldWriteRequestWithDefaultRetries() throws JsonParseException, JsonMappingException, IOException
    {
        // given
        createTaskCommand
            .taskType("foo");

        // when
        createTaskCommand.writeCommand(writeBuffer);

        // then
        requestDecoder.wrap(writeBuffer, headerDecoder.encodedLength(), requestDecoder.sbeBlockLength(), requestDecoder.sbeSchemaVersion());

        // skip topic name
        requestDecoder.topicName();

        final byte[] command = readBytes(requestDecoder::getCommand, requestDecoder::commandLength);

        final TaskEvent taskEvent = objectMapper.readValue(command, TaskEvent.class);

        assertThat(taskEvent.getRetries()).isEqualTo(3);
    }

    @Test
    public void shouldReadResponse() throws JsonProcessingException
    {
        final ClientResponseHandler<Long> responseHandler = createTaskCommand.getResponseHandler();

        assertThat(responseHandler.getResponseSchemaId()).isEqualTo(responseEncoder.sbeSchemaId());
        assertThat(responseHandler.getResponseTemplateId()).isEqualTo(responseEncoder.sbeTemplateId());

        responseEncoder.wrap(writeBuffer, 0);

        // given
        final Map<String, Object> headers = new HashMap<>();
        headers.put("k", "v");

        final TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventType(TaskEventType.CREATED);
        taskEvent.setType("foo");
        taskEvent.setHeaders(headers);
        taskEvent.setPayload(msgPackConverter.convertToMsgPack("{ \"bar\" : 4 }"));

        final byte[] jsonEvent = objectMapper.writeValueAsBytes(taskEvent);

        responseEncoder
            .topicName(DEFAULT_TOPIC_NAME)
            .partitionId(DEFAULT_PARTITION_ID)
            .key(2L)
            .putEvent(jsonEvent, 0, jsonEvent.length);

        // when
        final Long taskKey = responseHandler.readResponse(writeBuffer, 0, responseEncoder.sbeBlockLength(), responseEncoder.sbeSchemaVersion());

        // then
        assertThat(taskKey).isEqualTo(2L);
    }

    @Test
    public void shouldBeNotValidIfTaskTypeIsNotSet()
    {
        createTaskCommand
            .addHeader("k", "v")
            .payload("{ \"bar\" : 4 }");

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("task type must not be null");

        createTaskCommand.validate();
    }

    @Test
    public void shouldBeNotValidIfTaskTypeIsEmpty()
    {
        createTaskCommand
            .taskType("")
            .addHeader("k", "v")
            .payload("{ \"bar\" : 4 }");

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("task type must not be empty");

        createTaskCommand.validate();
    }

    @Test
    public void shouldBeNotValidIfRetriesLessThanZero()
    {
        createTaskCommand
            .taskType("foo")
            .retries(-1);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("retries must be greater than or equal to 0");

        createTaskCommand.validate();
    }

}
