package com.example.teencontre.actividades
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.teencontre.sharedprefs.PreferenceManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.model.BaseUser
import com.example.teencontre.data.model.MascotasAdopcionModel
import com.example.teencontre.data.model.MascotasEncontradasModel
import com.example.teencontre.data.model.MascotasPerdidasModel
import com.example.teencontre.data.model.Organizacion
import com.example.teencontre.data.model.Usuario
import com.example.teencontre.data.remote.RetrofitClient
import com.example.teencontre.screens.EditPerdidoScreen
import com.example.teencontre.screens.EditEncontradaScreen
import com.example.teencontre.screens.EditAdopcionScreen
import com.example.teencontre.screens.WizardMascotaPerdida
import com.example.teencontre.screens.WizardEncontreAnuncio
import com.example.teencontre.screens.WizardCrearAdopcion
import com.example.teencontre.viewmodel.UserViewModel
import android.util.Log
import com.example.teencontre.data.model.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.teencontre.data.model.RegisterRequest
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import com.example.teencontre.data.model.UpdateUserRequest
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import com.example.teencontre.data.model.EliminarRequest
import androidx.core.database.sqlite.transaction

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            val currentUser by userViewModel.currentUser.collectAsState()
            val context = LocalContext.current

            // Inicializamos PreferenceManager una sola vez para toda la App
            val prefs = remember { PreferenceManager(context) }
            // Estados globales de navegación y configuración
            var currentScreen by remember { mutableStateOf("login") }
            var selectedMascotaId by remember { mutableStateOf(0) }
            var wizardMode by remember { mutableStateOf("perdi") }
            var isDarkMode by remember { mutableStateOf(false) }
            var direccionSeleccionada by remember {
                mutableStateOf<String?>(null)
            }

            TeEncontreTheme(darkTheme = isDarkMode, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = currentScreen, label = "main_navigation") { screen ->
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
                            "selector" -> {
                                CreateAnnouncementScreen(
                                    onEncontreClick = {
                                        wizardMode = "encontre"
                                        currentScreen = "wizard"
                                    },
                                    onPerdiClick = {
                                        wizardMode = "perdi"
                                        currentScreen = "wizard"
                                    },
                                    onAdopcionClick = {
                                        wizardMode = "adopcion"
                                        currentScreen = "wizard"
                                    },
                                    onProfileClick = { currentScreen = "profile" },
                                    onPublishClick = { currentScreen = "selector" },
                                    onNavigate = { currentScreen = it }
                                )
                            }
                            "wizard" -> {
                                when (wizardMode) {
                                    "perdi" -> WizardMascotaPerdida(
                                        onBackToSelector = { currentScreen = "selector" }
                                    )
                                    "encontre" -> WizardEncontreAnuncio(
                                        onBackToSelector = { currentScreen = "selector" }
                                    )
                                    "adopcion" -> WizardCrearAdopcion(
                                        onBackToSelector = { currentScreen = "selector" }
                                    )
                                }
                            }
                                "profile" -> ProfileScreen(
                                    prefs = prefs,
                                    onLogout = {
                                        userViewModel.logout()
                                        currentScreen = "login"
                                    },
                                    onNavigate = { route ->
                                        when {
                                            route.startsWith("editar_perdido/") -> {
                                                selectedMascotaId = route.substringAfter("editar_perdido/").toIntOrNull() ?: 0
                                                currentScreen = "editar_perdido"
                                            }
                                            route.startsWith("editar_encontrada/") -> {
                                                selectedMascotaId = route.substringAfter("editar_encontrada/").toIntOrNull() ?: 0
                                                currentScreen = "editar_encontrada"
                                            }
                                            route.startsWith("editar_adopcion/") -> {
                                                selectedMascotaId = route.substringAfter("editar_adopcion/").toIntOrNull() ?: 0
                                                currentScreen = "editar_adopcion"
                                            }
                                            else -> currentScreen = route
                                        }
                                    }
                                )

                                // CORRECCIÓN: Inyección obligatoria de idMascota a las pantallas de edición
                                "editar_perdido" -> EditPerdidoScreen(
                                    idMascota = selectedMascotaId,
                                    onBack = { currentScreen = "profile" }
                                )
                                "editar_encontrada" -> EditEncontradaScreen(
                                    idMascota = selectedMascotaId,
                                    onBack = { currentScreen = "profile" }
                                )
                                "editar_adopcion" -> EditAdopcionScreen(
                                    idMascota = selectedMascotaId,
                                    onBack = { currentScreen = "profile" }
                                )

                            "settings" -> SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                onBack = { currentScreen = "profile" },
                                onNavigate = { route -> currentScreen = route },
                                onLogout = { currentScreen = "login" }
                            )
                            "encuentranos" -> EncuentranosScreen(
                                onProfileClick = { currentScreen = "profile" },
                                onPublishClick = { currentScreen = "selector" },
                                onNavigate = { currentScreen = it }
                            )
                            "mapa" -> MapScreen(
                                direccionPublicacion = direccionSeleccionada,
                                onNavigate = { currentScreen = it },
                                onProfileClick = { currentScreen = "profile" },
                                onPublishClick = { currentScreen = "selector" }
                            )
                            "detalle_anuncio" -> DetalleAnuncioScreen(

                                onBack = {
                                    currentScreen = "encuentranos"
                                },

                                onVerUbicacion = { lugar ->

                                    direccionSeleccionada = lugar
                                    currentScreen = "mapa"
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
                        }
                    }
                }
            }
        }
    }
}


// --- PANTALLA: LOGIN ---
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {

    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    val primaryColor = MaterialTheme.colorScheme.primary

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier.size(250.dp)
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.logo_perros
                ),
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

        LoginInput(
            label = "Correo",
            value = correo,
            onValueChange = { correo = it },
            placeholder = "example@email.com"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginInput(
            label = "Contraseña",
            value = password,
            onValueChange = { password = it },
            placeholder = "Contraseña",
            isPassword = !passwordVisible,
            trailingIcon = {

                val icon =
                    if (passwordVisible)
                        android.R.drawable.ic_menu_view
                    else
                        android.R.drawable.ic_menu_close_clear_cancel

                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Ver contraseña",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(

            onClick = {

                Log.d("LOGIN", "Botón presionado")

                if (correo.isBlank() || password.isBlank()) {

                    Log.e(
                        "LOGIN",
                        "Campos vacíos"
                    )

                    return@Button
                }

                Log.d(
                    "LOGIN",
                    "Email ingresado: $correo"
                )

                CoroutineScope(Dispatchers.IO).launch {

                    try {

                        val request = LoginRequest(
                            email = correo,
                            contrasena = password
                        )

                        Log.d(
                            "LOGIN",
                            "Enviando petición al servidor..."
                        )

                        val response =
                            RetrofitClient
                                .instance
                                .login(request)

                        Log.d(
                            "LOGIN",
                            "Código HTTP: ${response.code()}"
                        )

                        Log.d(
                            "LOGIN",
                            "Mensaje HTTP: ${response.message()}"
                        )

                        if (!response.isSuccessful) {

                            Log.e(
                                "LOGIN",
                                "ErrorBody: ${response.errorBody()?.string()}"
                            )
                        }

                        withContext(Dispatchers.Main) {

                            if (response.isSuccessful) {

                                val usuario = response.body()

                                Log.d(
                                    "LOGIN",
                                    "Respuesta recibida: $usuario"
                                )

                                if (usuario == null) {

                                    Log.e(
                                        "LOGIN",
                                        "Retrofit recibió NULL"
                                    )

                                    return@withContext
                                }

                                Log.d(
                                    "LOGIN",
                                    "Tipo usuario: ${usuario.tipo}"
                                )

                                if (usuario.tipo == "USUARIO") {

                                    Log.d(
                                        "LOGIN",
                                        "Guardando usuario normal"
                                    )

                                    prefs.saveLoggedUser(

                                        Usuario(
                                            id = usuario.id,
                                            email = usuario.email,
                                            nombre = usuario.nombre ?: "",
                                            telefono = usuario.telefono ?: ""
                                        )
                                    )

                                } else {

                                    Log.d(
                                        "LOGIN",
                                        "Guardando organización"
                                    )

                                    prefs.saveLoggedUser(

                                        Organizacion(
                                            id = usuario.id,
                                            email = usuario.email,
                                            nombreOrg = usuario.nombreOrg ?: "",
                                            ruc = usuario.ruc ?: "",
                                            direccion = usuario.direccion ?: "",
                                            esVerificada = usuario.esVerificada ?: false
                                        )
                                    )
                                }

                                Log.d(
                                    "LOGIN",
                                    "Login exitoso"
                                )

                                onLoginSuccess()

                            } else {

                                Log.e(
                                    "LOGIN",
                                    "Credenciales incorrectas"
                                )
                            }
                        }

                    } catch (e: Exception) {

                        withContext(Dispatchers.Main) {

                            Log.e(
                                "LOGIN",
                                "EXCEPCIÓN COMPLETA"
                            )

                            Log.e(
                                "LOGIN",
                                Log.getStackTraceString(e)
                            )
                        }
                    }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            shape = RoundedCornerShape(12.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )

        ) {

            Text("Ingresar")
        }

        TextButton(
            onClick = onRegisterClick
        ) {

            Text(
                text = "Registrar",
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
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
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var ruc by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
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
            Text("Verifíquese como centro de adopción", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FORMULARIO DINÁMICO
        if (isOrganization) {
            LoginInput(
                "Nombre",
                nombre,
                { nombre = it },
                "nombre de la organización"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                "Número de teléfono",
                telefono,
                { telefono = it },
                "+51"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                "Correo",
                email,
                { email = it },
                "example@email.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                "RUC",
                ruc,
                { ruc = it },
                "RUC"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                "Dirección",
                direccion,
                { direccion = it },
                "Dirección"
            )
        } else {
            LoginInput(
                "Nombres",
                nombre,
                { nombre = it },
                "Su nombre"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                "Número de teléfono",
                telefono,
                { telefono = it },
                "+51"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                "Correo",
                email,
                { email = it },
                "example@email.com"
            )
        }

        // CAMPOS COMUNES
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(
            "Contraseña",
            password,
            { password = it },
            "Contraseña",
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        LoginInput(
            "Confirmar contraseña",
            confirmPassword,
            { confirmPassword = it },
            "Confirmar contraseña",
            isPassword = true
        )

        // MENSAJE DE ESPERA PARA ONS
        if (isOrganization) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "confirmación tardará máximo 1 semana.",
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
                        text = "Más información",
                        color = linkBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BOTÓN REGISTRAR
        Button(
            onClick = {

                if(password != confirmPassword){

                    Log.e(
                        "REGISTER",
                        "Las contraseñas no coinciden"
                    )

                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {

                    try {

                        val request = RegisterRequest(
                            nombre = nombre,
                            telefono = telefono,
                            email = email,
                            contrasena = password,
                            ruc = if(isOrganization) ruc else null,
                            direccion = if(isOrganization) direccion else null,
                            esOrganizacion = isOrganization
                        )

                        val response =
                            RetrofitClient
                                .instance
                                .register(request)

                        withContext(Dispatchers.Main){

                            if(response.isSuccessful){

                                Log.d(
                                    "REGISTER",
                                    "Registro exitoso"
                                )

                                onRegisterSuccess()

                            }else {

                                Log.e(
                                    "REGISTER",
                                    "Codigo HTTP: ${response.code()}"
                                )

                                Log.e(
                                    "REGISTER",
                                    "Respuesta: ${response.errorBody()?.string()}"
                                )
                            }
                        }

                    }catch (e: Exception){

                        Log.e(
                            "REGISTER",
                            "EXCEPTION",
                            e
                        )
                    }
                }
            },

            enabled = acceptedTerms,

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            shape = RoundedCornerShape(12.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = primaryPurple
            )
        ) {
            Text(
                "Registrar",
                fontWeight = FontWeight.Bold
            )
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
            Text("Entendido y volver", fontWeight = FontWeight.Bold)
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
    onPerdiClick: () -> Unit,
    onAdopcionClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val primaryPurple = MaterialTheme.colorScheme.primary
    var showDialog by remember { mutableStateOf(false) }
// Guarda la ruta de la pantalla que se está mostrando actualmente
    val context = LocalContext.current

    val prefs = remember {
        PreferenceManager(context)
    }

    val usuario = remember {
        prefs.getLoggedUser()
    }

    val puedePublicarAdopcion =
        usuario is Organizacion &&
                usuario.esVerificada

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        var pantallaActiva by remember { mutableStateOf("publicar") }
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = pantallaActiva,
                    onProfileClick = onProfileClick,
                    onPublishClick = onPublishClick,
                    onEncuentranosClick = { onNavigate("encuentranos") },
                    onMapaClick = { onNavigate("mapa") }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Crear un anuncio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(30.dp))

                if (usuario is Organizacion) {

                    Text(
                        text =
                            if (usuario.esVerificada)
                                "✓ Organización verificada"
                            else
                                "⏳ Organización pendiente de verificación",
                        color =
                            if (usuario.esVerificada)
                                Color(0xFF4CAF50)
                            else
                                Color.Red,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                        "¿Qué ha pasado?",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN: PERDÍ A MI MASCOTA
                Button(
                    onClick = onPerdiClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) {
                    Text("Perdí a mi mascota.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN: ENCONTRÉ A MI MASCOTA
                OutlinedButton(
                    onClick = onEncontreClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF4CAF50), // Fondo verde
                        contentColor = Color.White           // Texto blanco
                    )
                ) {
                    Text("Encontré mascota",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN: ADOPCIÓN (Bloqueado/mediantesharepref)
                // Solo mostrar a organizaciones verificadas
                if (puedePublicarAdopcion) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onAdopcionClick()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                        shape = RoundedCornerShape(12.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF03A9F4),
                            contentColor = Color.White
                        )
                    ) {

                        Text(
                            text = "Adopción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                }
            }
        }

        // --- BURBUJA FLOTANTE ---
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 45.dp, start = 0.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.adopta),
                contentDescription = "Adopta",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }

    // DIÁLOGO
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Adopta con responsabilidad.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Adoptar una mascota es un compromiso de amor, cuidado y respeto.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Entendido", color = primaryPurple)
                }
            }
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String, // <-- Este parámetro recibe el valor de "pantallaActiva"
    onPublishClick: () -> Unit,
    onEncuentranosClick: () -> Unit,
    onMapaClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val selectedColor = Color(0xFF7C4DFF) // Morado de selección
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationItem(
            icon = android.R.drawable.ic_menu_add, // Reemplázalo por tu pack de iconos si usas personalizados
            label = "Publicar",
            // IMPORTANTE: Asegúrate de que el String "publicar" coincida con lo que guarda tu variable en esa sección
            color = if (currentRoute == "publicar") selectedColor else unselectedColor,
            onClick = onPublishClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_menu_search,
            label = "Encuéntralos",
            color = if (currentRoute == "encuentranos") selectedColor else unselectedColor,
            onClick = onEncuentranosClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_dialog_map,
            label = "Mapa",
            color = if (currentRoute == "mapa") selectedColor else unselectedColor,
            onClick = onMapaClick
        )
        NavigationItem(
            icon = android.R.drawable.ic_menu_myplaces,
            label = "Perfil",
            color = if (currentRoute == "perfil") selectedColor else unselectedColor,
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
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val customKeyboardOptions = if (isPassword) {
        keyboardOptions.copy(keyboardType = KeyboardType.Password)
    } else {
        keyboardOptions
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = customKeyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
@Composable
fun ProfileScreen(
    prefs: PreferenceManager,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    val usuarioLogueado: BaseUser? = remember { prefs.getLoggedUser() }
    val idUserActual = usuarioLogueado?.id ?: 0

    var listaPerdidos by remember { mutableStateOf<List<MascotasPerdidasModel>>(emptyList()) }
    var listaEncontrados by remember { mutableStateOf<List<MascotasEncontradasModel>>(emptyList()) }
    var listaAdopciones by remember { mutableStateOf<List<MascotasAdopcionModel>>(emptyList()) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val apiService = RetrofitClient.instance
    val db = dbHelper.readableDatabase

    val cursor = db.rawQuery(
        "SELECT * FROM ${DatabaseHelper.TABLE_PERDIDOS}",
        null
    )

    if (cursor.moveToFirst()) {
        do {
            Log.d(
                "CACHE_PERDIDOS",
                "ID=${cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_ID))} " +
                        "Usuario=${cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_USER_ID))} " +
                        "Nombre=${cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_NOMBRE))}"
            )
        } while (cursor.moveToNext())
    }

    cursor.close()
    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            try {
                val idUser = usuarioLogueado?.id ?: 0

                Log.d("PROBANDO_RED", "Llamando a Perdidos de Azure...")
                val responsePerdidos = apiService.getPerdidosUsuario(idUser)
                val perdidosOnline = if (responsePerdidos.isSuccessful) {
                    responsePerdidos.body() ?: emptyList()
                } else emptyList()
                perdidosOnline.forEach {
                    Log.d(
                        "AZURE_DEBUG",
                        "ID=${it.id}, Usuario=${it.idUsuario}, Nombre=${it.nombreM}"
                    )
                }

                Log.d("PROBANDO_RED", "Llamando a Encontrados de Azure...")
                val responseEncontrados = apiService.getEncontradosUsuario(idUser)
                val encontradosOnline = if (responseEncontrados.isSuccessful) {
                    responseEncontrados.body() ?: emptyList()
                } else emptyList()

                Log.d("PROBANDO_RED", "Llamando a Adopciones usando obtener_adopciones_usuario.php...")

                val responseAdopcion = apiService.obtenerAdopcionesPorUsuario(idUser)

                val adopcionesOnline = if (responseAdopcion.isSuccessful) {
                    // responseAdopcion.body() ya contiene directamente la List<MascotasAdopcionModel>
                    responseAdopcion.body() ?: emptyList()

                } else {
                    Log.e("PROBANDO_RED", "Error en respuesta de adopciones: ${responseAdopcion.code()}")
                    emptyList()
                }

                // Actualizar UI de inmediato con lo que viene de la red remota de Azure
                withContext(Dispatchers.Main) {
                    listaPerdidos = perdidosOnline
                    listaEncontrados = encontradosOnline
                    listaAdopciones = adopcionesOnline
                }
                val db = dbHelper.writableDatabase
                db.transaction {
                    try {
                        // Sincronizar Caché de Perdidos
                        delete(
                            DatabaseHelper.TABLE_PERDIDOS,
                            "${DatabaseHelper.PERDIDO_USER_ID} = ?",
                            arrayOf(idUserActual.toString())
                        )
                        perdidosOnline.forEach { mascota ->
                            val values = ContentValues().apply {
                                put(DatabaseHelper.PERDIDO_ID, mascota.id)
                                put(DatabaseHelper.PERDIDO_USER_ID, mascota.idUsuario)
                                put(DatabaseHelper.PERDIDO_NOMBRE, mascota.nombreM ?: "")
                                put(DatabaseHelper.PERDIDO_ESPECIE, mascota.especie ?: "")
                                put(DatabaseHelper.PERDIDO_GENERO, mascota.genero ?: "")
                                put(DatabaseHelper.PERDIDO_RAZA, mascota.raza ?: "")

                                val fotoBytes: ByteArray? = when (val fotoRaw = mascota.foto) {
                                    is ByteArray -> fotoRaw
                                    else -> null
                                }
                                put(DatabaseHelper.PERDIDO_FOTO, fotoBytes)

                                // Aquí continúa el resto de tus campos (fecha, lugar, descripción, etc.)
                                put(DatabaseHelper.PERDIDO_FECHA, mascota.fecha ?: "")
                                put(DatabaseHelper.PERDIDO_LUGAR, mascota.lugar ?: "")
                                put(DatabaseHelper.PERDIDO_DESCRIPCION, mascota.descripcion ?: "")
                                put(DatabaseHelper.PERDIDO_CONTACTO, mascota.contacto ?: "")
                                put(DatabaseHelper.PERDIDO_TELEFONO, mascota.telefono ?: "")
                                put(DatabaseHelper.PERDIDO_CORREO, mascota.correo ?: "")
                            }
                            Log.d(
                                "SYNC_AZURE",
                                "ID=${mascota.id} FOTO=${mascota.foto}"
                            )
                            Log.d(
                                "SYNC_AZURE",
                                "TOTAL PERDIDOS=${perdidosOnline.size}"
                            )
                            insertWithOnConflict(
                                DatabaseHelper.TABLE_PERDIDOS,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE
                            )
                        }

                        // Sincronizar Caché de Encontrados
                        delete(
                            DatabaseHelper.TABLE_ENCONTRADOS,
                            "${DatabaseHelper.ENCONTRADO_USER_ID} = ?",
                            arrayOf(idUserActual.toString())
                        )
                        encontradosOnline.forEach { mascota ->
                            val values = ContentValues().apply {
                                put(DatabaseHelper.ENCONTRADO_ID, mascota.id)
                                put(DatabaseHelper.ENCONTRADO_USER_ID, mascota.idUsuario)
                                put(DatabaseHelper.ENCONTRADO_ESPECIE, mascota.especie ?: "")
                                put(DatabaseHelper.ENCONTRADO_GENERO, mascota.genero ?: "")

                                // CORREGIDO: Añadido 'as Any?' para permitir la evaluación dinámica de tipo
                                val fotoBytes: ByteArray? = when (val fotoRaw = mascota.foto) {
                                    is ByteArray -> fotoRaw
                                    else -> null
                                }
                                put(DatabaseHelper.ENCONTRADO_FOTO, fotoBytes)

                                put(DatabaseHelper.ENCONTRADO_FECHA, mascota.fecha ?: "")
                                put(DatabaseHelper.ENCONTRADO_LUGAR, mascota.lugar ?: "")
                                put(
                                    DatabaseHelper.ENCONTRADO_DESCRIPCION,
                                    mascota.descripcion ?: ""
                                )
                                put(DatabaseHelper.ENCONTRADO_CONTACTO, mascota.contacto ?: "")
                                put(DatabaseHelper.ENCONTRADO_TELEFONO, mascota.telefono ?: "")
                                put(DatabaseHelper.ENCONTRADO_CORREO, mascota.correo ?: "")
                            }
                            insertWithOnConflict(
                                DatabaseHelper.TABLE_ENCONTRADOS,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE
                            )
                        }

                        // Sincronizar Caché de Adopciones
                        delete(
                            DatabaseHelper.TABLE_ADOPCION,
                            "${DatabaseHelper.ADOPCION_USER_ID} = ?",
                            arrayOf(idUserActual.toString())
                        )
                        adopcionesOnline.forEach { mascota ->
                            val values = ContentValues().apply {
                                put(DatabaseHelper.ADOPCION_ID, mascota.id)
                                put(DatabaseHelper.ADOPCION_USER_ID, mascota.idUsuario)
                                put(DatabaseHelper.ADOPCION_ESPECIE, mascota.especie ?: "")
                                put(DatabaseHelper.ADOPCION_GENERO, mascota.genero ?: "")
                                put(DatabaseHelper.ADOPCION_RAZA, mascota.raza ?: "")
                                put(
                                    DatabaseHelper.ADOPCION_VACUNADO,
                                    if (mascota.vacunado) 1 else 0
                                )
                                put(
                                    DatabaseHelper.ADOPCION_ESTERILIZADO,
                                    if (mascota.esterilizado) 1 else 0
                                )
                                put(
                                    DatabaseHelper.ADOPCION_DESPARASITADO,
                                    if (mascota.desparasitado) 1 else 0
                                )
                                put(DatabaseHelper.ADOPCION_TAMANO, mascota.tamano ?: "")
                                put(
                                    DatabaseHelper.ADOPCION_TEMPERAMENTO,
                                    mascota.temperamento ?: ""
                                )

                                val fotoBytes: ByteArray? = when (val fotoRaw = mascota.foto) {
                                    is ByteArray -> fotoRaw
                                    else -> null
                                }
                                put(DatabaseHelper.ADOPCION_FOTO, fotoBytes)

                                put(DatabaseHelper.ADOPCION_DESCRIPCION, mascota.descripcion ?: "")
                                put(
                                    DatabaseHelper.ADOPCION_ORGANIZACION,
                                    mascota.nombreOrganizacion ?: ""
                                )
                                put(DatabaseHelper.ADOPCION_TELEFONO, mascota.telefono ?: "")
                                put(DatabaseHelper.ADOPCION_CORREO, mascota.correo ?: "")
                            }
                            insertWithOnConflict(
                                DatabaseHelper.TABLE_ADOPCION,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE
                            )
                        }
                        val c = rawQuery(
                            "SELECT id,idUsuario,nombreM FROM addPerdido WHERE idUsuario=?",
                            arrayOf(idUserActual.toString())
                        )

                        while (c.moveToNext()) {
                            Log.d(
                                "CACHE_GUARDADA",
                                "ID=${c.getInt(0)} Usuario=${c.getInt(1)} Nombre=${c.getString(2)}"
                            )
                        }
                        c.close()
                        Log.d("SYNC", "Datos localmente sincronizados exitosamente desde Azure.")
                    } finally {
                    }
                }

            } catch (e: Exception) {
                Log.e("OFFLINE", "Sin internet, cargando datos locales filtrados por usuario", e)
                val db = dbHelper.readableDatabase

                val listaP = mutableListOf<MascotasPerdidasModel>()
                val cursorP = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PERDIDOS} WHERE ${DatabaseHelper.PERDIDO_USER_ID} = ?", arrayOf(idUserActual.toString()))
                if (cursorP.moveToFirst()) {
                    do {
                        listaP.add(MascotasPerdidasModel(
                            id = cursorP.getInt(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_ID)),
                            idUsuario = cursorP.getInt(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_USER_ID)),
                            nombreM = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_NOMBRE)),
                            especie = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_ESPECIE)),
                            genero = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_GENERO)),
                            raza = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_RAZA)),
                            foto = cursorP.getBlob(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_FOTO)),
                            fecha = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_FECHA)),
                            lugar = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_LUGAR)),
                            descripcion = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_DESCRIPCION)),
                            contacto = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_CONTACTO)),
                            telefono = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_TELEFONO)),
                            correo = cursorP.getString(cursorP.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_CORREO))
                        ))
                    } while (cursorP.moveToNext())
                }
                cursorP.close()

                val listaE = mutableListOf<MascotasEncontradasModel>()
                val cursorE = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ENCONTRADOS} WHERE ${DatabaseHelper.ENCONTRADO_USER_ID} = ?", arrayOf(idUserActual.toString()))
                if (cursorE.moveToFirst()) {
                    do {
                        listaE.add(MascotasEncontradasModel(
                            id = cursorE.getInt(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_ID)),
                            idUsuario = cursorE.getInt(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_USER_ID)),
                            especie = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_ESPECIE)),
                            genero = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_GENERO)),
                            foto = cursorE.getBlob(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_FOTO)),
                            fecha = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_FECHA)),
                            lugar = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_LUGAR)),
                            descripcion = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_DESCRIPCION)),
                            contacto = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_CONTACTO)),
                            telefono = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_TELEFONO)),
                            correo = cursorE.getString(cursorE.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_CORREO))
                        ))
                    } while (cursorE.moveToNext())
                }
                cursorE.close()

                val listaA = mutableListOf<MascotasAdopcionModel>()
                val cursorA = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ADOPCION} WHERE ${DatabaseHelper.ADOPCION_USER_ID} = ?", arrayOf(idUserActual.toString()))
                if (cursorA.moveToFirst()) {
                    do {
                        listaA.add(MascotasAdopcionModel(
                            id = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ID)),
                            idUsuario = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_USER_ID)),
                            especie = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ESPECIE)),
                            genero = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_GENERO)),
                            raza = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_RAZA)),
                            vacunado = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_VACUNADO)) == 1,
                            esterilizado = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ESTERILIZADO)) == 1,
                            desparasitado = cursorA.getInt(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_DESPARASITADO)) == 1,
                            tamano = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TAMANO)),
                            temperamento = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TEMPERAMENTO)),
                            foto = cursorA.getBlob(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_FOTO)),
                            descripcion = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_DESCRIPCION)),
                            nombreOrganizacion = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ORGANIZACION)),
                            telefono = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TELEFONO)),
                            correo = cursorA.getString(cursorA.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_CORREO))
                        ))
                    } while (cursorA.moveToNext())
                }
                cursorA.close()

                withContext(Dispatchers.Main) {
                    listaPerdidos = listaP
                    listaEncontrados = listaE
                    listaAdopciones = listaA
                }
            }
        }
    }

    val totalAnuncios = listaPerdidos.size + listaEncontrados.size + listaAdopciones.size

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "perfil", // <-- Agregado para activar el botón de Perfil
                onProfileClick = { refreshTrigger++ },
                onPublishClick = { onNavigate("selector") },
                onEncuentranosClick = { onNavigate("encuentranos") },
                onMapaClick = { onNavigate("mapa") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))

                // --- SECCIÓN ENCABEZADO DE USUARIO ---
                when (usuarioLogueado) {
                    is Usuario -> {
                        Text(text = "Hola, ${usuarioLogueado.nombre}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(text = "Teléfono: ${usuarioLogueado.telefono} • Cuenta Usuario", fontSize = 13.sp, color = Color.Gray)
                    }
                    is Organizacion -> {
                        Text(text = usuarioLogueado.nombreOrg, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(text = "RUC: ${usuarioLogueado.ruc} • Verificada: ${if(usuarioLogueado.esVerificada) "Sí" else "No"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    else -> {
                        Text(text = "Mi perfil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                ProfileOptionsCard(onNavigate = onNavigate)
                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Mis anuncios", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (totalAnuncios > 0) {
                items(listaPerdidos) { mascota ->
                    AdItemCard(
                        description = mascota.nombreM.ifEmpty { "Sin nombre" },
                        status = "PERDIDA",
                        location = mascota.lugar.ifEmpty { "No especificado" },

                        foto = mascota.foto,
                        onEdit = { onNavigate("editar_perdido/${mascota.id}") },
                        onDelete = {
                            coroutineScope.launch(Dispatchers.IO) {
                                // Aseguramos capturar el ID exacto antes de entrar a la operación de red
                                val idMascota = mascota.id

                                try {
                                    Log.d("DELETE_AZURE", "Intentando eliminar mascota con ID: $idMascota")
                                    val response = apiService.eliminarPerdido(
                                        EliminarRequest(
                                            idMascota
                                        )
                                    )

                                    if (response.isSuccessful) {
                                        // 1. Borramos localmente usando el ID verificado
                                        dbHelper.writableDatabase.delete(
                                            DatabaseHelper.TABLE_PERDIDOS,
                                            "${DatabaseHelper.PERDIDO_ID}=?",
                                            arrayOf(idMascota.toString())
                                        )

                                        // 2. Notificamos a la interfaz en el hilo principal
                                        withContext(Dispatchers.Main) {
                                            refreshTrigger++ // Esto forzará a recomponer y actualizar tu lista automáticamente
                                            Toast.makeText(context, "Anuncio eliminado correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        // Si Azure responde con un código de error (ej. 400 o 500)
                                        val errorBody = response.errorBody()?.string()
                                        Log.e("DELETE_AZURE", "Servidor rechazó la eliminación: $errorBody")

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "El servidor rechazó la eliminación", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("DELETE_AZURE", "Error de red o excepción al eliminar", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error de red al conectar con Azure", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                }

                // 2. RENDER ENCONTRADOS
                items(listaEncontrados) { mascota ->
                    AdItemCard(
                        description =
                            if (mascota.especie.isNotEmpty())
                                "Especie: ${mascota.especie}"
                            else
                                "Especie no especificada",

                        location = mascota.lugar.ifEmpty { "No especificado" },

                        foto = mascota.foto,
                        status = "ENCONTRADA",
                        onEdit = { onNavigate("editar_encontrada/${mascota.id}") },
                        onDelete = {
                            coroutineScope.launch(Dispatchers.IO) {
                                // Aseguramos capturar el ID exacto antes de la operación de red
                                val idMascota = mascota.id

                                try {
                                    android.util.Log.d("DELETE_AZURE", "Intentando eliminar encontrada con ID: $idMascota")
                                    val response = apiService.eliminarEncontrado(idMascota)

                                    if (response.isSuccessful) {
                                        // 1. Borramos localmente de la tabla addEncontrada
                                        dbHelper.writableDatabase.delete(
                                            DatabaseHelper.TABLE_ENCONTRADOS,
                                            "${DatabaseHelper.ENCONTRADO_ID}=?",
                                            arrayOf(idMascota.toString())
                                        )

                                        // 2. Notificamos en el hilo principal y refrescamos la pantalla
                                        withContext(Dispatchers.Main) {
                                            refreshTrigger++ // Forzará la actualización automática de la lista
                                            Toast.makeText(context, "Anuncio eliminado correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        android.util.Log.e("DELETE_AZURE", "Servidor rechazó la eliminación: $errorBody")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "El servidor rechazó la eliminación", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("DELETE_AZURE", "Error de red al eliminar encontrada", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error de red al conectar con Azure", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                }

                items(listaAdopciones) { mascota ->
                    val esp = mascota.especie.ifEmpty { "Mascota" }
                    val raz = mascota.raza.ifEmpty { "Mestizo" }
                    AdItemCard(
                        description = "$esp ($raz)",
                        status = "ADOPCION",
                        location = mascota.nombreOrganizacion.ifEmpty { "Particular" },
                        foto = mascota.foto,
                        onEdit = { onNavigate("editar_adopcion/${mascota.id}") },
                        onDelete = {
                            coroutineScope.launch(Dispatchers.IO) {
                                // Aseguramos capturar el ID exacto antes de la operación de red
                                val idMascota = mascota.id

                                try {
                                    android.util.Log.d("DELETE_AZURE", "Intentando eliminar adopción con ID: $idMascota")
                                    val response = apiService.eliminarAdopcion(idMascota)

                                    if (response.isSuccessful) {
                                        // 1. Borramos localmente de la tabla addAdopcion
                                        dbHelper.writableDatabase.delete(
                                            DatabaseHelper.TABLE_ADOPCION,
                                            "${DatabaseHelper.ADOPCION_ID}=?",
                                            arrayOf(idMascota.toString())
                                        )

                                        // 2. Notificamos en el hilo principal y refrescamos la pantalla
                                        withContext(Dispatchers.Main) {
                                            refreshTrigger++ // Forzará la actualización automática de la lista
                                            Toast.makeText(context, "Anuncio eliminado correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        android.util.Log.e("DELETE_AZURE", "Servidor rechazó la eliminación: $errorBody")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "El servidor rechazó la eliminación", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("DELETE_AZURE", "Error de red al eliminar adopción", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error de red al conectar con Azure", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                }
            } else {
                item {
                    NoAdsCard()
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun NoAdsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aún no tienes anuncios publicados",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdItemCard(
    description: String,
    status: String,
    location: String,
    foto: Any?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {

                when (foto) {

                    is String -> {
                        AsyncImage(
                            model = foto,
                            contentDescription = "Foto mascota",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    is ByteArray -> {

                        val bitmap = remember(foto) {
                            BitmapFactory.decodeByteArray(
                                foto,
                                0,
                                foto.size
                            )?.asImageBitmap()
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Foto mascota",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "🐾",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    else -> {
                        Text(
                            "🐾",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = status.uppercase(),
                    color = when (status.uppercase()) {
                        "PERDIDA" -> Color(0xFF7C4DFF)
                        "ENCONTRADA" -> Color(0xFF4CAF50)
                        "ADOPCION" -> Color(0xFF2196F3)
                        else -> Color.Gray
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = location,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
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
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {

    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    val usuario = remember {
        prefs.getLoggedUser()
    }

    var nombre by remember {

        mutableStateOf(
            when (usuario) {
                is Usuario -> usuario.nombre
                is Organizacion -> usuario.nombreOrg
                else -> ""
            }
        )
    }

    var telefono by remember {

        mutableStateOf(
            if (usuario is Usuario)
                usuario.telefono
            else
                ""
        )
    }

    var correo by remember {

        mutableStateOf(
            usuario?.email ?: ""
        )
    }

    var emailNotifications by remember {
        mutableStateOf(prefs.getNotifications())
    }

    var passwordActual by remember {
        mutableStateOf("")
    }

    var passwordNueva by remember {
        mutableStateOf("")
    }

    var passwordRepetir by remember {
        mutableStateOf("")
    }

    var showPasswordActual by remember {
        mutableStateOf(false)
    }

    var showPasswordNueva by remember {
        mutableStateOf(false)
    }

    var showPasswordRepetir by remember {
        mutableStateOf(false)
    }

    Scaffold(

        bottomBar = {
            BottomNavigationBar(
                currentRoute = "secundaria", // <-- Evita iluminar botones incorrectos al estar en subpantallas
                onProfileClick = { onBack() },
                onPublishClick = { onNavigate("selector") },
                onEncuentranosClick = { onNavigate("encuentranos") },
                onMapaClick = { onNavigate("mapa") }
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

            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(0.dp)
            ) {

                Text(
                    "← Atrás",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Ajustes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Text(
                    "Modo oscuro",
                    fontSize = 20.sp
                )

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeChange
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                "Información de contacto",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                label = "Nombre",
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Nombre"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (usuario is Usuario) {

                LoginInput(
                    label = "Teléfono",
                    value = telefono,
                    onValueChange = {

                        if (it.length <= 9)
                            telefono = it
                    },
                    placeholder = "999999999"
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            LoginInput(
                label = "Correo",
                value = correo,
                onValueChange = { correo = it },
                placeholder = "correo@email.com"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Switch(
                    checked = emailNotifications,
                    onCheckedChange = {

                        emailNotifications = it
                        prefs.setNotifications(it)
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Notificaciones por correo"
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                "Seguridad",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginInput(
                label = "Contraseña actual",
                value = passwordActual,
                onValueChange = { passwordActual = it },
                placeholder = "********",
                isPassword = !showPasswordActual
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginInput(
                label = "Nueva contraseña",
                value = passwordNueva,
                onValueChange = { passwordNueva = it },
                placeholder = "********",
                isPassword = !showPasswordNueva
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginInput(
                label = "Repetir contraseña",
                value = passwordRepetir,
                onValueChange = { passwordRepetir = it },
                placeholder = "********",
                isPassword = !showPasswordRepetir
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(

                onClick = {

                    val usuario =
                        prefs.getLoggedUser()
                            ?: return@Button

                    CoroutineScope(Dispatchers.IO).launch {

                        try {

                            val request = UpdateUserRequest(

                                id = usuario.id,

                                nombre = nombre,

                                telefono =
                                    if (usuario is Usuario)
                                        telefono
                                    else
                                        null,

                                email = correo,

                                ruc =
                                    if (usuario is Organizacion)
                                        usuario.ruc
                                    else
                                        null,

                                direccion =
                                    if (usuario is Organizacion)
                                        usuario.direccion
                                    else
                                        null
                            )

                            Log.d(
                                "UPDATE_USER",
                                "Enviando actualización..."
                            )

                            val response =
                                RetrofitClient
                                    .instance
                                    .updateUser(request)

                            withContext(Dispatchers.Main) {

                                if (response.isSuccessful) {

                                    Log.d(
                                        "UPDATE_USER",
                                        "Actualización exitosa"
                                    )

                                    Toast.makeText(
                                        context,
                                        "Datos actualizados",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Actualizar SharedPreferences

                                    if (usuario is Usuario) {

                                        prefs.saveLoggedUser(

                                            usuario.copy(
                                                nombre = nombre,
                                                telefono = telefono,
                                                email = correo
                                            )
                                        )

                                    } else if (usuario is Organizacion) {

                                        prefs.saveLoggedUser(

                                            usuario.copy(
                                                nombreOrg = nombre,
                                                email = correo
                                            )
                                        )
                                    }

                                } else {

                                    Log.e(
                                        "UPDATE_USER",
                                        "Error ${response.code()}"
                                    )

                                    Toast.makeText(
                                        context,
                                        "Error al actualizar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        } catch (e: Exception) {

                            withContext(Dispatchers.Main) {

                                Log.e(
                                    "UPDATE_USER",
                                    e.message ?: "Error desconocido"
                                )

                                Toast.makeText(
                                    context,
                                    e.message ?: "Error desconocido",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),

                shape = RoundedCornerShape(10.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )

            ) {

                Text(
                    text = "Aplicar",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(

                onClick = {

                    prefs.clearSession()

                    onLogout()
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )

            ) {

                Text(
                    "Cerrar sesión",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}


