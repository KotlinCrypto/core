module org.kotlincrypto.core.digest {
    requires kotlin.stdlib;
    requires transitive org.kotlincrypto.core;

    exports org.kotlincrypto.core.digest;
    exports org.kotlincrypto.core.digest.internal;
}
