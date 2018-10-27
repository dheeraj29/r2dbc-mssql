/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.mssql.codec;

import io.netty.buffer.ByteBuf;
import io.r2dbc.mssql.message.type.TypeInformation;
import io.r2dbc.mssql.message.type.TypeInformation.LengthStrategy;
import io.r2dbc.mssql.message.type.TypeInformation.SqlServerType;
import io.r2dbc.mssql.util.EncodedAssert;
import io.r2dbc.mssql.util.HexUtils;
import io.r2dbc.mssql.util.TestByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ZonedDateTimeCodec}.
 *
 * @author Mark Paluch
 */
class ZonedDateTimeCodecUnitTests {

    static final TypeInformation DATETIMEOFFSET = TypeInformation.builder().withLengthStrategy(LengthStrategy.BYTELENTYPE).withScale(5).withServerType(SqlServerType.DATETIMEOFFSET).build();

    @Test
    void shouldDecodeDateTimeOffset() {

        ByteBuf buffer = HexUtils.decodeToByteBuf("0AA026314194A43E0B2D00");

        ZonedDateTime decoded = ZonedDateTimeCodec.INSTANCE.decode(buffer, ColumnUtil.createColumn(DATETIMEOFFSET), ZonedDateTime.class);

        assertThat(decoded).isEqualTo("2018-08-27T17:41:14.890+00:45[UT+00:45]");
    }

    @Test
    void shouldEncodeSmallDateTime() {

        ZonedDateTime value = ZonedDateTime.parse("2018-08-27T17:41:14.890+00:45[UT+00:45]");

        ByteBuf encoded = ZonedDateTimeCodec.INSTANCE.encode(TestByteBufAllocator.TEST, DATETIMEOFFSET, value);
        EncodedAssert.assertThat(encoded).isEqualToHex("0AA026314194A43E0B2D00");
    }
}