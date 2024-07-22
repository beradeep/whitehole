package com.bera.whitehole.data.localdb

import android.content.Context

object DbHolder {
    lateinit var database: WhDatabase

    fun create(applicationContext: Context) {
        database = WhDatabase.create(applicationContext)
    }
}