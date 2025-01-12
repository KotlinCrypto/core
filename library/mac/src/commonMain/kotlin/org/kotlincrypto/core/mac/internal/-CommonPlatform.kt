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
@file:Suppress("KotlinRedundantDiagnosticSuppress", "NOTHING_TO_INLINE")

package org.kotlincrypto.core.mac.internal

import org.kotlincrypto.core.mac.Mac

private val SINGLE_0BYTE_KEY = ByteArray(1) { 0 }

@Suppress("UnusedReceiverParameter")
@Throws(IllegalArgumentException::class)
internal inline fun Mac.commonInit(algorithm: String) {
    require(algorithm.isNotBlank()) { "algorithm cannot be blank" }
}

internal inline fun Mac.commonToString(): String {
    return "Mac[${algorithm()}]@${hashCode()}"
}

internal inline fun Mac.commonClearKey(engineReset: (ByteArray) -> Unit) {
    try {
        engineReset(SINGLE_0BYTE_KEY)
    } catch (e1: IllegalArgumentException) {
        try {
            engineReset(ByteArray(macLength()))
        } catch (e2: IllegalArgumentException) {
            e2.addSuppressed(e1)
            throw e2
        }
    }
}
