package com.example.teencontre.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import com.example.teencontre.data.model.BaseUser
import com.example.teencontre.data.model.Usuario
import com.example.teencontre.data.model.Organizacion
import com.google.gson.Gson
import androidx.core.content.edit

class PreferenceManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "user_settings_panda"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOTIFICATIONS = "notifications"

        // Nuevas llaves para la sesión de Azure
        private const val KEY_USER_DATA = "user_data_json"
        private const val KEY_USER_ROLE = "user_role_type"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- NOTIFICACIONES ---
    fun getNotifications(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    fun setNotifications(enabled: Boolean) { prefs.edit { putBoolean(KEY_NOTIFICATIONS, enabled) } }

    // --- GESTIÓN DE SESIÓN DE USUARIOS (AZURE) ---

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

    fun saveLoggedUser(user: BaseUser) {
        val json = gson.toJson(user)
        prefs.edit {
            putString(KEY_USER_DATA, json)
            putString(KEY_USER_ROLE, user.tipo)
        }
    }

    fun clearSession() {
        prefs.edit {
            remove(KEY_USER_DATA)
            remove(KEY_USER_ROLE)
        }
    }

    /**
     * Extrae de forma segura el nombre del usuario actual evaluando su tipo de modelo.
     * Revisa si tu data class maneja la propiedad 'nombre' (para Usuario) o 'nombreOrganizacion' (para ONS)
     */
    /**
     * Extrae de forma segura el nombre del usuario o de la organización actual
     * mapeando correctamente las propiedades exactas de tus modelos.
     */
    fun getUserName(): String? {
        val user = getLoggedUser() ?: return null
        return when (user) {
            is Usuario -> user.nombre       // Usa .nombre de tu data class Usuario
            is Organizacion -> user.nombreOrg // Usa .nombreOrg de tu data class Organizacion
            else -> null
        }
    }
}