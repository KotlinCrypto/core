/*
 * Copyright (c) 2025 Matthew Nelson
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
import org.kotlincrypto.core.InternalKotlinCryptoApi
import org.kotlincrypto.core.xof.Xof
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 3)
@OptIn(InternalKotlinCryptoApi::class)
open class XofUtilsBenchmark {

    private val longs: LongArray = LongArray(10) { Random.Default.nextLong() }
    private val loHi = Array(longs.size) { longs[it].let { l -> l.toInt() to l.rotateLeft(32).toInt() } }

    @Benchmark
    fun leftEncodeLongs() {
        val longs = longs
        longs.forEach { long -> Xof.Utils.leftEncode(long) }
    }

    @Benchmark
    fun leftEncodeLoHi() {
        val loHi = loHi
        loHi.forEach { Xof.Utils.leftEncode(it.first, it.second) }
    }
}
