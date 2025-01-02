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
package org.kotlincrypto.core

import org.kotlincrypto.core.Counter.Bit32.Companion.MAX_INCREMENT
import org.kotlincrypto.core.Counter.Bit64.Companion.MAX_INCREMENT
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Utility for counting things.
 * */
public sealed class Counter private constructor(): Resettable, Copyable<Counter> {

    /**
     * Increments the counter
     * */
    public abstract fun increment()

    /**
     * A counter that utilizes 32-bit numbers providing a maximum count of 2^64.
     *
     * @see [Bit64]
     * */
    public class Bit32: Counter {

        public companion object {

            /**
             * The maximum value for which [Bit32.incrementBy] can be set to.
             *
             * 1024^2 >> 1048576
             * */
            public const val MAX_INCREMENT: Int = 1048576 // Never decrease, only increase.
        }

        /**
         * The value to increment things by
         * */
        @JvmField
        public val incrementBy: Int

        /**
         * The least significant bits of the number
         * */
        @get:JvmName("lo")
        public var lo: Int
            private set

        /**
         * The most significant bits of the number
         * */
        @get:JvmName("hi")
        public var hi: Int
            private set

        /**
         * Creates a new [Bit32] counter initialized to [lo] and [hi]
         *
         * @throws [IllegalArgumentException] when:
         *  - [incrementBy] is less than or equal to 0
         *  - [incrementBy] is greater than [MAX_INCREMENT]
         *  - [incrementBy] is not a factor of 8
         *  - [lo] is not a factor of [incrementBy]
         * */
        public constructor(lo: Int, hi: Int, incrementBy: Int): super() {
            require(incrementBy > 0) { "incrementBy[$incrementBy] must be greater than 0" }
            require(incrementBy <= MAX_INCREMENT) { "incrementBy[$incrementBy] must be less than or equal to $MAX_INCREMENT" }
            require(incrementBy % 8 == 0) { "incrementBy[$incrementBy] must be a factor of 8" }
            require(lo % incrementBy == 0) { "lo must be a factor of incrementBy[$incrementBy]" }

            this.incrementBy = incrementBy
            this.lo = lo
            this.hi = hi
        }

        /**
         * Creates a new [Bit32] counter initialized to 0, 0
         *
         * @throws [IllegalArgumentException] when [incrementBy] is:
         *  - Less than or equal to 0
         *  - Greater than [MAX_INCREMENT]
         *  - Not a factor of 8
         * */
        public constructor(incrementBy: Int): this(0, 0, incrementBy)

        public override fun copy(): Bit32 = Bit32(this)

        public override fun increment() {
            lo += incrementBy
            if (lo == 0) hi++
        }

        public override fun reset() {
            lo = 0
            hi = 0
        }

        private constructor(other: Bit32): super() {
            this.incrementBy = other.incrementBy
            this.lo = other.lo
            this.hi = other.hi
        }
    }

    /**
     * A counter that utilizes 64-bit numbers providing a maximum count of 2^128.
     *
     * @see [Bit32]
     * */
    public class Bit64: Counter {

        public companion object {

            /**
             * The maximum value for which [Bit64.incrementBy] can be set to.
             *
             * 1024^4 >> 1099511627776
             * */
            public const val MAX_INCREMENT: Long = 1099511627776L // Never decrease, only increase.
        }

        /**
         * The value to increment things by
         * */
        @JvmField
        public val incrementBy: Long

        /**
         * The least significant bits of the number
         * */
        @get:JvmName("lo")
        public var lo: Long
            private set

        /**
         * The most significant bits of the number
         * */
        @get:JvmName("hi")
        public var hi: Long
            private set

        /**
         * Creates a new [Bit64] counter initialized to [lo] and [hi]
         *
         * @throws [IllegalArgumentException] when:
         *  - [incrementBy] is less than or equal to 0
         *  - [incrementBy] is greater than [MAX_INCREMENT]
         *  - [incrementBy] is not a factor of 8
         *  - [lo] is not a factor of [incrementBy]
         * */
        public constructor(lo: Long, hi: Long, incrementBy: Long): super() {
            require(incrementBy > 0L) { "incrementBy[$incrementBy] must be greater than 0" }
            require(incrementBy <= MAX_INCREMENT) { "incrementBy[$incrementBy] must be less than or equal to $MAX_INCREMENT" }
            require(incrementBy % 8 == 0L) { "incrementBy[$incrementBy] must be a factor of 8" }
            require(lo % incrementBy == 0L) { "lo must be a factor of incrementBy[$incrementBy]" }

            this.incrementBy = incrementBy
            this.lo = lo
            this.hi = hi
        }

        /**
         * Creates a new [Bit64] counter initialized to 0, 0
         *
         * @throws [IllegalArgumentException] when [incrementBy] is:
         *  - Less than or equal to 0
         *  - Greater than [MAX_INCREMENT]
         *  - Not a factor of 8
         * */
        public constructor(incrementBy: Long): this(0, 0, incrementBy)

        public override fun copy(): Bit64 = Bit64(this)

        public override fun increment() {
            lo += incrementBy
            if (lo == 0L) hi++
        }

        public override fun reset() {
            lo = 0L
            hi = 0L
        }

        private constructor(other: Bit64): super() {
            this.incrementBy = other.incrementBy
            this.lo = other.lo
            this.hi = other.hi
        }
    }

    private val code = Any()

    /** @suppress */
    public final override fun equals(other: Any?): Boolean = other is Counter && other.hashCode() == hashCode()
    /** @suppress */
    public final override fun hashCode(): Int = 17 * 31 + code.hashCode()
    /** @suppress */
    public final override fun toString(): String = when (this) {
        is Bit32 -> "Bit32[lo=$lo, hi=$hi, incrementBy=$incrementBy]"
        is Bit64 -> "Bit64[lo=$lo, hi=$hi, incrementBy=$incrementBy]"
    }.let { value -> "Counter.$value@${hashCode()}" }
}
