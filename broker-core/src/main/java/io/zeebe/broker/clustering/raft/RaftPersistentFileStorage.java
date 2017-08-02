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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.zeebe.logstreams.log.LogStream;
import io.zeebe.raft.RaftPersistentStorage;
import io.zeebe.raft.event.RaftConfiguration;
import io.zeebe.transport.SocketAddress;

public class RaftPersistentFileStorage implements RaftPersistentStorage
{

    private final RaftConfiguration configuration = new RaftConfiguration();

    private final LogStream logStream;
    private final SocketAddress votedFor = new SocketAddress();

    public RaftPersistentFileStorage(final LogStream logStream)
    {
        this.logStream = logStream;
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
    }

    protected void store()
    {
        final int length = configuration.getEncodedLength();

        if (length > writeBuffer.capacity())
        {
            writeBuffer.wrap(new byte[length]);
        }

        meta.write(writeBuffer, 0);

        final File target = new File(String.format("%s.bak", file));
        try (OutputStream os = new FileOutputStream(target))
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
            final Path sourcePath = Paths.get(String.format("%s", file));
            final Path backupPath = Paths.get(String.format("%s.bak", file));
            Files.move(backupPath, sourcePath, REPLACE_EXISTING);
        }
        catch (final IOException e)
        {
            LangUtil.rethrowUnchecked(e);
        }
    }

}
