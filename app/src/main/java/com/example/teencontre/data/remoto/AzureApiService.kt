package com.example.teencontre.data.remote

import com.example.teencontre.data.model.ApiResponse
import com.example.teencontre.data.model.EliminarRequest
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
import com.example.teencontre.data.model.MostrarPublicaciones
import com.example.teencontre.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.Path

import com.example.teencontre.data.model.RegisterResponse
import com.example.teencontre.data.model.UpdateUserRequest
import com.example.teencontre.data.model.UpdateUserResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Query

interface AzureApiService {
    @POST("api/Usuarios/update_profile.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
        ): Response<UpdateUserResponse>

    @POST("api/Usuarios/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/Usuarios/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @Multipart
    @POST("api/Publicaciones/guardar_perdido.php")
    suspend fun subirPerdido(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("nombreM") nombreM: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part("fecha") fecha: RequestBody,
        @Part("lugar") lugar: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody
    ): Response<ApiResponse>

    @GET("api/Publicaciones/obtener_perdidos_usuario.php")
    suspend fun getPerdidosUsuario(
        @Query("idUsuario") id: Int
    ): Response<List<MascotasPerdidasModel>>

    @POST("api/Publicaciones/editar_perdido.php")
    suspend fun editarPerdido(
        @Body mascota: MascotasPerdidasModel
    ): Response<ApiResponse>

    @POST("api/Publicaciones/eliminar_perdido.php")
    suspend fun eliminarPerdido(
        @Body request: EliminarRequest
    ): Response<ApiResponse>

    @POST("api/Publicaciones/eliminar_encontrado.php")
    suspend fun eliminarEncontrado(
        @Query("id") id: Int
    ): Response<ApiResponse>

    @POST("api/Publicaciones/eliminar_adopcion.php")
    suspend fun eliminarAdopcion(
        @Query("id") id: Int
    ): Response<ApiResponse>

    @Multipart
    @POST("api/Publicaciones/insertar_encontrado.php")
    suspend fun registrarMascotaEncontrada(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part("fecha") fecha: RequestBody,
        @Part("lugar") lugar: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody
    ): Response<ApiResponse>

    @POST("api/Publicaciones/editar_encontrado.php")
    suspend fun editarEncontrado(
        @Body mascota: MascotasEncontradasModel
    ): Response<ApiResponse>

    @GET("api/Publicaciones/obtener_encontrados_usuario.php")
    suspend fun getEncontradosUsuario(
        @Query("idUsuario") id: Int
    ): Response<List<MascotasEncontradasModel>>

    @Multipart
    @POST("api/Publicaciones/insertar_adopcion.php")
    suspend fun registrarMascotaAdopcion(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part("vacunado") vacunado: RequestBody,
        @Part("esterilizado") esterilizado: RequestBody,
        @Part("desparasitado") desparasitado: RequestBody,
        @Part("tamano") tamano: RequestBody,
        @Part("temperamento") temperamento: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part("descripcion") descripcion: RequestBody,
        @Part("nombreOrganizacion") nombreOrganizacion: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody
    ): Response<ApiResponse>

    @GET("api/Publicaciones/obtener_adopciones_usuario.php")
    suspend fun obtenerAdopcionesPorUsuario(
        @Query("idUsuario") id: Int
    ): Response<List<MascotasAdopcionModel>>

    @POST("api/Publicaciones/editar_adopcion.php")
    suspend fun editarAdopcion(
        @Body mascota: MascotasAdopcionModel
    ): Response<ApiResponse>

    //y ahora parte donde muestra publi
    @GET("api/Publicaciones/listarPublicaciones.php")
    suspend fun obtenerPublicaciones():
            Response<List<MostrarPublicaciones>>


}