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
package org.kotlincrypto.core.internal

import org.kotlincrypto.core.KC_ANDROID_SDK_INT
import org.kotlincrypto.core.InternalKotlinCryptoApi
import java.security.Provider
import javax.crypto.MacSpi

/**
 * Android API 1-23 requires that a Provider be set, otherwise
 * when [javax.crypto.Mac.init] is called it will not use the
 * provided [org.kotlincrypto.core.Mac.Engine] (i.e., [spi]).
 *
 * This simply wraps the [org.kotlincrypto.core.Mac.Engine]
 * such that initial [javax.crypto.Mac.init] call sets it
 * as the spiImpl, and does not look to system providers
 * for an instance that supports the [algorithm].
 *
 * See: https://github.com/KotlinCrypto/core/issues/37
 * See: https://github.com/KotlinCrypto/core/issues/41
 * See: https://github.com/KotlinCrypto/core/issues/42
 * */
@Suppress("DEPRECATION")
internal class AndroidApi23MacSpiProvider private constructor(
    @Volatile
    private var spi: MacSpi?,
    private val algorithm: String,
): Provider("KC", 0.0, "") {

    override fun getService(type: String?, algorithm: String?): Service {
        return if (type == "Mac" && algorithm == this.algorithm) {
            SpiProviderService()
        } else {
            super.getService(type, algorithm)
        }
    }

    private inner class SpiProviderService: Service(
        /* provider   */ this,
        /* type       */ "Mac",
        /* algorithm  */ algorithm,
        /* className  */ "",
        /* aliases    */ null,
        /* attributes */ null
    ) {
        override fun newInstance(constructorParameter: Any?): Any = synchronized(this@AndroidApi23MacSpiProvider) {
            // simply return this if spi reference was dropped. b/c this is not
            // an instance of MacSpi, android's implementation of javax.crypto.Mac
            // will throw an exception which is what we want.
            val spi = spi ?: return@synchronized this

            // javax.crypto.Mac.init was called with a blanked key via org.kotlincrypto.Mac's
            // init block in order to set javax.crypto.Mac.initialized to true. return
            // the MacSpi (i.e. org.kotlincrypto.Mac.Engine), and null the reference as
            // we cannot provide a new instance if called again and do not want to return the
            // same, already initialized org.kotlincrypto.Mac.Engine
            this@AndroidApi23MacSpiProvider.spi = null
            return spi
        }
    }

    internal companion object {

        @JvmStatic
        @JvmSynthetic
        internal fun createOrNull(engine: MacSpi, algorithm: String): AndroidApi23MacSpiProvider? {
            @OptIn(InternalKotlinCryptoApi::class)
            return KC_ANDROID_SDK_INT?.let { sdkInt ->
                if (sdkInt < 24) AndroidApi23MacSpiProvider(engine, algorithm) else null
            }
        }
    }
}