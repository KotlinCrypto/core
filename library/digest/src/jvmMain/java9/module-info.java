module org.kotlincrypto.core.digest {
    requires transitive kotlin.stdlib;
    requires transitive org.kotlincrypto.core;

    exports org.kotlincrypto.core.digest;
    exports org.kotlincrypto.core.digest.internal;
}
