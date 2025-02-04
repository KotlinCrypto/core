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

/**
 * This exception is thrown when an invalid parameter is encountered.
 * */
public actual open class InvalidParameterException: IllegalArgumentException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
}

/**
 * The `GeneralSecurityException` class is a generic security exception class that provides type
 * safety for all the security-related exception classes that extend from it.
 * */
public actual open class GeneralSecurityException: Exception {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
    public actual constructor(message: String?, cause: Throwable?): super(message, cause)
    public actual constructor(cause: Throwable?): super(cause)
}

/**
 * This exception is thrown when a particular padding mechanism is expected for the input data but
 * the data is not padded properly.
 * */
public actual open class BadPaddingException: GeneralSecurityException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
}

/**
 * The `CertificateException` class is a generic security exception class that provides type safety
 * for all the certificate-related exception classes that extend from it.
 * */
public actual open class CertificateException: GeneralSecurityException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
    public actual constructor(message: String?, cause: Throwable?): super(message, cause)
    public actual constructor(cause: Throwable?): super(cause)
}

/**
 * This exception is thrown when an error occurs while attempting to encode a certificate.
 * */
public actual open class CertificateEncodingException: CertificateException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
    public actual constructor(message: String?, cause: Throwable?): super(message, cause)
    public actual constructor(cause: Throwable?): super(cause)
}

/**
 * This exception is thrown whenever an invalid DER-encoded certificate is parsed, or unsupported
 * DER features are found in the Certificate.
 * */
public actual open class CertificateParsingException: CertificateException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
    public actual constructor(message: String?, cause: Throwable?): super(message, cause)
    public actual constructor(cause: Throwable?): super(cause)
}

/**
 * This exception is thrown when the length of data provided to a block cipher is incorrect, (i.e.
 * does not match the block size of the cipher.)
 * */
public actual open class IllegalBlockSizeException: GeneralSecurityException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
}

/**
 * The `KeyException` class is a generic security exception class that provides type safety for all
 * the key-related exception classes that extend from it.
 * */
public actual open class KeyException: GeneralSecurityException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
    public actual constructor(message: String?, cause: Throwable?): super(message, cause)
    public actual constructor(cause: Throwable?): super(cause)
}

/**
 * This exception is thrown when a key is invalid (e.g. invalid encoding, wrong length, etc.).
 * */
public actual open class InvalidKeyException: KeyException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
    public actual constructor(message: String?, cause: Throwable?): super(message, cause)
    public actual constructor(cause: Throwable?): super(cause)
}

/**
 * This exception is thrown when a buffer provided by the user is too short to hold the operation result.
 * */
public actual open class ShortBufferException: GeneralSecurityException {
    public actual constructor(): super()
    public actual constructor(message: String?): super(message)
}
