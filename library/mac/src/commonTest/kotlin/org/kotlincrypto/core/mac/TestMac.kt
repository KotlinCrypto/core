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
package org.kotlincrypto.core.mac

class TestMac : Mac {

    constructor(
        key: ByteArray,
        algorithm: String,
        macLen: Int = 0,
        resetOnDoFinal: Boolean = true,
        reset: () -> Unit = {},
        rekey: (new: ByteArray) -> Unit = {},
        doFinal: () -> ByteArray = { ByteArray(macLen) },
    ): super(algorithm, TestEngine(key, reset, rekey, doFinal, macLen, resetOnDoFinal))

    private constructor(algorithm: String, engine: TestEngine): super(algorithm, engine)
    private constructor(other: TestMac): super(other)

    override fun copy(): Mac = TestMac(this)

    private class TestEngine: Engine {

        private val reset: () -> Unit
        private val rekey: (new: ByteArray) -> Unit
        private val doFinal: () -> ByteArray
        private val macLen: Int

        constructor(
            key: ByteArray,
            reset: () -> Unit,
            rekey: (new: ByteArray) -> Unit,
            doFinal: () -> ByteArray,
            macLen: Int,
            resetOnDoFinal: Boolean,
        ): super(key, resetOnDoFinal) {
            this.reset = reset
            this.rekey = rekey
            this.doFinal = doFinal
            this.macLen = macLen
        }

        private constructor(other: TestEngine): super(other) {
            this.reset = other.reset
            this.rekey = other.rekey
            this.doFinal = other.doFinal
            this.macLen = other.macLen
        }

        // To ensure that Java implementation initializes javax.crypto.Mac
        // on instantiation so that it does not throw IllegalStateException
        // whenever updating.
        override fun update(input: Byte) { throw ConcurrentModificationException() }

        override fun reset() { reset.invoke() }
        override fun reset(newKey: ByteArray) { rekey.invoke(newKey) }
        override fun update(input: ByteArray) {}
        override fun update(input: ByteArray, offset: Int, len: Int) {}
        override fun macLength(): Int = macLen
        override fun doFinal(): ByteArray = doFinal.invoke()

        override fun copy(): Engine = TestEngine(this)
    }
}
