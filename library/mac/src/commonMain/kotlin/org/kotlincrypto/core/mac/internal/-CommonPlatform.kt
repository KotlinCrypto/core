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
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package org.kotlincrypto.core.mac.internal

import org.kotlincrypto.core.mac.Mac

@Throws(IllegalArgumentException::class)
@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
internal inline fun Mac.commonInit(algorithm: String) {
    require(algorithm.isNotBlank()) { "algorithm cannot be blank" }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Mac.commonToString(): String {
    return "Mac[${algorithm()}]@${hashCode()}"
}
