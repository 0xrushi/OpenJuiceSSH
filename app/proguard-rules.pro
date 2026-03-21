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
