package com.example.teencontre.data.model




abstract class BaseUser {
    abstract val id: Int
    abstract val email: String
    abstract val tipo: String
}

data class Usuario(
    override val id: Int,
    override val email: String,
    val nombre: String,
    val telefono: String
) : BaseUser() {

    override val tipo = "USUARIO"
}

data class Organizacion(
    override val id: Int,
    override val email: String,
    val nombreOrg: String,
    val ruc: String,
    val direccion: String,
    val esVerificada: Boolean = false
) : BaseUser() {

    override val tipo = "ORG"
}

data class UpdateUserRequest(

    val id: Int,

    val nombre: String,

    val telefono: String?,

    val email: String,

    val ruc: String?,

    val direccion: String?
)

data class UpdateUserResponse(

    val success: Boolean,

    val message: String
)
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
    val tipo: String,
    val nombre: String?,
    val telefono: String?,
    val nombreOrg: String?,
    val ruc: String?,
    val direccion: String?,
    val esVerificada: Boolean?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class Comentario(
    val id_publicacion: Int? = null,
    val tipo_publicacion: String,
    val nombre_usuario: String,
    val mensaje: String,
    val tiempo: String? = null
)
