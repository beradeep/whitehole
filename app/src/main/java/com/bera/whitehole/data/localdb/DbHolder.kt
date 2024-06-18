package com.bera.whitehole.data.localdb

import android.content.Context
import androidx.room.Room

object DbHolder {
    private const val DATABASE_NAME = "TeledriveDb"
    lateinit var database: Database

    fun create(applicationContext: Context) {
        database = Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            DATABASE_NAME
        ).build()
    }
}