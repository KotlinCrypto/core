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
import org.kotlincrypto.core.Counter.Bit64.Final
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
             * 1024^2 = 1048576
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

        /**
         * Produces a final count, including any additional value needed to be
         * added (such as the size of buffered input).
         *
         * **NOTE:** [reset] is not called and the [Counter] is unaltered by
         * [additional] value.
         * */
        public fun final(additional: Int): Final {
            var lo = lo
            var hi = hi
            val lt0 = lo < 0
            lo += additional
            if (lt0 && lo >= 0) hi++
            return Final(lo = lo, hi = hi)
        }

        public override fun reset() {
            lo = 0
            hi = 0
        }

        /**
         * Holder of the count produced by [final]
         * */
        public class Final private constructor(
            @JvmField
            public val lo: Int,
            @JvmField
            public val hi: Int,
            private val isBits: Boolean,
        ) {

            internal constructor(lo: Int, hi: Int): this(lo, hi, isBits = false)

            public operator fun component1(): Int = lo
            public operator fun component2(): Int = hi

            /**
             * Convenience function for converting the final count to bits, assuming
             * that the counter is tracking bytes of input (its intended purpose).
             * */
            public fun asBits(): Final {
                if (isBits) return this
                return Final(lo shl 3, (hi shl 3) or (lo ushr 29), isBits = true)
            }

            /** @suppress */
            public override fun equals(other: Any?): Boolean = other is Final && other.hashCode() == hashCode()
            /** @suppress */
            public override fun hashCode(): Int {
                var result = 17
                result = result * 31 + lo.hashCode()
                result = result * 31 + hi.hashCode()
                result = result * 31 + isBits.hashCode()
                return result
            }
            /** @suppress */
            public override fun toString(): String = "Counter.Bit32.Final[lo=$lo, hi=$hi]"
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
             * 1024^4 = 1099511627776
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

        /**
         * Produces a final count, including any additional value needed to be
         * added (such as the size of buffered input).
         *
         * **NOTE:** [reset] is not called and the [Counter] is unaltered by
         * [additional] value.
         * */
        public fun final(additional: Int): Final {
            var lo = lo
            var hi = hi
            val lt0 = lo < 0
            lo += additional
            if (lt0 && lo >= 0) hi++
            return Final(lo = lo, hi = hi)
        }

        public override fun reset() {
            lo = 0L
            hi = 0L
        }

        /**
         * Holder of the count produced by [final]
         * */
        public class Final private constructor(
            @JvmField
            public val lo: Long,
            @JvmField
            public val hi: Long,
            private val isBits: Boolean,
        ) {

            internal constructor(lo: Long, hi: Long): this(lo, hi, isBits = false)

            public operator fun component1(): Long = lo
            public operator fun component2(): Long = hi

            /**
             * Convenience function for converting the final count to bits, assuming
             * that the counter is tracking bytes of input (its intended purpose).
             * */
            public fun asBits(): Final {
                if (isBits) return this
                return Final(lo shl 3, (hi shl 3) or (lo ushr 29), isBits = true)
            }

            /** @suppress */
            public override fun equals(other: Any?): Boolean = other is Final && other.hashCode() == hashCode()
            /** @suppress */
            public override fun hashCode(): Int {
                var result = 17
                result = result * 31 + lo.hashCode()
                result = result * 31 + hi.hashCode()
                result = result * 31 + isBits.hashCode()
                return result
            }
            /** @suppress */
            public override fun toString(): String = "Counter.Bit64.Final[lo=$lo, hi=$hi]"
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
