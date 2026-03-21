package com.daremote.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "daremote_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun store(value: String): String {
        val ref = UUID.randomUUID().toString()
        prefs.edit().putString(ref, value).apply()
        return ref
    }

    fun retrieve(ref: String): String? {
        return prefs.getString(ref, null)
    }

    fun update(ref: String, value: String) {
        prefs.edit().putString(ref, value).apply()
    }

    fun delete(ref: String) {
        prefs.edit().remove(ref).apply()
    }
}
