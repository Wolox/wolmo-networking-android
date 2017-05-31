/**
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

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

/**
 * Transforms a serialized {@link JsonElement} representing a {@link java.util.Date} into
 * a {@link LocalDate} instance. It also works the other way around, serializing a {@link LocalDate}
 * into a {@link JsonElement}.
 * <p>
 * This class is useful for sending and receiving {@link java.util.Date} instances over the network.
 */
public class LocalDateSerializer implements JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Transforms a {@link JsonElement} representing a {@link java.util.Date} into a
     * {@link LocalDate} class from the JodaTime library.
     * This is useful for receiving data over the network.
     *
     * @param element a {@link JsonElement} representing a {@link java.util.Date}
     * @return returns an instance of {@link LocalDate} representing a {@link java.util.Date}
     */
    @Override
    public LocalDate deserialize(JsonElement element, Type type,
                                 JsonDeserializationContext context) throws JsonParseException {
        String date = element.toString();
        date = date.substring(1, date.length() - 1);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(getDateFormat());
        return dtf.parseLocalDate(date);
    }

    /**
     * Transforms and instance of {@link LocalDate} from the JodaTime library into a
     * {@link JsonElement}. This is useful for sending the data over the network.
     *
     * @param date an instance of {@link LocalDate} to be serialized into a {@link JsonElement}
     * @return an instance of {@link JsonElement} representing a {@link LocalDate}
     */
    @Override
    public JsonElement serialize(LocalDate date, Type type, JsonSerializationContext context) {
        return new JsonParser().parse(date.toString());
    }

    /**
     * Override if needed.
     * This method returns the format of the {@link java.util.Date} class that is serialized.
     * Usually, this should match the format that is being received from the API over the network.
     * <p>
     * The default return value is the constant DEFAULT_DATE_FORMAT, available in this class.
     *
     * @return returns the format of the serialized {@link java.util.Date}
     */
    @NonNull
    protected String getDateFormat() {
        return DEFAULT_DATE_FORMAT;
    }
}