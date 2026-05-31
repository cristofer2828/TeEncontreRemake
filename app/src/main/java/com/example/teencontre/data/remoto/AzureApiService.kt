package com.example.teencontre.data.remote

import com.example.teencontre.data.model.MascotasAdopcionModel
import com.example.teencontre.data.model.MascotasEncontradasModel
import com.example.teencontre.data.model.MascotasPerdidasModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.example.teencontre.data.model.LoginRequest
import com.example.teencontre.data.model.LoginResponse
import com.example.teencontre.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.Path

import com.example.teencontre.data.model.RegisterResponse
import com.example.teencontre.data.model.UpdateUserRequest
import com.example.teencontre.data.model.UpdateUserResponse

interface AzureApiService {
    // --- AUTH ---
    @POST("api/Usuarios/update_profile.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
        ): Response<UpdateUserResponse>
    @GET("api/perdidos/usuario/{id}")
    suspend fun getPerdidosUsuario(
        @Path("id") id: Int
    ): List<MascotasPerdidasModel>

    @GET("api/encontrados/usuario/{id}")
    suspend fun getEncontradosUsuario(
        @Path("id") id: Int
    ): List<MascotasEncontradasModel>

    @GET("api/adopciones/usuario/{id}")
    suspend fun getAdopcionesUsuario(
        @Path("id") id: Int
    ): List<MascotasAdopcionModel>
    @POST("api/Usuarios/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/Usuarios/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // --- MASCOTAS PERDIDAS ---
    @GET("api/perdidos")
    suspend fun getTodosLosPerdidos(): List<MascotasPerdidasModel>

    // Usamos @Multipart para enviar los datos de texto separados del archivo binario de la foto
    @Multipart
    @POST("api/perdidos")
    suspend fun subirPerdido(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("nombreM") nombreM: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("lugar") lugar: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part foto: MultipartBody.Part? // Aquí viaja el ByteArray transformado en archivo real
    ): Response<Void>

    // --- MASCOTAS ENCONTRADAS ---
    @GET("api/encontrados")
    suspend fun getTodosLosEncontrados(): List<MascotasEncontradasModel>

    @Multipart
    @POST("api/encontrados")
    suspend fun subirEncontrado(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("lugar") lugar: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<Void>

    // --- ADOPCIONES ---
    @GET("api/adopciones")
    suspend fun getTodasLasAdopciones(): List<MascotasAdopcionModel>

    @Multipart
    @POST("api/adopciones")
    suspend fun subirAdopcion(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part("vacunado") vacunado: RequestBody,
        @Part("esterilizado") esterilizado: RequestBody,
        @Part("desparasitado") desparasitado: RequestBody,
        @Part("tamano") tamano: RequestBody,
        @Part("temperamento") temperamento: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("nombreOrganizacion") nombreOrganizacion: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<Void>


}