package com.example.iptfinal.services

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging

object NotificationPreference {
    private const val PREF_NAME = "notification_prefs"
    private const val KEY_ENABLED = "notifications_enabled"
    private const val TOPIC = "vaccine_reminders"

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()

        if (enabled) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
    }
}
