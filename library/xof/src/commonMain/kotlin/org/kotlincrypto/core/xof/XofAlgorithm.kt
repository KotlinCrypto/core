/*
 * Copyright (c) 2023 KotlinCrypto
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
package org.kotlincrypto.core.xof

import org.kotlincrypto.core.Algorithm
import org.kotlincrypto.error.InvalidKeyException

/**
 * Denotes a class as a user of a specified cryptographic [algorithm]
 * which supports Extendable-Output Functions (XOFs).
 * */
public interface XofAlgorithm: Algorithm

/**
 * Extended functionality, specifically for a [Xof] who's backing instance
 * is a [org.kotlincrypto.core.mac.Mac].
 *
 *
 * @see [Xof.Companion.reset]
 * */
public interface ReKeyableXofAlgorithm: XofAlgorithm {

    /**
     * Resets and re-initializes the instance with the provided [newKey]
     *
     * This is useful if wanting to clear the key before de-referencing.
     *
     * @throws [InvalidKeyException] if [newKey] is unacceptable.
     * */
    public fun reset(newKey: ByteArray)
}
