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

class TestBit32Counter: Counter.Bit32 {
    constructor(lo: Int, hi: Int, incrementBy: Int): super(lo, hi, incrementBy)
    constructor(incrementBy: Int): super(incrementBy)
    constructor(other: TestBit32Counter): super(other)

    public override fun increment() { super.increment() }
    public override fun reset() { super.reset() }
}

class TestBit64Counter: Counter.Bit64 {
    constructor(lo: Long, hi: Long, incrementBy: Long): super(lo, hi, incrementBy)
    constructor(incrementBy: Long): super(incrementBy)
    constructor(other: TestBit64Counter): super(other)

    public override fun increment() { super.increment() }
    public override fun reset() { super.reset() }
}