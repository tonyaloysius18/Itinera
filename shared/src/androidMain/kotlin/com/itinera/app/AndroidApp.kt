package com.itinera.app

import android.content.Context

/**
 * Holds the Application context so the shared module can reach Android APIs.
 * Set this once from MainActivity.onCreate via AndroidApp.init(this).
 */
object AndroidApp {
    lateinit var context: Context
        private set

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }
}
