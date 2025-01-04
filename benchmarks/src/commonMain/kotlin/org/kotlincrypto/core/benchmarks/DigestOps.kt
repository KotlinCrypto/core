/*
 * Copyright (c) 2024 Matthew Nelson
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
package org.kotlincrypto.core.benchmarks

import kotlinx.benchmark.*
import org.kotlincrypto.core.digest.Digest
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 3)
open class DigestBenchmark {

    private class TestDigest: Digest {
        constructor(): super("Benchmark", 32, 32)
        private constructor(other: TestDigest): super(other)
        override fun resetProtected() {}
        override fun copy(): TestDigest = TestDigest(this)
        override fun compressProtected(input: ByteArray, offset: Int) {}
        override fun digestProtected(buf: ByteArray, bufPos: Int): ByteArray = ByteArray(0)
    }

    private val digest = TestDigest()
    private val bytes = Random.Default.nextBytes(100)

    @Setup
    fun setup() { digest.update(bytes, 0, digest.blockSize()) }

    @Benchmark
    fun update() { digest.update(bytes) }
}
