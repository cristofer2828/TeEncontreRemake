package com.example.teencontre.data.model

// Usamos sealed class para asegurar que el sistema solo conozca estos dos tipos de usuario
sealed class UserRole(val type: String) {
    object Usuario : UserRole("USUARIO")
    object Organizacion : UserRole("ORG")
}

// Clase base utilizando data class para facilitar la serialización
open class BaseUser(
    open val id: Int,
    open val email: String,
    val tipo: String
)

data class Usuario(
    override val id: Int,
    override val email: String,
    val nombre: String,
    val telefono: String
) : BaseUser(id, email, UserRole.Usuario.type)

data class Organizacion(
    override val id: Int,
    override val email: String,
    val nombreOrg: String,
    val ruc: String,
    val direccion: String,
    val esVerificada: Boolean = false
) : BaseUser(id, email, UserRole.Organizacion.type)


data class RegisterRequest(
    val nombre: String,
    val telefono: String,
    val email: String,
    val contrasena: String,
    val ruc: String?, // Nulo para usuario normal
    val direccion: String?, // Nulo para usuario normal
    val esOrganizacion: Boolean
)

// Clase para loguear
data class LoginRequest(val email: String, val contrasena: String)

// Respuesta del servidor al loguear
data class LoginResponse(
    val id: Int,
    val email: String,
    val tipo: String, // "USUARIO" o "ORG"
    val nombre: String?,
    val nombreOrg: String?,
    val esVerificada: Boolean?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)