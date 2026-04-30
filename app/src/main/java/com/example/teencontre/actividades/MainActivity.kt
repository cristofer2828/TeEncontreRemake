package com.example.teencontre.actividades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import com.example.teencontre.ui.theme.TeEncontreTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextFieldDefaults
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf("login") }
            var isDarkMode by remember { mutableStateOf(false) }

            // Envolvemos TODO con tu función de tema
            TeEncontreTheme(darkTheme = isDarkMode, dynamicColor = false) {
                // Surface ahora usará el color de fondo que dicte el tema (claro u oscuro)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = currentScreen) { screen ->
                        when (screen) {
                            "login" -> LoginScreen(
                                onLoginSuccess = { currentScreen = "selector" },
                                onRegisterClick = { currentScreen = "register" }
                            )
                            "register" -> RegisterScreen(
                                onRegisterSuccess = { currentScreen = "login" },
                                onBackToLogin = { currentScreen = "login" },
                                onShowTerms = { isOns ->
                                    currentScreen = if (isOns) "terms_ons" else "terms_user"
                                }
                            )
                            "terms_user" -> TermsFrame(
                                title = "Términos de Usuario",
                                content = textoTerminosUsuario,
                                onBack = { currentScreen = "register" }
                            )
                            "terms_ons" -> TermsFrame(
                                title = "Términos para Organizaciones",
                                content = textoTerminosONS,
                                onBack = { currentScreen = "register" }
                            )
                            "selector" -> CreateAnnouncementScreen(
                                onEncontreClick = { currentScreen = "wizard" },
                                onPerdiClick = { /* Lógica */ },
                                onProfileClick = { currentScreen = "profile" },
                                onPublishClick = { currentScreen = "selector" }
                            )
                            "profile" -> ProfileScreen(
                                onLogout = { currentScreen = "login" },
                                onNavigate = { route -> currentScreen = route }
                            )
                            "settings" -> SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                onBack = { currentScreen = "profile" },
                                onNavigate = { route -> currentScreen = route }
                            )
                        }
                    }
                }
            }
        }
    }
}



// --- PANTALLA: LOGIN ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    // En lugar de un color fijo, usamos el del tema para que sea consistente
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Logo circular: Cambiamos el fondo blanco fijo por surfaceVariant
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_perros),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Ingresar",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Estos ya funcionarán con el tema porque actualizamos la función LoginInput antes
        LoginInput(label = "Correo", value = "", onValueChange = {}, placeholder = "example@email.com")
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(label = "Contraseña", value = "", onValueChange = {}, placeholder = "Contraseña", isPassword = true)

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("INGRESAR ACCESO")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Registrar", color = primaryColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    onShowTerms: (Boolean) -> Unit
) {
    val primaryPurple = Color(0xFF7C4DFF)
    val linkBlue = Color(0xFF2196F3)

    // ESTADOS
    var isOrganization by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // TÍTULO DINÁMICO
        Text(
            text = if (isOrganization) "Registrar ONS" else "Registrar",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        // SWITCH DE VERIFICACIÓN ONS
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isOrganization,
                onCheckedChange = { isOrganization = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verifiquese como centro de adopcion", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FORMULARIO DINÁMICO
        if (isOrganization) {
            LoginInput("Nombre", "", {}, "Nombre de la organizacion")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Número de teléfono", "", {}, "+51")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Correo", "", {}, "example@email.com")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("RUC", "", {}, "RUC")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Direccion", "", {}, "Direccion")
        } else {
            LoginInput("Nombres", "", {}, "Su nombre")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Número de teléfono", "", {}, "+51")
            Spacer(modifier = Modifier.height(16.dp))
            LoginInput("Correo", "", {}, "example@email.com")
        }

        // CAMPOS COMUNES
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput("Contraseña", "", {}, "Contraseña", isPassword = true)
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput("Confirmar contraseña", "", {}, "Confirmar contraseña", isPassword = true)

        // MENSAJE DE ESPERA PARA ONS
        if (isOrganization) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recuerde que la confirmacion tardara maximo 1 semana",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // TÉRMINOS Y CONDICIONES CON NAVEGACIÓN
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it },
                modifier = Modifier.scale(0.7f)
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = if (isOrganization) "Acepto los términos para organizaciones." else "Acepto los términos y condiciones.",
                    fontSize = 12.sp
                )
                TextButton(
                    onClick = { onShowTerms(isOrganization) }, // Abre el frame correspondiente
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(
                        text = "Mas informacion",
                        color = linkBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BOTÓN REGISTRAR
        Button(
            onClick = onRegisterSuccess,
            enabled = acceptedTerms,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Text("Registrar", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onBackToLogin) {
            Text("¿Ya tienes cuenta? Ingresa aquí", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TermsFrame(title: String, content: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = content, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
        ) {
            Text("Entendido y Volver", fontWeight = FontWeight.Bold)
        }
    }
}

val textoTerminosUsuario = """
    1. USO RESPONSABLE: Esta plataforma es exclusivamente para facilitar la adopción y el reencuentro de mascotas.
    
    2. DATOS PERSONALES: Al registrarte, aceptas que tus datos de contacto sean visibles para otros usuarios cuando reportes o busques una mascota.
    
    3. PROHIBICIONES: Está estrictamente prohibido lucrar o vender animales a través de esta aplicación.
    
    4. COMUNIDAD: Nos reservamos el derecho de eliminar cuentas que realicen reportes falsos.
""".trimIndent()

val textoTerminosONS = """
    1. VERIFICACIÓN DE IDENTIDAD: Como organización, es obligatorio proporcionar un RUC válido.
    
    2. TIEMPO DE RESPUESTA: La confirmación de tu centro de adopción tardará un máximo de 1 semana mientras validamos tus datos.
    
    3. COMPROMISO: Te comprometes a garantizar el bienestar de los animales publicados y a mantener la información actualizada.
    
    4. PRIVACIDAD: Tu dirección y RUC serán almacenados de forma segura y usados solo para fines de transparencia institucional.
""".trimIndent()
// --- PANTALLA 2: SELECTOR ---

@Composable
fun CreateAnnouncementScreen(
    onEncontreClick: () -> Unit,
    onPerdiClick: () -> Unit,// Añadimos este parámetro para la lógica de "Perdí"
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    val primaryPurple = Color(0xFF7C4DFF)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = onProfileClick,
                onPublishClick = onPublishClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // MENSAJE EMOTIVO
            Text(
                text = "Cada acción cuenta para que un corazón vuelva a casa.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Crear un anuncio",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text("¿Que ha pasado?", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN: PERDÍ A MI MASCOTA
            Button(
                onClick = onPerdiClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
            ) {
                Text("Perdí a mi mascota", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTÓN: ENCONTRÉ A MI MASCOTA
            OutlinedButton(
                onClick = onEncontreClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryPurple)
            ) {
                Text("Encontre mascota", color = primaryPurple, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onProfileClick: () -> Unit, onPublishClick: () -> Unit) {
    // Definimos los colores basados en el tema actual
    val backgroundColor = MaterialTheme.colorScheme.surface
    val selectedColor = Color(0xFF7C4DFF)
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor) // <--- Aquí ya no es fijo
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationItem(
            icon = android.R.drawable.ic_menu_add,
            label = "Publicar",
            color = unselectedColor,
            onClick = onPublishClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_menu_search,
            label = "Encuentralos",
            color = unselectedColor,
            onClick = { /* Próximamente */ }
        )
        NavigationItem(
            icon = android.R.drawable.ic_dialog_map,
            label = "Mapa",
            color = unselectedColor,
            onClick = { /* Próximamente */ }
        )
        NavigationItem(
            icon = android.R.drawable.ic_menu_myplaces,
            label = "Perfil",
            color = selectedColor,
            onClick = onProfileClick
        )
    }
}

@Composable
fun NavigationItem(icon: Int, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(text = label, fontSize = 10.sp, color = color)
    }
}

@Composable
fun LoginInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            // En tu versión los parámetros son así:
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,

            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),

            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,

            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { /* Ya estamos aquí */ },
                onPublishClick = { onNavigate("selector") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mi perfil",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                // onBackground será blanco en modo oscuro
                color = MaterialTheme.colorScheme.onBackground
            )
            // tarjeta de opciones
            ProfileOptionsCard(onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(32.dp))

            // SECCIÓN: Mis Anuncios
            Text(
                text = "Mis anuncios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Aquí va la lista de anuncios o un mensaje de "vacío"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aún no tienes anuncios publicados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // BOTÓN DE CERRAR SESIÓN
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Cerrar Sesión")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileOptionItem(icon: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            // onSurfaceVariant es oscuro en modo claro y claro en modo oscuro
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var emailNotifications by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { /* Ya estamos en el flujo de perfil */ },
                onPublishClick = { onNavigate("selector") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN ATRÁS
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "← Atrás",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Ajustes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // MODO OSCURO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Modo oscuro",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onDarkModeChange(it) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // SECCIÓN: INFORMACIÓN DE CONTACTO
            Text("Información del contacto", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            LoginInput("Su nombre", "", {}, "Nombre")
            Spacer(modifier = Modifier.height(12.dp))
            LoginInput("Número de teléfono", "", {}, "+51")
            Spacer(modifier = Modifier.height(12.dp))
            LoginInput("Tu correo electrónico", "", {}, "example@email.com")

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it },
                    modifier = Modifier.scale(0.8f)
                )
                Text(
                    "Notificaciones por correo",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // SECCIÓN: SEGURIDAD
            Text("Seguridad", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            LoginInput("Tu contraseña actual", "", {}, "Contraseña", isPassword = true)
            Spacer(modifier = Modifier.height(12.dp))
            LoginInput("Ingrese una nueva contraseña", "", {}, "Nueva contraseña", isPassword = true)
            Spacer(modifier = Modifier.height(12.dp))
            LoginInput("Repita la nueva contraseña", "", {}, "Nueva contraseña", isPassword = true)

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN APLICAR (Adaptado al tema)
            Button(
                onClick = { /* Lógica para guardar cambios */ },
                modifier = Modifier
                    .width(150.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "APLICAR",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileOptionsCard(onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        // containerColor ahora cambia según el tema (Oscuro o Claro)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileOptionItem(
                icon = android.R.drawable.ic_menu_preferences,
                label = "Ajustes",
                onClick = { onNavigate("settings") }
            )

            ProfileOptionItem(
                icon = android.R.drawable.ic_input_add,
                label = "Crear",
                onClick = { onNavigate("selector") }
            )

            ProfileOptionItem(
                icon = android.R.drawable.btn_star_big_on,
                label = "Seleccionado",
                onClick = { /* Lógica futura */ }
            )
        }
    }
}

