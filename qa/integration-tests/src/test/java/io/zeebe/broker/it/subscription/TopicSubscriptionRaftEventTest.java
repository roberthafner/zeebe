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
package io.zeebe.broker.it.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.broker.it.ClientRule;
import io.zeebe.broker.it.EmbeddedBrokerRule;
import io.zeebe.broker.it.subscription.RecordingEventHandler.RecordedEvent;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.TopicEventType;
import io.zeebe.client.event.TopicSubscription;
import io.zeebe.test.util.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;

public class TopicSubscriptionRaftEventTest
{
    public static final String SUBSCRIPTION_NAME = "subscription";

    public EmbeddedBrokerRule brokerRule = new EmbeddedBrokerRule();

    public ClientRule clientRule = new ClientRule();

    @Rule
    public RuleChain ruleChain = RuleChain
        .outerRule(brokerRule)
        .around(clientRule);

    @Rule
    public Timeout timeout = Timeout.seconds(10);

    protected ZeebeClient client;
    protected RecordingEventHandler recordingHandler;
    protected ObjectMapper objectMapper;

    @Before
    public void setUp()
    {
        this.client = clientRule.getClient();
        this.recordingHandler = new RecordingEventHandler();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * There should always be one raft events on each log by default:
     * One that marks that this broker is the leader for this log.
     */
    @Test
    public void shouldReceiveNoopEvents()
    {
        // given
        final TopicSubscription subscription =
            clientRule.topic()
                      .newSubscription()
                      .startAtHeadOfTopic()
                      .handler(recordingHandler)
                      .name(SUBSCRIPTION_NAME)
                      .open();

        // when
        TestUtil.waitUntil(() -> recordingHandler.numRecordedNoopEvents() == 1, 100);

        // then
        final List<RecordedEvent> noopEvents = recordingHandler.getRecordedEvents()
                .stream()
                .filter((re) -> re.getMetadata().getEventType() == TopicEventType.NOOP)
                .collect(Collectors.toList());

        assertThat(isJsonObject(noopEvents.get(0).getEvent().getJson())).isTrue();

        subscription.close();
    }

    // TODO(menski): write a test which creates raft events, requires multiple brokers

    protected boolean isJsonObject(String json)
    {
        try
        {
            return json != null && objectMapper.readTree(json).isObject();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
