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

package net.openhft.chronicle.hash.serialization.internal;

import net.openhft.chronicle.hash.hashing.LongHashFunction;
import net.openhft.lang.io.Bytes;

public interface MetaBytesInterop<E, I> extends MetaBytesWriter<E, I> {

    boolean startsWith(I interop, Bytes bytes, E e);
    
    <I2> boolean equivalent(I interop, E e,
                            MetaBytesInterop<E, I2> otherMetaInterop, I2 otherInterop, E other);

    long hash(I interop, LongHashFunction hashFunction, E e);
}
