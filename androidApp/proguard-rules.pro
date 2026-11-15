# Conservar atributos de anotación y firmas genéricas para Ktor, OkHttp y Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Reglas oficiales de Kotlinx Serialization (incluye clases DTO privadas y $$serializer)
-dontnote kotlinx.serialization.SerializationKt
-keep class kotlinx.serialization.** { *; }
-keep,includedescriptorclasses class pe.edu.upeu.reniecmovil.**$$serializer { *; }
-keepclassmembers class pe.edu.upeu.reniecmovil.** {
    *** Companion;
}
-keepclasseswithmembers class pe.edu.upeu.reniecmovil.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class pe.edu.upeu.reniecmovil.** { *; }
-keep class pe.edu.upeu.reniecmovil.data.remote.** { *; }
-keep class pe.edu.upeu.reniecmovil.data.repository.** { *; }
-keep class pe.edu.upeu.reniecmovil.domain.model.** { *; }

# Koin (Inyección de dependencias) y SQLDelight
-keep class org.koin.** { *; }
-keep class app.cash.sqldelight.** { *; }
-keep class pe.edu.upeu.reniecmovil.di.** { *; }

# Ktor Client, ServiceLoader y OkHttp/Okio (necesarios en Release para conectar a Supabase)
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class * implements io.ktor.client.HttpClientEngineContainer { *; }
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# AndroidX Security Crypto / Tink (EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**
-keep class com.google.crypto.tink.proto.** { *; }
-keep class com.google.crypto.tink.shaded.protobuf.** { *; }
-keepclassmembers class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite { *; }
