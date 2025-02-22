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
package org.kotlincrypto.core.digest

import java.security.MessageDigest

/**
 * Verifies that [Digest] abstraction produces the same exceptions that
 * the Jvm's [MessageDigest] does.
 * */
class JvmDigestExceptionTest: AbstractTestUpdateExceptions() {

    private val digest = MessageDigest.getInstance("MD5")

    override fun update(input: Byte) {
        digest.update(input)
    }

    override fun update(input: ByteArray) {
        digest.update(input)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        digest.update(input, offset, len)
    }
}
