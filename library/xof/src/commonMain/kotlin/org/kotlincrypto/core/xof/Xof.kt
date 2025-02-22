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
@file:Suppress("KotlinRedundantDiagnosticSuppress", "NOTHING_TO_INLINE")

package org.kotlincrypto.core.xof

import org.kotlincrypto.core.*
import org.kotlincrypto.error.InvalidKeyException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Extendable-Output Function (i.e. XOF)
 *
 * FIPS PUB 202 introduced XOFs where output for certain cryptographic functions can be
 * variable in length. This is an implementation which provides such functionality.
 *
 * e.g.
 *
 *     val xof = SHAKE128.xOf()
 *     xof.update(Random.Default.nextBytes(500))
 *
 *     val out1 = ByteArray(64)
 *     val out2 = ByteArray(out1.size * 2)
 *     xof.use(resetXof = false) { read(out1); read(out2) }
 *
 *     val out3 = ByteArray(out1.size)
 *     val out4 = ByteArray(out2.size)
 *     val reader = xof.reader()
 *     reader.read(out3)
 *     reader.use { read(out4, 0, out4.size) }
 *
 *     assertContentEquals(out1, out3)
 *     assertContentEquals(out2, out4)
 *
 * https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.202.pdf
 *
 * @see [use]
 * @see [reader]
 * @see [Reader]
 * */
public sealed class Xof<A: XofAlgorithm>(
    @JvmField
    protected val delegate: A,
): Algorithm, Copyable<Xof<A>>, Resettable, Updatable {

    public companion object {

        /**
         * Helper to provide access to the instance backing [Xof], if said instance
         * can be re-keyed (such as a [org.kotlincrypto.core.mac.Mac]).
         *
         * @throws [InvalidKeyException] if [newKey] is unacceptable.
         * */
        @JvmStatic
        public fun <A: ReKeyableXofAlgorithm> Xof<A>.reset(newKey: ByteArray) { delegate.reset(newKey) }
    }

    /**
     * Takes a snapshot of the current [Xof]'s state and produces
     * a [Reader].
     *
     * [Reader] is automatically closed after action completes
     * such that reading from the closed [Reader] will throw
     * exception; another [Reader] must be produced if further
     * reading is required.
     *
     * The [Xof] can continue to be updated with new data or read
     * from again as it is unaffected by [Reader.read]s.
     *
     * @param [resetXof] if true, also resets the [Xof] to its
     *   initial state after taking the snapshot.
     * */
    @JvmOverloads
    @OptIn(ExperimentalContracts::class)
    public inline fun <T: Any?> use(
        resetXof: Boolean = true,
        action: Reader.() -> T,
    ): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        return reader(resetXof).use(action)
    }

    /**
     * Takes a snapshot of the current [Xof]'s state and produces
     * a [Reader].
     *
     * The [Reader] remains open and will continue to produce new
     * output until [Reader.close] is explicitly called.
     *
     * The [Xof] can continue to be updated with new data or read
     * from again as it is unaffected by [Reader.read]s.
     *
     * @param [resetXof] if true, also resets the [Xof] to its
     *   initial state after taking the snapshot.
     * */
    @JvmOverloads
    public fun reader(resetXof: Boolean = true): Reader {
        val reader = newReader()

        // newReader() takes copy of Xof at its current
        // state, so calling reset() _after_ is ok.
        if (resetXof) reset()

        return reader
    }

    /**
     * Reads the [Xof] snapshot.
     * */
    public abstract inner class Reader {

        private var lo: Int = 0
        private var hi: Int = 0

        /**
         * The total amount of bytes read for this [Reader] instance.
         *
         * **NOTE:** Only tracks up to a maximum of 2^64 bytes. Actual
         * output can potentially be larger than that.
         * */
        @get:JvmName("bytesRead")
        public val bytesRead: Long get() {
            val lo = lo
            val hi = hi
            if (hi == 0) return lo.toLong()
            return ((hi.toLong() and 0xffffffff) shl 32) or (lo.toLong() and 0xffffffff)
        }

        /**
         * If the reader is closed or not
         * */
        @get:JvmName("isClosed")
        public var isClosed: Boolean = false
            private set

        /**
         * Helper function which automatically invokes [close]
         * once action completes.
         * */
        @OptIn(ExperimentalContracts::class)
        public inline fun <T: Any?> use(action: Reader.() -> T): T {
            contract {
                callsInPlace(action, InvocationKind.EXACTLY_ONCE)
            }
            try {
                return action(this)
            } finally {
                close()
            }
        }

        /**
         * Reads the [Xof] snapshot's state for when [Reader] was
         * produced, filling the provided [out] array completely.
         *
         * This can be called multiple times until [close] has been invoked.
         *
         * @param [out] The array to fill
         * @return The number of bytes written to [out]
         * @throws [IllegalStateException] if [isClosed] is true
         * */
        public fun read(out: ByteArray): Int = read(out, 0, out.size)

        /**
         * Reads the [Xof] snapshot's state for when [Reader] was
         * produced, filling the provided [out] array for specified
         * [offset] and [len] arguments.
         *
         * This can be called multiple times until [close] has been invoked.
         *
         * @param [out] The array to put the data into
         * @param [offset] The index for [out] to start putting data
         * @param [len] The number of bytes to put into [out]
         * @return The number of bytes written to [out]
         * @throws [IllegalArgumentException] if [offset] and/or [len] are inappropriate
         * @throws [IllegalStateException] if [isClosed] is true
         * @throws [IndexOutOfBoundsException] if [offset] and/or [len] are inappropriate
         * */
        public fun read(out: ByteArray, offset: Int, len: Int): Int {
            if (isClosed) throw IllegalStateException("Reader is closed")
            if (out.size - offset < len) throw IllegalArgumentException("out is too short")
            if (len == 0) return 0
            if (offset < 0 || len < 0 || offset > out.size - len) throw IndexOutOfBoundsException()

            val read = readProtected(out, offset, len)

            // Update read counter
            val lt0 = lo < 0
            lo += read
            if (lt0 && lo >= 0) hi++

            return read
        }

        /**
         * Closes the [Reader], rendering it no-longer usable for [read]s.
         * Attempting to [read] again after closure will result in an
         * [IllegalStateException] being thrown.
         *
         * Successive invocations to [close] do nothing.
         * */
        public fun close() {
            if (isClosed) return
            closeProtected()
            isClosed = true
        }

        protected abstract fun readProtected(out: ByteArray, offset: Int, len: Int): Int
        protected abstract fun closeProtected()

        /** @suppress */
        public final override fun toString(): String = "${this@Xof}.Reader@${hashCode()}"
    }

    /** @suppress */
    @InternalKotlinCryptoApi
    public object Utils {

        @JvmStatic
        public fun leftEncode(value: Int): ByteArray {
            return encode(lo = value, hi = 0, left = true)
        }

        @JvmStatic
        public fun leftEncode(value: Long): ByteArray {
            return encode(lo = value.lo(), hi = value.hi(), left = true)
        }

        @JvmStatic
        public fun leftEncode(lo: Int, hi: Int): ByteArray {
            return encode(lo = lo, hi = hi, left = true)
        }

        @JvmStatic
        public fun rightEncode(value: Int): ByteArray {
            return encode(lo = value, hi = 0, left = false)
        }

        @JvmStatic
        public fun rightEncode(value: Long): ByteArray {
            return encode(lo = value.lo(), hi = value.hi(), left = false)
        }

        @JvmStatic
        public fun rightEncode(lo: Int, hi: Int): ByteArray {
            return encode(lo = lo, hi = hi, left = false)
        }

        @JvmStatic
        private inline fun Long.lo(): Int = toInt()

        @JvmStatic
        private inline fun Long.hi(): Int = rotateLeft(32).toInt()

        @JvmStatic
        private inline fun encode(lo: Int, hi: Int, left: Boolean): ByteArray {
            val a = if (hi == 0) {
                if (lo == 0) {
                    // If it's zero, return early
                    return if (left) byteArrayOf(1, 0) else byteArrayOf(0, 1)
                }

                byteArrayOf(
                    (lo ushr 24).toByte(),
                    (lo ushr 16).toByte(),
                    (lo ushr  8).toByte(),
                    (lo        ).toByte(),
                )
            } else {
                byteArrayOf(
                    (hi ushr 24).toByte(),
                    (hi ushr 16).toByte(),
                    (hi ushr  8).toByte(),
                    (hi        ).toByte(),
                    (lo ushr 24).toByte(),
                    (lo ushr 16).toByte(),
                    (lo ushr  8).toByte(),
                    (lo        ).toByte(),
                )
            }

            // Find index of first non-zero byte
            var i = 0
            while (i < a.size && a[i] == ZERO) {
                i++
            }

            val b = ByteArray(a.size - i + 1)
            val num = (a.size - i).toByte()

            val offset = if (left) {
                // Prepend with number of non-zero bytes
                b[0] = num
                1
            } else {
                // Append with number of non-zero bytes
                b[b.lastIndex] = num
                0
            }

            a.copyInto(b, offset, i)

            return b
        }

        private const val ZERO: Byte = 0
    }

    protected abstract fun newReader(): Reader

    /** @suppress */
    public final override fun toString(): String = "Xof[${algorithm()}]@${hashCode()}"
}
