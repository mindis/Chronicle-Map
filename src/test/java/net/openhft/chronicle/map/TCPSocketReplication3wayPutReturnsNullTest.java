/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.map;

import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test  ReplicatedChronicleMap where the Replicated is over a TCP Socket
 *
 * @author Rob Austin.
 */

public class TCPSocketReplication3wayPutReturnsNullTest {

    private ChronicleMap<Integer, CharSequence> map1;
    private ChronicleMap<Integer, CharSequence> map2;
    private ChronicleMap<Integer, CharSequence> map3;

    static <T extends ChronicleMap<Integer, CharSequence>> T newTcpSocketShmIntString(
            final byte identifier,
            final int serverPort,
            final InetSocketAddress... endpoints) throws IOException {
        TcpTransportAndNetworkConfig tcpConfig = TcpTransportAndNetworkConfig.of(serverPort, Arrays.asList(endpoints))
                .autoReconnectedUponDroppedConnection(true);

        return (T) ChronicleMapBuilder.of(Integer.class, CharSequence.class)
                .putReturnsNull(true)
                .replication(identifier, tcpConfig).create();
    }

    @Before
    public void setup() throws IOException {
        map1 = newTcpSocketShmIntString((byte) 1, 8026, new InetSocketAddress("localhost", 8027),
                new InetSocketAddress("localhost", 8028));
        map2 = newTcpSocketShmIntString((byte) 2, 8027, new InetSocketAddress("localhost", 8028));
        map3 = newTcpSocketShmIntString((byte) 3, 8028);
    }

    @After
    public void tearDown() throws InterruptedException {

        for (final Closeable closeable : new Closeable[]{map1, map2, map3}) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.gc();
    }

    Set<Thread> threads;

    @Before
    public void sampleThreads() {
        threads = Thread.getAllStackTraces().keySet();
    }

    @After
    public void checkThreadsShutdown() {
        StatelessClientTest.checkThreadsShutdown(threads);
    }

    @Test
    public void test3() throws IOException, InterruptedException {

        map3.put(5, "EXAMPLE-2");

        // allow time for the recompilation to resolve
        waitTillEqual(15000);

        assertEquals(map1, map2);
        assertEquals(map3, map2);
        assertTrue(!map1.isEmpty());
    }

    @Test
    public void test() throws IOException, InterruptedException {

        assertEquals(null, map1.put(1, "EXAMPLE-1"));
        assertEquals(null, map1.put(2, "EXAMPLE-2"));
        assertEquals(null, map1.put(2, "EXAMPLE-1"));

        assertEquals(null, map2.put(5, "EXAMPLE-2"));
        assertEquals(null, map2.put(6, "EXAMPLE-2"));

        map1.remove(2);
        map2.remove(3);
        map1.remove(3);
        map2.put(5, "EXAMPLE-2");

        // allow time for the recompilation to resolve
        waitTillEqual(15000);

        assertEquals(map1, map2);
        assertEquals(map3, map3);
        assertTrue(!map1.isEmpty());
    }

    @Test
    public void testPutIfAbsent() throws IOException, InterruptedException {

        assertEquals(null, map1.putIfAbsent(1, "EXAMPLE-1"));
        assertEquals("EXAMPLE-1", map1.putIfAbsent(1, "EXAMPLE-2"));
        assertEquals(null, map1.putIfAbsent(2, "EXAMPLE-2"));
        assertEquals(null, map1.putIfAbsent(3, "EXAMPLE-1"));

        assertEquals(null, map2.putIfAbsent(5, "EXAMPLE-2"));
        assertEquals(null, map2.putIfAbsent(6, "EXAMPLE-2"));

        map1.remove(2);
        map2.remove(3);
        map1.remove(3);
        map2.putIfAbsent(5, "EXAMPLE-2");

        // allow time for the recompilation to resolve
        waitTillEqual(15000);

        assertEquals(map1, map2);
        assertEquals(map3, map3);
        assertTrue(!map1.isEmpty());
    }

    private void waitTillEqual(final int timeOutMs) throws InterruptedException {
        int t = 0;
        for (; t < timeOutMs; t++) {
            if (map1.equals(map2) &&
                    map1.equals(map3)
                    && map2.equals(map3))
                break;
            Thread.sleep(1);
        }
    }
}

