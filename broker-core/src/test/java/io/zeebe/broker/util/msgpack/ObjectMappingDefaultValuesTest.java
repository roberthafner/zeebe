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
package io.zeebe.broker.util.msgpack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static io.zeebe.broker.util.msgpack.MsgPackUtil.encodeMsgPack;
import static io.zeebe.broker.util.msgpack.MsgPackUtil.utf8;
import static io.zeebe.test.util.BufferAssert.assertThatBuffer;

import java.util.Map;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import io.zeebe.broker.util.msgpack.POJO.POJOEnum;
import io.zeebe.msgpack.spec.MsgPackReader;
import org.junit.Test;

public class ObjectMappingDefaultValuesTest
{

    @Test
    public void shouldReturnDefaultValueForMissingProperty()
    {
        // given
        final MutableDirectBuffer msgPackBuffer = encodeMsgPack((w) ->
        {
            w.writeMapHeader(1);
            w.writeString(utf8("noDefaultValueProp"));
            w.writeInteger(123123L);
        });

        final long defaultValue = -1L;
        final DefaultValuesPOJO pojo = new DefaultValuesPOJO(defaultValue);

        // when
        pojo.wrap(msgPackBuffer);

        // then
        assertThat(pojo.getNoDefaultValueProperty()).isEqualTo(123123L);
        assertThat(pojo.getDefaultValueProperty()).isEqualTo(defaultValue);
    }

    @Test
    public void shouldNotReturnDefaultValueForExistingProperty()
    {
        // given
        final MutableDirectBuffer msgPackBuffer = encodeMsgPack((w) ->
        {
            w.writeMapHeader(2);
            w.writeString(utf8("noDefaultValueProp"));
            w.writeInteger(123123L);
            w.writeString(utf8("defaultValueProp"));
            w.writeInteger(987L);
        });

        final long defaultValue = -1L;
        final DefaultValuesPOJO pojo = new DefaultValuesPOJO(defaultValue);

        // when
        pojo.wrap(msgPackBuffer);

        // then
        assertThat(pojo.getNoDefaultValueProperty()).isEqualTo(123123L);
        assertThat(pojo.getDefaultValueProperty()).isEqualTo(987L);
    }

    @Test
    public void shouldReturnDefaultValueAfterReset()
    {
        // given
        final MutableDirectBuffer msgPackBuffer = encodeMsgPack((w) ->
        {
            w.writeMapHeader(2);
            w.writeString(utf8("noDefaultValueProp"));
            w.writeInteger(123123L);
            w.writeString(utf8("defaultValueProp"));
            w.writeInteger(987L);
        });

        final long defaultValue = -1L;
        final DefaultValuesPOJO pojo = new DefaultValuesPOJO(defaultValue);
        pojo.wrap(msgPackBuffer);

        // when
        pojo.reset();

        // then
        assertThat(pojo.getDefaultValueProperty()).isEqualTo(defaultValue);
    }

    /**
     * Default values should be written. Use case: we read a message of version 1 and always
     * write it in version 2, where a new property should be included.
     */
    @Test
    public void shouldWriteDefaultValue()
    {
        // given
        final long defaultValue = -1L;
        final DefaultValuesPOJO pojo = new DefaultValuesPOJO(defaultValue);
        pojo.setNoDefaultValueProperty(123123L);

        final UnsafeBuffer buf = new UnsafeBuffer(new byte[pojo.getLength()]);

        // when
        pojo.write(buf, 0);

        // then
        final MsgPackReader reader = new MsgPackReader();
        reader.wrap(buf, 0, buf.capacity());
        final Map<String, Object> msgPackMap = MsgPackUtil.asMap(buf, 0, buf.capacity());

        assertThat(msgPackMap).hasSize(2);
        assertThat(msgPackMap).contains(
                entry("noDefaultValueProp", 123123L),
                entry("defaultValueProp", defaultValue)
        );
    }

    @Test
    public void shouldSupportDefaultValuesForAllPropertyTypes()
    {
        // given
        final MutableDirectBuffer msgPackBuffer = encodeMsgPack((w) ->
        {
            w.writeMapHeader(0);
        });

        final MutableDirectBuffer packedMsgPackBuffer = encodeMsgPack((w) ->
        {
            w.writeMapHeader(1);
            w.writeInteger(123L);
            w.writeInteger(456L);
        });

        final AllTypesDefaultValuesPOJO pojo = new AllTypesDefaultValuesPOJO(
                POJOEnum.FOO,
                654L,
                123,
                "defaultString",
                packedMsgPackBuffer,
                utf8("defaultBinary"),
                new POJONested().setLong(12L)
                );

        // when
        pojo.wrap(msgPackBuffer);

        // then
        assertThat(pojo.getEnum()).isEqualTo(POJOEnum.FOO);
        assertThat(pojo.getLong()).isEqualTo(654L);
        assertThat(pojo.getInt()).isEqualTo(123);
        assertThatBuffer(pojo.getString()).hasBytes(utf8("defaultString"));
        assertThatBuffer(pojo.getPacked()).hasBytes(packedMsgPackBuffer);
        assertThatBuffer(pojo.getBinary()).hasBytes(utf8("defaultBinary"));
        assertThat(pojo.getNestedObject().getLong()).isEqualTo(12L);
    }

}
