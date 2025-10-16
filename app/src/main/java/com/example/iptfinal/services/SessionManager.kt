package com.example.iptfinal.services

import android.content.Context
import android.content.SharedPreferences
import com.example.iptfinal.models.Users

class SessionManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUser(user: Users) {
        sharedPref.edit().apply {
            putString("uid", user.uid)
            putString("firstname", user.firstname)
            putString("lastname", user.lastname)
            putString("email", user.email)
            putString("address", user.address)
            putString("mobileNum", user.mobileNum)
            putString("profilePic", user.profilePic)
            apply()
        }
    }

    fun getUser(): Users {
        return Users(
            uid = sharedPref.getString("uid", "") ?: "",
            firstname = sharedPref.getString("firstname", "") ?: "",
            lastname = sharedPref.getString("lastname", "") ?: "",
            email = sharedPref.getString("email", "") ?: "",
            address = sharedPref.getString("address", "") ?: "",
            mobileNum = sharedPref.getString("mobileNum", "") ?: "",
            profilePic = sharedPref.getString("profilePic", "") ?: ""
        )
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.contains("uid")
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }


    fun setGoogleLogin(isGoogle: Boolean) {
        sharedPref.edit().putBoolean("isGoogleLogin", isGoogle).apply()
    }

    fun isGoogleLogin(): Boolean {
        return sharedPref.getBoolean("isGoogleLogin", false)
    }
}