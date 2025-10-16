package com.example.iptfinal.services

import android.content.Context

object NotificationManagerHelper {
    private const val PREF_NAME = "notification_prefs"
    private const val KEY_COUNT = "notification_count"

    fun getCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COUNT, 0)
    }

    fun incrementCount(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val newCount = prefs.getInt(KEY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_COUNT, newCount).commit()

    }

    fun clearCount(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_COUNT, 0).apply()
    }
}