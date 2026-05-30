package com.example.teencontre.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import com.example.teencontre.data.model.BaseUser
import com.example.teencontre.data.model.Usuario
import com.example.teencontre.data.model.Organizacion
import com.google.gson.Gson

class PreferenceManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "user_settings_panda"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_AD_TYPE = "ad_type"

        // Nuevas llaves para la sesión de Azure
        private const val KEY_USER_DATA = "user_data_json"
        private const val KEY_USER_ROLE = "user_role_type"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson() // Instancia de Gson para serializar/deserializar objetos complejos

    // --- INFORMACIÓN DE CONTACTO ---
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun setUserName(name: String) { prefs.edit().putString(KEY_USER_NAME, name).apply() }

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""
    fun setPhone(phone: String) { prefs.edit().putString(KEY_PHONE, phone).apply() }

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun setEmail(email: String) { prefs.edit().putString(KEY_EMAIL, email).apply() }

    // --- NOTIFICACIONES ---
    fun getNotifications(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    fun setNotifications(enabled: Boolean) { prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply() }

    // --- GESTIÓN DE SESIÓN DE USUARIOS (AZURE) ---

    /**
     * Guarda el usuario logueado en SharedPreferences convirtiéndolo a JSON de forma automática.
     */
    fun saveLoggedUser(user: BaseUser) {
        val userJson = gson.toJson(user)
        prefs.edit().apply {
            putString(KEY_USER_DATA, userJson)
            putString(KEY_USER_ROLE, user.tipo) // Almacena si es "USUARIO" o "ORG"
            apply()
        }
    }

    /**
     * Recupera el usuario logueado reconstruyendo polimórficamente su clase real.
     */
    fun getLoggedUser(): BaseUser? {
        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        val userRole = prefs.getString(KEY_USER_ROLE, null)

        return try {
            when (userRole) {
                "USUARIO" -> gson.fromJson(userJson, Usuario::class.java)
                "ORG" -> gson.fromJson(userJson, Organizacion::class.java)
                else -> gson.fromJson(userJson, BaseUser::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina los datos de sesión cuando el usuario presiona Logout.
     */
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_USER_DATA)
            remove(KEY_USER_ROLE)
            apply()
        }
    }

    // --- GESTIÓN DE ANUNCIOS ACTUALIZADA ---

    /**
     * Guarda el anuncio incluyendo el tipo (PERDIDA, ENCONTRADA, ADOPCIÓN)
     */
    fun saveAd(name: String, phone: String, email: String, type: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_PHONE, phone)
            putString(KEY_EMAIL, email)
            putString(KEY_AD_TYPE, type)
            apply()
        }
    }

    /**
     * Recupera el anuncio guardado.
     * Retorna el tipo guardado o "ACTIVO" por defecto.
     */
    fun getSavedAd(): Map<String, String>? {
        val name = prefs.getString(KEY_USER_NAME, null) ?: return null

        return mapOf(
            "name" to name,
            "phone" to (prefs.getString(KEY_PHONE, "") ?: ""),
            "email" to (prefs.getString(KEY_EMAIL, "") ?: ""),
            "type" to (prefs.getString(KEY_AD_TYPE, "ACTIVO") ?: "ACTIVO")
        )
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}