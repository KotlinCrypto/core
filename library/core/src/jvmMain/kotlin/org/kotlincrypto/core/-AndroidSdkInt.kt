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
package org.kotlincrypto.core

@InternalKotlinCryptoApi
public val KC_ANDROID_SDK_INT: Int? by lazy {
    if (
        System.getProperty("java.runtime.name")
            ?.contains("android", ignoreCase = true) != true
    ) {
        // Not Android runtime
        return@lazy null
    }

    try {
        val clazz = Class.forName("android.os.Build\$VERSION")

        try {
            clazz?.getField("SDK_INT")?.getInt(null)
        } catch (_: Throwable) {
            clazz?.getField("SDK")?.get(null)?.toString()?.toIntOrNull()
        }
    } catch (_: Throwable) {
        null
    }
}
