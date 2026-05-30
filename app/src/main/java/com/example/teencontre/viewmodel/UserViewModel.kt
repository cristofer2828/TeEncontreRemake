package com.example.teencontre.viewmodel

import androidx.lifecycle.ViewModel
import com.example.teencontre.data.model.BaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    // Este es el "cerebro" que guarda al usuario actual
    private val _currentUser = MutableStateFlow<BaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Función para actualizar el usuario cuando hace login
    fun setUser(user: BaseUser) {
        _currentUser.value = user
    }

    // Función para borrar el usuario cuando hace logout
    fun logout() {
        _currentUser.value = null
    }
}