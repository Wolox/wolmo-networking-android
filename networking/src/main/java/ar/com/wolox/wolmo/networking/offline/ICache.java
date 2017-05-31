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

package ar.com.wolox.wolmo.networking.offline;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Interface, which resembles a CRUD (Create, Read, Update & Delete), for querying cached data.
 */
public interface ICache {

    /**
     * Stores data of class {@link T}.
     *
     * @param clazz class of the data
     * @param data  to store
     */
    <T> void save(@NonNull Class<T> clazz, @NonNull T data);

    /**
     * @param clazz  class of the data
     * @param key    to identify the object
     * @param update to apply to the data
     * @return updated data. <code>null</code> if there was no information to update.
     */
    @Nullable
    <T> T update(@NonNull Class<T> clazz, @NonNull Object key, IUpdate<T> update);

    /**
     * @param clazz class of the data to read
     * @param key   to identify the object
     * @return data found. <code>null</code> if there was none.
     */
    @Nullable
    <T> T read(@NonNull Class<T> clazz, @NonNull Object key);

    /**
     * Clears data of class {@link T}, identified with a key, from cache.
     *
     * @param clazz target {@link Class<T>}
     * @param key   to identify the object
     */
    <T> void clear(@NonNull Class<T> clazz, @NonNull Object key);

}
