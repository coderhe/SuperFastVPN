/*
 ** Copyright 2015, Mohamed Naufal
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **
 ** http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.android.ppp.data;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ByteBufferPool
{
    private static final int BUFFER_SIZE = 16384;
    private static ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<ByteBuffer>();

    public static ByteBuffer acquire()
    {
        synchronized (pool)
        {
            ByteBuffer buffer = pool.poll();
            if (buffer == null)
            {
                buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            }
            buffer.clear();

            return buffer;
        }
    }

    public static void release(ByteBuffer buffer)
    {
        synchronized (pool)
        {
            buffer.clear();
        }
    }

    public static void clear()
    {
        synchronized (pool)
        {
            pool.clear();
        }
    }
}