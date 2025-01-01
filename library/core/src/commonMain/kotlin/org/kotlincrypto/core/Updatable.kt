/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.kotlincrypto.core

public interface Updatable {

    /**
     * Updates the instance with the specified byte.
     *
     * @param [input] The byte to update the instance with.
     * */
    public fun update(input: Byte)

    /**
     * Updates the instance with the specified array of bytes.
     *
     * @param [input] The array of bytes to update the instance with.
     * */
    public fun update(input: ByteArray)

    /**
     * Updates the instance with the specified array of bytes,
     * starting at [offset]
     *
     * @param [input] The array of bytes to update the instance with.
     * @param [offset] The index to start from in the array of bytes.
     * @param [len] The number of bytes to use, starting at [offset].
     * @throws [IllegalArgumentException] if [input] size is inappropriate
     *   for given [len] parameter.
     * @throws [IndexOutOfBoundsException] if [offset] and [len] are
     *   inappropriate.
     * */
    public fun update(input: ByteArray, offset: Int, len: Int)
}
