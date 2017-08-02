/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.clustering.raft;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.zeebe.logstreams.log.LogStream;
import io.zeebe.raft.RaftPersistentStorage;
import io.zeebe.transport.SocketAddress;
import io.zeebe.util.LangUtil;
import io.zeebe.util.StreamUtil;
import io.zeebe.util.allocation.AllocatedBuffer;
import io.zeebe.util.allocation.DirectBufferAllocator;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class RaftPersistentFileStorage implements RaftPersistentStorage
{

    private final RaftPersistentState state = new RaftPersistentState();
    private final DirectBufferAllocator allocator = new DirectBufferAllocator();

    private final LogStream logStream;
    private final SocketAddress votedFor = new SocketAddress();

    private final File file;
    private final File tmpFile;
    private final Path path;
    private final Path tmpPath;

    private AllocatedBuffer allocatedWriteBuffer;
    private final MutableDirectBuffer writeBuffer = new UnsafeBuffer(0, 0);

    private AllocatedBuffer allocatedReadBuffer;
    private final MutableDirectBuffer readBuffer = new UnsafeBuffer(0, 0);



    public RaftPersistentFileStorage(final LogStream logStream, final String filename)
    {
        this.logStream = logStream;

        file = new File(filename);
        tmpFile = new File(filename + ".tmp");
        path = Paths.get(filename);
        tmpPath = Paths.get(filename + ".tmp");

        load();
    }

    @Override
    public int getTerm()
    {
        return logStream.getTerm();
    }

    @Override
    public void setTerm(final int term)
    {
        logStream.setTerm(term);

        state.setTerm(term);

        save();
    }

    @Override
    public SocketAddress getVotedFor()
    {
        if (votedFor.hostLength() > 0)
        {
            return votedFor;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setVotedFor(final SocketAddress votedFor)
    {
        if (votedFor != null)
        {
            this.votedFor.wrap(votedFor);
        }
        else
        {
            this.votedFor.reset();
        }

        save();
    }

    protected void load()
    {
        if (file.exists())
        {
            final long length = file.length();
            if (length > readBuffer.capacity())
            {
                allocateReadBuffer((int) length);
            }

            try (InputStream is = new FileInputStream(file))
            {
                StreamUtil.read(is, readBuffer.byteArray());
            }
            catch (final IOException e)
            {
                LangUtil.rethrowUnchecked(e);
            }

            state.wrap(readBuffer);
        }
    }

    protected void save()
    {
        final int length = state.getEncodedLength();

        if (length > writeBuffer.capacity())
        {
            allocateWriteBuffer(length);
        }

        state.write(writeBuffer, 0);

        try (OutputStream os = new FileOutputStream(tmpFile))
        {
            os.write(writeBuffer.byteArray(), 0, length);
            os.flush();

        }
        catch (final IOException e)
        {
            LangUtil.rethrowUnchecked(e);
        }

        try
        {
            Files.move(tmpPath, path, REPLACE_EXISTING);
        }
        catch (final IOException e)
        {
            LangUtil.rethrowUnchecked(e);
        }
    }

    private void allocateWriteBuffer(final int capacity)
    {
        if (allocatedWriteBuffer != null)
        {
            allocatedWriteBuffer.close();
        }

        allocatedWriteBuffer = allocator.allocate(capacity);
        writeBuffer.wrap(allocatedWriteBuffer.getRawBuffer());
    }

    private void allocateReadBuffer(final int capacity)
    {
        if (allocatedReadBuffer != null)
        {
            allocatedReadBuffer.close();
        }

        allocatedReadBuffer = allocator.allocate(capacity);
        readBuffer.wrap(allocatedReadBuffer.getRawBuffer());
    }

}
