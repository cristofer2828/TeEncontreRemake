package com.example.teencontre.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teencontre.data.model.MostrarPublicaciones
import com.example.teencontre.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class PublicacionesViewModel : ViewModel() {

    var publicaciones by mutableStateOf<List<MostrarPublicaciones>>(emptyList())
        private set

    init {
        cargarPublicaciones()
    }

    private fun cargarPublicaciones() {

        viewModelScope.launch {

            try {

                val response =
                    RetrofitClient
                        .instance
                        .obtenerPublicaciones()

                if (response.isSuccessful) {

                    publicaciones =
                        response.body() ?: emptyList()

                } else {

                    println("Error HTTP: ${response.code()}")
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }
}