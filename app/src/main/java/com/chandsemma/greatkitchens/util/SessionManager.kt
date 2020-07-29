package com.chandsemma.greatkitchens.util

import android.content.Context

class SessionManager(context: Context) {
    var PRIVATE_MODE = 0
    val PREF_NAME = "GreatKitchensApp"
    val KEY_IS_LOGGEDIN = "isLoggedIn"
    var sharedPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    var editor = sharedPref.edit()
    fun setLogin(isLoggedIn: Boolean){
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn)
        editor.apply()
    }
    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean(KEY_IS_LOGGEDIN, false)
    }
}