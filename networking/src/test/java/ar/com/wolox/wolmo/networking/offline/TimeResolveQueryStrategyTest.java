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
package ar.com.wolox.wolmo.networking.offline;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;

public class TimeResolveQueryStrategyTest {

    private static final int REFRESH_DELTA = 10;

    private TimeResolveQueryStrategy<String, String> mTimeResolveQueryStrategySpy;

    @Before
    public void beforeTest() {
        mTimeResolveQueryStrategySpy = spy(new TimeResolveQueryStrategy<String, String>(REFRESH_DELTA) {

            @Override
            public void invalidate(@NonNull String cache) {
            }

            @Override
            public String cleanReadLocalSource(@NonNull String cache) {
                return "CleanRead";
            }

            @Override
            public void refresh(@NonNull String data, @NonNull String cache) {
            }
        });
    }

    @Test
    public void cacheReadFirstTime() throws Exception {
        Thread.sleep(REFRESH_DELTA + 5);
        assertThat(mTimeResolveQueryStrategySpy.readLocalSource("Cache")).isNull();
        verify(mTimeResolveQueryStrategySpy, times(1)).invalidate(eq("Cache"));
    }

    @Test
    public void cacheReadCleanCache() throws Exception {
        Thread.sleep(REFRESH_DELTA + 5);
        assertThat(mTimeResolveQueryStrategySpy.readLocalSource("Cache")).isNull();
        verify(mTimeResolveQueryStrategySpy, times(1)).invalidate(eq("Cache"));

        // Refresh cache
        mTimeResolveQueryStrategySpy.consumeRemoteSource("Data", "RemoteCache");

        // Do not invalidate again
        assertThat(mTimeResolveQueryStrategySpy.readLocalSource("Cache")).isEqualTo("CleanRead");
        verify(mTimeResolveQueryStrategySpy, times(1)).refresh(eq("Data"), eq("RemoteCache"));
        verify(mTimeResolveQueryStrategySpy, times(1)).invalidate(eq("Cache"));
        verify(mTimeResolveQueryStrategySpy, times(1)).cleanReadLocalSource(eq("Cache"));
    }
}
