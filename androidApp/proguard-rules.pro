# Conservar atributos de anotación y firmas genéricas para Ktor y Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Kotlinx Serialization: conservar clases @Serializable y sus Companion / serializers generados
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class pe.edu.upeu.reniecmovil.** { *; }
-keep class pe.edu.upeu.reniecmovil.data.remote.dto.** { *; }
-keep class pe.edu.upeu.reniecmovil.domain.model.** { *; }

# Koin (Inyección de dependencias) y SQLDelight
-keep class org.koin.** { *; }
-keep class app.cash.sqldelight.** { *; }
-keep class pe.edu.upeu.reniecmovil.di.** { *; }

# Ktor Client
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# AndroidX Security Crypto / Tink (anotaciones de compilación ErrorProne)
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**

