/*
 * MIT License
 * <p>
 * Copyright (c) 2017 Wolox S.A
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package ar.com.wolox.wolmo.networking.retrofit.serializer;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

public class LocalDateSerializerTest {

    private LocalDateSerializer mLocalDateSerializer;

    private JsonDeserializationContext mDeserializationContextMock;
    private JsonSerializationContext mSerializationContextMock;
    private Type mTypeMock;

    @Before
    public void beforeTest() {
        mLocalDateSerializer = new LocalDateSerializer();
        mDeserializationContextMock = mock(JsonDeserializationContext.class);
        mSerializationContextMock = mock(JsonSerializationContext.class);
        mTypeMock = mock(Type.class);
    }


    @Test
    public void defaultFormatDeserialize() {
        JsonElement jsonElementMock = mock(JsonElement.class);
        when(jsonElementMock.toString()).thenReturn("2000-01-23");

        LocalDate localDate = mLocalDateSerializer.deserialize(jsonElementMock, mTypeMock,
                mDeserializationContextMock);
        assertThat(localDate).isEqualTo(new LocalDate("2000-01-23"));
    }

    @Test
    public void defaultFormatDeserializeWithBadFormattedDate() {
        JsonElement jsonElementMock = mock(JsonElement.class);
        when(jsonElementMock.toString()).thenReturn("\"2000-01-23\"");

        LocalDate localDate = mLocalDateSerializer.deserialize(jsonElementMock, mTypeMock,
                mDeserializationContextMock);
        assertThat(localDate).isEqualTo(new LocalDate("2000-01-23"));
    }

    @Test
    public void customFormatDeserializeDate() {
        mLocalDateSerializer = new LocalDateSerializer() {
            @NonNull
            @Override
            protected String getDateFormat() {
                return "MM-yyyy-dd";
            }
        };

        JsonElement jsonElementMock = mock(JsonElement.class);
        when(jsonElementMock.toString()).thenReturn("01-2000-23");

        LocalDate localDate = mLocalDateSerializer.deserialize(jsonElementMock, mTypeMock,
                mDeserializationContextMock);
        assertThat(localDate).isEqualTo(new LocalDate("2000-01-23"));
    }

    @Test
    public void defaultFormatSerializeDate() {
        LocalDate localDate = new LocalDate("2000-01-23");

        JsonElement serializedDate = mLocalDateSerializer.serialize(localDate, mTypeMock,
                mSerializationContextMock);
        assertThat(serializedDate.getAsString()).isEqualTo("2000-01-23");
    }

    @Test
    public void customFormatSerializeDate() {
        LocalDate localDate = new LocalDate("2000-01-23");

        mLocalDateSerializer = new LocalDateSerializer() {
            @NonNull
            @Override
            protected String getDateFormat() {
                return "MM-yyyy-dd";
            }
        };

        JsonElement serializedDate = mLocalDateSerializer.serialize(localDate, mTypeMock,
                mSerializationContextMock);
        assertThat(serializedDate.getAsString()).isEqualTo("01-2000-23");
    }
}