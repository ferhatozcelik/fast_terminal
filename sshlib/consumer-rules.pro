#-keepattributes SourceFile,LineNumberTable
#-optimizationpasses 5
-dontobfuscate

# App classes
-keep class org.connectbot.**
-keepattributes InnerClasses
-keep public class com.trilead.ssh2.compression.**
-keep public class com.trilead.ssh2.crypto.**

# All the classes are referenced indirectly.
-keep class org.conscrypt.** { *; }

# Backward compatibility code in Conscrypt
-dontnote libcore.io.Libcore
-dontnote org.apache.harmony.xnet.provider.jsse.OpenSSLRSAPrivateKey
-dontnote org.apache.harmony.security.utils.AlgNameMapper
-dontnote sun.security.x509.AlgorithmId

-dontwarn dalvik.system.BlockGuard
-dontwarn dalvik.system.BlockGuard$Policy
-dontwarn dalvik.system.CloseGuard
-dontwarn com.android.org.conscrypt.OpenSSLSocketImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.OpenSSLSocketImpl