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
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.kotlincrypto.core

public actual typealias InvalidParameterException = java.security.InvalidParameterException
public actual typealias GeneralSecurityException = java.security.GeneralSecurityException
public actual typealias BadPaddingException = javax.crypto.BadPaddingException
public actual typealias CertificateException = java.security.cert.CertificateException
public actual typealias CertificateEncodingException = java.security.cert.CertificateEncodingException
public actual typealias CertificateParsingException = java.security.cert.CertificateParsingException
public actual typealias IllegalBlockSizeException = javax.crypto.IllegalBlockSizeException
public actual typealias KeyException = java.security.KeyException
public actual typealias InvalidKeyException = java.security.InvalidKeyException
public actual typealias ShortBufferException = javax.crypto.ShortBufferException
