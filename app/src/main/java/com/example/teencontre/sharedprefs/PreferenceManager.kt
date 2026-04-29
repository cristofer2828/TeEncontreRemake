package com.example.teencontre.sharedprefs

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    // Usamos constantes para evitar errores de dedo al escribir las llaves (keys)
    companion object {
        private const val PREFS_NAME = "user_settings_panda"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOTIFICATIONS = "notifications"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- CONFIGURACIÓN DE INTERFAZ ---

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "es") ?: "es"

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    // --- INFORMACIÓN DE CONTACTO ---

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""

    fun setPhone(phone: String) {
        prefs.edit().putString(KEY_PHONE, phone).apply()
    }

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun setEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    // --- NOTIFICACIONES ---

    fun getNotifications(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)

    fun setNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    /**
     * Limpia todas las preferencias
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}