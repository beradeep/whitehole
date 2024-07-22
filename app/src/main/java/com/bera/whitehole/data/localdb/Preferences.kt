package com.bera.whitehole.data.localdb

import android.content.Context
import android.content.SharedPreferences

@Suppress("ktlint:standard:property-naming")
object Preferences {

    const val botToken: String = "botToken"
    const val channelId: String = "channelId"
    const val startTabKey: String = "startTab"

    const val isAutoBackupEnabledKey: String = "isPeriodicPhotoBackupEnabled"
    const val autoBackupIntervalKey: String = "periodicPhotoBackupInterval"
    const val autoBackupNetworkTypeKey: String = "periodicPhotoBackupNetworkType"
    const val isAutoExportDatabaseEnabledKey: String = "isAutoExportDatabaseEnabled"
    const val autoExportDatabaseIntervalKey: String = "autoExportDatabaseInterval"
    const val autoExportDatabseLocation: String = "autoExportDatabaseLocation"
    const val defaultAutoExportDatabaseIntervalKey: Long = 7
    const val defaultAutoBackupInterval: Long = 7

    private const val prefFile: String = "preferences"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
    }

    fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)
    fun getString(key: String, defValue: String) = preferences.getString(key, defValue) ?: defValue
    fun getFloat(key: String, defValue: Float) = preferences.getFloat(key, defValue)
    fun getLong(key: String, defValue: Long) = preferences.getLong(key, defValue)
    fun getStringSet(key: String, defValue: Set<String>) =
        preferences.getStringSet(key, defValue) ?: defValue

    fun edit(action: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(action).apply()
    }
}