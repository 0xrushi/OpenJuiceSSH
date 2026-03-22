-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn javax.security.auth.login.LoginContext
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.MessageProp
-dontwarn org.ietf.jgss.Oid
-dontwarn sun.security.x509.X509Key

# SSHJ
-keep class net.schmizz.sshj.** { *; }
-keep class com.hierynomus.** { *; }
-keep class net.i2p.crypto.eddsa.** { *; }
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
