# Reglas de ProGuard/R8 de la app Monderas.

# Calendario: biweekly (iCalendar)
-keep class biweekly.** { *; }

# Calendario: OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# biweekly: módulo JSON opcional (Jackson no incluido)
-dontwarn com.fasterxml.jackson.**

# Mantener atributos para bibliotecas con reflexión
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
