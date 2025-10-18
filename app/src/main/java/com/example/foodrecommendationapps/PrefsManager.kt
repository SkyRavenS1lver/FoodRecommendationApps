package com.example.foodrecommendationapps

import android.content.Context
import androidx.core.content.edit

object PrefsManager {

    private const val PREF_NAME = "nutrition_apps_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"

    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit {
            remove(KEY_TOKEN)
        }
    }
    fun saveUserId(context: Context, userId:Int) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit {
            putInt(KEY_USER_ID, userId)
        }
    }

    fun getUserId(context: Context): Int {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getInt(KEY_USER_ID, 0)
    }

    fun clearUserId(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit {
            remove(KEY_USER_ID)
        }
    }
}
