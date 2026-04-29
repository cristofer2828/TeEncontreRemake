package com.example.teencontre.actividades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teencontre.sharedprefs.PreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VistaPreferenciasPanda()
        }
    }
}

@Composable
fun VistaPreferenciasPanda() {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    var isDarkMode by remember { mutableStateOf(prefs.isDarkMode()) }
    var currentLang by remember { mutableStateOf(prefs.getLanguage()) }
    var userName by remember { mutableStateOf(prefs.getUserName()) }
    var phone by remember { mutableStateOf(prefs.getPhone()) }
    var email by remember { mutableStateOf(prefs.getEmail()) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val es = currentLang == "es"
    val title = if (es) "Mi Perfil Panda" else "My Panda Profile"
    val applyBtn = if (es) "GUARDAR CAMBIOS" else "SAVE CHANGES"

    val pandaColors = if (isDarkMode) {
        darkColorScheme(primary = Color.White, background = Color.Black, surface = Color(0xFF1A1A1A))
    } else {
        lightColorScheme(primary = Color.Black, background = Color.White, surface = Color(0xFFF0F0F0))
    }

    MaterialTheme(
        colorScheme = pandaColors,
        // CORRECCIÓN: Uso correcto de Shapes en Material3
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(24.dp),
            large = RoundedCornerShape(32.dp)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                PandaCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (es) "Tema Oscuro" else "Dark Mode", fontWeight = FontWeight.Bold)
                        Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it; prefs.setDarkMode(it) })
                    }

                    Button(
                        onClick = {
                            val newLang = if (currentLang == "es") "en" else "es"
                            currentLang = newLang
                            prefs.setLanguage(newLang)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("${if (es) "Idioma" else "Language"}: ${currentLang.uppercase()}")
                    }
                }

                Text(if (es) "Información" else "Information", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

                PandaInput(value = userName, onValueChange = { userName = it; prefs.setUserName(it) }, label = if (es) "Nombre" else "Name")

                PandaInput(
                    value = phone,
                    onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) { phone = it; prefs.setPhone(it) } },
                    label = if (es) "Teléfono" else "Phone",
                    placeholder = "999888777"
                )

                PandaInput(value = email, onValueChange = { email = it; prefs.setEmail(it) }, label = "Email")

                Text(if (es) "Seguridad" else "Security", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

                // CORRECCIÓN: Campo de contraseña oculto
                PandaInput(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = if (es) "Contraseña Actual" else "Current Password",
                    isPassword = true
                )

                PandaInput(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = if (es) "Nueva Contraseña" else "New Password",
                    isPassword = true
                )

                Button(
                    onClick = { /* Lógica de guardado opcional */ },
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 8.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(applyBtn, fontWeight = FontWeight.Bold, color = if(isDarkMode) Color.Black else Color.White)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun PandaCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
fun PandaInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        // Versión compatible de colores:
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = Color.Transparent,   // Cambiado aquí
            unfocusedContainerColor = Color.Transparent, // Cambiado aquí
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}