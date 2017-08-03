package io.zeebe.broker.clustering.raft;

import static io.zeebe.util.EnsureUtil.ensureGreaterThan;
import static io.zeebe.util.EnsureUtil.ensureNotNull;
import static io.zeebe.util.buffer.BufferUtil.bufferAsString;

import java.util.ArrayList;
import java.util.List;

import io.zeebe.msgpack.UnpackedObject;
import io.zeebe.msgpack.property.ArrayProperty;
import io.zeebe.msgpack.property.IntegerProperty;
import io.zeebe.msgpack.property.StringProperty;
import io.zeebe.msgpack.spec.MsgPackHelper;
import io.zeebe.msgpack.value.ArrayValue;
import io.zeebe.transport.SocketAddress;
import io.zeebe.util.EnsureUtil;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class RaftConfiguration extends UnpackedObject
{
    private static final DirectBuffer EMPTY_ARRAY = new UnsafeBuffer(MsgPackHelper.EMPTY_ARRAY);
    private static final DirectBuffer EMPTY_STRING = new UnsafeBuffer(0, 0);

    protected StringProperty topicNameProp = new StringProperty("topicName", "");
    protected IntegerProperty partitionIdProp = new IntegerProperty("partitionId", -1);
    protected StringProperty logDirectoryProp = new StringProperty("logDirectory", "");
    protected IntegerProperty termProp = new IntegerProperty("term", 0);
    protected StringProperty votedForHostProp = new StringProperty("votedForHost", "");
    protected IntegerProperty votedForPortProp = new IntegerProperty("votedForPort", 0);

    protected ArrayProperty<RaftConfigurationMember> membersProp = new ArrayProperty<>("members",
        new ArrayValue<>(),
        new ArrayValue<>(EMPTY_ARRAY, 0, EMPTY_ARRAY.capacity()), new RaftConfigurationMember());

    public RaftConfiguration()
    {
        declareProperty(partitionIdProp);
        declareProperty(topicNameProp);
        declareProperty(logDirectoryProp);
        declareProperty(termProp);
        declareProperty(votedForHostProp);
        declareProperty(votedForPortProp);
        declareProperty(membersProp);
    }

    public DirectBuffer getTopicName()
    {
        return topicNameProp.getValue();
    }

    public void setTopicName(final DirectBuffer topicName)
    {
        ensureGreaterThan("Topic name length", topicName.capacity(), 0);
        topicNameProp.setValue(topicName, 0, topicName.capacity());
    }

    public int getPartitionId()
    {
        return partitionIdProp.getValue();
    }

    public void setPartitionId(final int partitionId)
    {
        partitionIdProp.setValue(partitionId);
    }

    public String getLogDirectory()
    {
        return bufferAsString(logDirectoryProp.getValue());
    }

    public void setLogDirectory(final String logDirectory)
    {
        ensureNotNull("Log directory", logDirectory);
        logDirectoryProp.setValue(logDirectory);
    }

    public int getTerm()
    {
        return termProp.getValue();
    }

    public void setTerm(final int term)
    {
        termProp.setValue(term);
    }

    public void getVotedForHostProp(final SocketAddress votedFor)
    {
        votedFor.reset();

        final DirectBuffer votedForValue = votedForHostProp.getValue();
        final int votedForLength = votedForValue.capacity();

        if (votedForLength > 0)
        {
            votedFor.host(votedForValue, 0, votedForLength);
            votedFor.port(votedForPortProp.getValue());
        }
    }

    public void setVotedFor(final SocketAddress votedFor)
    {
        if (votedFor != null)
        {
            votedForHostProp.setValue(votedFor.getHostBuffer(), 0, votedFor.hostLength());
            votedForPortProp.setValue(votedFor.port());
        }
        else
        {
            votedForHostProp.setValue(EMPTY_STRING, 0, 0);
            votedForPortProp.setValue(-1);
        }
    }

    public List<SocketAddress> getMembers()
    {
        final List<SocketAddress> members = new ArrayList<>();

        while (membersProp.hasNext())
        {
            final RaftConfigurationMember configurationMember = membersProp.next();
            final DirectBuffer hostBuffer = configurationMember.getHost();


            final SocketAddress member =
                new SocketAddress()
                    .host(hostBuffer, 0, hostBuffer.capacity())
                    .port(configurationMember.getPort());

            members.add(member);
        }

        return members;
    }

    public void setMembers(final List<SocketAddress> members)
    {
        membersProp.reset();

        for (int i = 0; i < members.size(); i++)
        {
            addMember(members.get(i));
        }
    }

    public void addMember(final SocketAddress member)
    {
        ensureNotNull("Member", member);

        membersProp.add()
                   .setHost(member.getHostBuffer(), 0, member.hostLength())
                   .setPort(member.port());
    }
}
