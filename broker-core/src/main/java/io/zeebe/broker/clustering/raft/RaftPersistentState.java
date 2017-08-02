package io.zeebe.broker.clustering.raft;

import io.zeebe.broker.clustering.handler.BrokerAddress;
import io.zeebe.msgpack.UnpackedObject;
import io.zeebe.msgpack.property.ArrayProperty;
import io.zeebe.msgpack.property.IntegerProperty;
import io.zeebe.msgpack.property.StringProperty;
import io.zeebe.msgpack.spec.MsgPackHelper;
import io.zeebe.msgpack.value.ArrayValue;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class RaftPersistentState extends UnpackedObject
{
    private static final DirectBuffer EMPTY_ARRAY = new UnsafeBuffer(MsgPackHelper.EMPTY_ARRAY);
    private static final DirectBuffer EMPTY_STRING = new UnsafeBuffer(0, 0);

    protected StringProperty topicNameProp = new StringProperty("topicName", "");
    protected IntegerProperty partitionIdProp = new IntegerProperty("partitionId", -1);
    protected StringProperty directoryProp = new StringProperty("logDirectory", "");
    protected IntegerProperty termProp = new IntegerProperty("term", 0);
    protected StringProperty voteForHostProp = new StringProperty("voteForHost", "");
    protected IntegerProperty voteForPortProp = new IntegerProperty("voteForPort", 0);

    protected ArrayProperty<BrokerAddress> membersProp = new ArrayProperty<>("members",
        new ArrayValue<>(),
        new ArrayValue<>(EMPTY_ARRAY, 0, EMPTY_ARRAY.capacity()), new BrokerAddress());

    public RaftPersistentState()
    {
        declareProperty(partitionIdProp);
        declareProperty(topicNameProp);
        declareProperty(directoryProp);
        declareProperty(termProp);
        declareProperty(voteForHostProp);
        declareProperty(voteForPortProp);
        declareProperty(membersProp);
    }


}
