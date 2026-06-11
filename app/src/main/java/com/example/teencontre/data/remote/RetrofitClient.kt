package com.example.teencontre.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "https://tencontre-hkfrcbh9d6fhepdu.canadacentral-01.azurewebsites.net/"

    val instance: AzureApiService by lazy {

        val logging = HttpLoggingInterceptor {
            Log.d("RETROFIT", it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val customGson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.java, JsonDeserializer<Boolean> { json, _, _ ->
                if (json.isJsonPrimitive) {
                    val primitive = json.asJsonPrimitive

                    if (primitive.isBoolean)
                        return@JsonDeserializer primitive.asBoolean

                    if (primitive.isNumber)
                        return@JsonDeserializer primitive.asInt == 1

                    if (primitive.isString)
                        return@JsonDeserializer (
                                primitive.asString == "1" ||
                                        primitive.asString.lowercase() == "true"
                                )
                }
                false
            })
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(customGson)
            )
            .build()
            .create(AzureApiService::class.java)
    }
}