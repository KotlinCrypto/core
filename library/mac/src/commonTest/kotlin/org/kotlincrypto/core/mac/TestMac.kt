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
        reset: () -> Unit = {},
        doFinal: () -> ByteArray = { ByteArray(0) },
    ): super(algorithm, TestEngine(key, reset, doFinal))

    private constructor(algorithm: String, engine: TestEngine): super(algorithm, engine)

    override fun copy(engineCopy: Engine): Mac = TestMac(algorithm(), engineCopy as TestEngine)

    private class TestEngine: Engine {

        private val reset: () -> Unit
        private val doFinal: () -> ByteArray

        constructor(
            key: ByteArray,
            reset: () -> Unit,
            doFinal: () -> ByteArray,
        ): super(key) {
            this.reset = reset
            this.doFinal = doFinal
        }

        private constructor(state: State, engine: TestEngine): super(state) {
            this.reset = engine.reset
            this.doFinal = engine.doFinal
        }

        // To ensure that Java implementation initializes javax.crypto.Mac
        // on instantiation so that it does not throw IllegalStateException
        // whenever updating.
        override fun update(input: Byte) { throw ConcurrentModificationException() }

        override fun reset() { reset.invoke() }
        override fun update(input: ByteArray) {}
        override fun update(input: ByteArray, offset: Int, len: Int) {}
        override fun macLength(): Int = 0
        override fun doFinal(): ByteArray = doFinal.invoke()

        override fun copy(): Engine = TestEngine(object : State() {}, this)
    }
}