package com.example.teencontre.actividades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teencontre.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // CONTROL DE NAVEGACIÓN EXTENDIDO
            var currentScreen by remember { mutableStateOf("login") }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF9F9F9)) {
                Crossfade(targetState = currentScreen) { screen ->
                    when (screen) {
                        "login" -> LoginScreen(
                            onLoginSuccess = { currentScreen = "selector" },
                            onRegisterClick = { currentScreen = "register" } // Nueva conexión
                        )
                        "register" -> RegisterScreen(
                            onRegisterSuccess = { currentScreen = "login" },
                            onBackToLogin = { currentScreen = "login" }
                        )
                        "selector" -> CreateAnnouncementScreen(onEncontreClick = { currentScreen = "wizard" })
                        "wizard" -> FoundPetWizard()
                    }
                }
            }
        }
    }
}

// --- PANTALLA: LOGIN ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    val primaryPurple = Color(0xFF7C4DFF)
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        // Logo circular de TeEncontre
        Box(modifier = Modifier.size(250.dp).clip(RoundedCornerShape(20.dp)).background(Color.White)) {
            Image(
                painter = painterResource(id = R.drawable.logo_perros),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text("Ingresar", modifier = Modifier.fillMaxWidth(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        LoginInput(label = "Correo", value = "", onValueChange = {}, placeholder = "example@email.com")
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(label = "Contraseña", value = "", onValueChange = {}, placeholder = "Contraseña", isPassword = true)

        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Text("INGRESAR Acceso")
        }

        // Botón para ir a registrarse
        TextButton(onClick = onRegisterClick) {
            Text("Registrar", color = primaryPurple, fontWeight = FontWeight.Bold)
        }
    }
}

// --- PANTALLA: REGISTRAR ---
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    val primaryPurple = Color(0xFF7C4DFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState()), // Por si los campos no caben
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Registrar",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Campos según tu diseño
        LoginInput(label = "Nombres", value = "", onValueChange = {}, placeholder = "Su nombre")
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(label = "Número de teléfono", value = "", onValueChange = {}, placeholder = "+51")
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(label = "Correo", value = "", onValueChange = {}, placeholder = "example@email.com")
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(label = "Contraseña", value = "", onValueChange = {}, placeholder = "Contraseña", isPassword = true)
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(label = "Confirmar contraseña", value = "", onValueChange = {}, placeholder = "Confirmar contraseña", isPassword = true)

        Spacer(modifier = Modifier.height(40.dp))

        // Botón principal de Registro
        Button(
            onClick = onRegisterSuccess,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Text("Registrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onBackToLogin) {
            Text("¿Ya tienes cuenta? Ingresa aquí", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// --- RESTO DE FUNCIONES (CreateAnnouncementScreen, FoundPetWizard, LoginInput) SE MANTIENEN IGUAL ---

// --- PANTALLA 2: SELECTOR ---
@Composable
fun CreateAnnouncementScreen(onEncontreClick: () -> Unit) {
    val primaryPurple = Color(0xFF7C4DFF)
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear un anuncio", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))
        Text("¿Que ha pasado?", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { /* Lógica Perdí */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Text("Perdí a mi mascota")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onEncontreClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, primaryPurple)
        ) {
            Text("Encontre a mi mascota", color = primaryPurple)
        }
    }
}

// --- PANTALLA 3: WIZARD (PASOS) ---
@Composable
fun FoundPetWizard() {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("Found Pet $currentStep/$totalSteps", color = Color.Gray, fontSize = 14.sp)
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Color(0xFF7C4DFF),
            trackColor = Color(0xFFEEEEEE)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                1 -> StepOneContent()
                else -> Text("Contenido del paso $currentStep", modifier = Modifier.align(Alignment.Center))
            }
        }

        Button(
            onClick = { if (currentStep < totalSteps) currentStep++ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
        ) {
            Text("Próximo")
        }
    }
}

@Composable
fun StepOneContent() {
    Column {
        Text("Encontré una mascota", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("¿Qué se encontró?", color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))
        LoginInput("Animal", "", {}, "Ej. Perro")
    }
}

// --- COMPONENTE REUTILIZABLE: INPUT ---
@Composable
fun LoginInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, isPassword: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF7C4DFF),
                unfocusedBorderColor = Color.LightGray
            )
        )
    }
}