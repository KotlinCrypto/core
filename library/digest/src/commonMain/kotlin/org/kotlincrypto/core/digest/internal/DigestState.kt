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
package org.kotlincrypto.core.digest.internal

/**
 * Used as a holder for copying/cloning of digests.
 *
 * **NOTE:** Can only be consumed once to instantiate a new Digest. This
 * instance should **not** be held onto and should only be utilized by
 * the `copy` function's implementation.
 * */
public sealed class DigestState(
    internal val algorithm: String,
    internal val digestLength: Int,
    internal val bufOffs: Int,
)
