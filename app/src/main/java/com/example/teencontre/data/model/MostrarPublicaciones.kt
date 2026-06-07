package com.example.teencontre.data.model

import com.google.gson.annotations.SerializedName

data class MostrarPublicaciones(

    @SerializedName("Id")
    val id: Int,

    @SerializedName("IdUsuario")
    val idUsuario: Int,

    @SerializedName("Tipo")
    val tipo: String,

    @SerializedName("NombreMascota")
    val nombreMascota: String?,

    @SerializedName("Especie")
    val especie: String,

    @SerializedName("Genero")
    val genero: String,

    @SerializedName("Raza")
    val raza: String?,

    @SerializedName("Foto")
    val foto: String?,

    @SerializedName("Lugar")
    val lugar: String?,

    @SerializedName("Descripcion")
    val descripcion: String,

    @SerializedName("Fecha")
    val fecha: String?,

    @SerializedName("FechaRegistro")
    val fechaRegistro: String,

    @SerializedName("Vacunado")
    val vacunado: Boolean?,

    @SerializedName("Esterilizado")
    val esterilizado: Boolean?,

    @SerializedName("Desparasitado")
    val desparasitado: Boolean?,

    @SerializedName("Tamano")
    val tamano: String?,

    @SerializedName("Temperamento")
    val temperamento: String?,

    @SerializedName("NombreOrganizacion")
    val nombreOrganizacion: String?,

    @SerializedName("Telefono")
    val telefono: String?,

    @SerializedName("Correo")
    val correo: String?
)