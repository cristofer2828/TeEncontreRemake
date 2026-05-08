package com.example.teencontre.actividades

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import coil.compose.AsyncImage
import com.example.teencontre.sharedprefs.PreferenceManager
import com.example.teencontre.ui.theme.BorderGray
import com.example.teencontre.ui.theme.FigmaBlue
import com.example.teencontre.ui.theme.TextGray
import java.text.SimpleDateFormat
import java.util.*


// --- WIZARD 1: PERDÍ A MI MASCOTA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAnuncio(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var step by remember { mutableIntStateOf(1) }

    // --- ESTADOS DE CONTROL DE INTERFAZ ---
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS ---
    var nombreMascota by remember { mutableStateOf("") }
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var contactName by remember { mutableStateOf(prefs.getUserName()) }
    var contactPhone by remember { mutableStateOf(prefs.getPhone()) }
    var contactEmail by remember { mutableStateOf(prefs.getEmail()) }
    var acceptedTerms by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = {  },
                onPublishClick = onBackToSelector,
                onEncuentranosClick = { },
                onMapaClick = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Indicador de progreso superior
            ProgressCircle(step = step)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (step < 6) "Mascota perdida" else "Hecho",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Contenedor dinámico de pasos
            Box(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> PasoMascota(nombre = nombreMascota, raza = razaMascota, type = petType, gen = gender, onNombre = { nombreMascota = it }, onRaza = { razaMascota = it }, onType = { petType = it }, onGen = { gender = it })
                    2 -> PasoFoto(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoUbicacion(location, selectedDate, { location = it }, { selectedDate = it })
                    4 -> PasoDescripcion(description) { description = it }
                    5 -> PasoContacto(
                        contactName, contactPhone, contactEmail, acceptedTerms,
                        { contactName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it }
                    )
                    6 -> PantallaHecho(onBackToSelector)
                }
            }

            // Botones de navegación (solo visibles del paso 1 al 5)
            if (step in 1..5) {
                Spacer(modifier = Modifier.height(24.dp))
                NavigationButtons(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step < 5) {
                            step++
                        } else {
                            prefs.saveAd(
                                contactName ?: "",
                                contactPhone ?: "",
                                contactEmail ?: "",
                                "PERDIDA"
                            )
                            step = 6
                        }
                    },
                    onBack = {
                        if (step > 1) step-- else onBackToSelector()
                    },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
            }
        }
    }

    // --- COMPONENTES EMERGENTES (BottomSheets) ---
    if (showPhotoSheet) {
        OmitirFotoSheet(
            onDismiss = { showPhotoSheet = false },
            onConfirm = {
                showPhotoSheet = false
                step++
            }
        )
    }

    if (showDescSheet) {
        OmitirDescripcionSheet(
            onDismiss = { showDescSheet = false },
            onConfirm = {
                showDescSheet = false
                step++
            }
        )
    }
}

// --- WIZARD 2: ENCONTRÉ UNA MASCOTA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardEncontreMascota(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var step by remember { mutableIntStateOf(1) }

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }

    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var contactName by remember { mutableStateOf(prefs.getUserName()) }
    var contactPhone by remember { mutableStateOf(prefs.getPhone()) }
    var contactEmail by remember { mutableStateOf(prefs.getEmail()) }
    var acceptedTerms by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { },
                onPublishClick = onBackToSelector,
                onEncuentranosClick = { },
                onMapaClick = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            ProgressCircle(step = step)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = if (step < 6) "Mascota encontrada" else "¡Gracias!", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> PasoEncontreIdentificacion(petType, gender, { petType = it }, { gender = it })
                    2 -> PasoFoto(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoEncontreUbicacion(location, selectedDate, { location = it }, { selectedDate = it })
                    4 -> PasoEncontreDescripcion(description) { description = it }
                    5 -> PasoContacto(contactName, contactPhone, contactEmail, acceptedTerms, { contactName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it })
                    6 -> PantallaHechoEncontre(onBackToSelector)
                }
            }

            if (step in 1..5) {
                Spacer(modifier = Modifier.height(24.dp))
                NavigationButtons(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step < 5) step++
                        else {
                            // Se agrega el parámetro "ENCONTRADA"
                            prefs.saveAd(contactName, contactPhone, contactEmail, "ENCONTRADA")
                            step = 6
                        }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
            }
        }
    }

    if (showPhotoSheet) OmitirFotoSheet(onDismiss = { showPhotoSheet = false }, onConfirm = { showPhotoSheet = false; step = 3 })
    if (showDescSheet) OmitirDescripcionSheet(onDismiss = { showDescSheet = false }, onConfirm = { showDescSheet = false; step = 5 })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAdopcion(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var step by remember { mutableIntStateOf(1) }

    // Control de advertencias
    var showPhotoSheet by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS ---
    // Paso 1: Mascota
    var nombreMascota by remember { mutableStateOf("") }
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }

    // Paso 2: Salud
    var vacunado by remember { mutableStateOf(false) }
    var esterilizado by remember { mutableStateOf(false) }
    var desparasitado by remember { mutableStateOf(false) }

    // Paso 3: Características
    var tamano by remember { mutableStateOf("Mediano") }
    var temperamento by remember { mutableStateOf("Tranquilo") }

    // Paso 4: Fotos y Descripción
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var description by remember { mutableStateOf("") }

    // Paso 5: Contacto (Organización)
    var contactName by remember { mutableStateOf(prefs.getUserName() ?: "") }
    var contactPhone by remember { mutableStateOf(prefs.getPhone() ?: "") }
    var contactEmail by remember { mutableStateOf(prefs.getEmail() ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { /* Opcional: Navegar al perfil */ },
                onPublishClick = onBackToSelector,
                onEncuentranosClick = { },
                onMapaClick = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            ProgressCircle(step = step)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (step < 6) "Mascota en adopción" else "¡Publicado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> PasoMascota(
                        nombre = nombreMascota, raza = razaMascota, type = petType, gen = gender,
                        onNombre = { nombreMascota = it }, onRaza = { razaMascota = it },
                        onType = { petType = it }, onGen = { gender = it }
                    )
                    2 -> PasoSalud(
                        v = vacunado, s = esterilizado, d = desparasitado,
                        onV = { vacunado = it }, onS = { esterilizado = it }, onD = { desparasitado = it }
                    )
                    3 -> PasoCaracteristicas(
                        size = tamano, temp = temperamento,
                        onSize = { tamano = it }, onTemp = { temperamento = it }
                    )
                    4 -> PasoFotoYDesc(
                        photos = selectedPhotos, desc = description,
                        onPhotos = { selectedPhotos = it }, onDesc = { description = it }
                    )
                    5 -> PasoContactoOrg(
                        name = contactName, phone = contactPhone, email = contactEmail, terms = acceptedTerms,
                        onName = { contactName = it }, onPhone = { contactPhone = it },
                        onEmail = { contactEmail = it }, onTerms = { acceptedTerms = it }
                    )
                    6 -> PantallaHechoAdopcion(onBackToSelector)
                }
            }

            if (step in 1..5) {
                Spacer(modifier = Modifier.height(24.dp))
                NavigationButtons(
                    step = step,
                    // Solo obligamos a aceptar términos en el último paso (5)
                    accepted = if (step == 5) acceptedTerms else true,
                    onNext = {
                        if (step < 5) {
                            step++
                        } else {
                            prefs.saveAd(contactName, contactPhone, contactEmail, "ADOPCIÓN")
                            step = 6
                        }
                    },
                    onBack = {
                        if (step > 1) step-- else onBackToSelector()
                    },
                    onOmit = {
                        if (step == 4) showPhotoSheet = true
                    }
                )
            }
        }
    }

    if (showPhotoSheet) {
        OmitirFotoSheet(
            onDismiss = { showPhotoSheet = false },
            onConfirm = {
                showPhotoSheet = false
                step = 5
            }
        )
    }
}
// --- COMPONENTES DE APOYO ---

@Composable
fun NavigationButtons(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onNext,
            enabled = if (step == 5) accepted else true,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
        ) {
            Text(if (step < 5) "Siguiente" else "Publicar anuncio", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBack) { Text("Atrás", color = Color.Gray) }
        if (step == 4) {
            TextButton(onClick = onOmit) { Text("Omitir", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun PasoContacto(
    n: String,
    t: String,
    m: String,
    accepted: Boolean,
    onN: (String) -> Unit,
    onT: (String) -> Unit,
    onM: (String) -> Unit,
    onAccepted: (Boolean) -> Unit
) {
    // Estado para el diálogo de términos
    var showTermsDialog by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Información de contacto",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Deja tu información de contacto para que puedan comunicarse contigo después de que encuentren a tu mascota.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Usando LoginInput para mantener consistencia con el modo oscuro
        LoginInput("¿Cómo puedo contactarte?", n, onN, "Nombre")
        Spacer(modifier = Modifier.height(12.dp))
        LoginInput("Número de teléfono:", t, onT, "Teléfono")
        Spacer(modifier = Modifier.height(12.dp))
        LoginInput("Correo:", m, onM, "example@email.com")

        Spacer(modifier = Modifier.height(16.dp))

        // Fila de aceptación con enlace "Ver info"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Switch(
                checked = accepted,
                onCheckedChange = onAccepted,
                modifier = Modifier.scale(0.8f)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Acepto los términos y condiciones.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ver info",
                    fontSize = 12.sp,
                    color = Color(0xFF7C4DFF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showTermsDialog = true }
                )
            }
        }
    }

    // Diálogo de términos unificado para Reencuentros (Perdí / Encontré)
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    "Términos de publicación y Seguridad",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = """
                        Al publicar un anuncio en TeEncontre para buscar o reportar una mascota, aceptas:
                        
                        1. Facilitar el Reencuentro: Tus datos de contacto serán públicos para que cualquier persona con información relevante pueda contactarte.
                        
                        2. Seguridad en Entregas: Recomendamos realizar entregas en lugares públicos y concurridos. No asistas solo(a) a las citas de reencuentro.
                        
                        3. Veracidad: Te comprometes a no publicar información falsa. Si la mascota es recuperada, debes desactivar el anuncio.
                        
                        4. Prohibición de Venta: Queda estrictamente prohibida la venta de animales a través de esta plataforma. TeEncontre es una red de apoyo gratuita; cualquier intento de comercialización resultará en la eliminación del anuncio.
                        
                        5. Gratitud y Recompensas: La plataforma no gestiona ni garantiza el pago de recompensas. Cualquier acuerdo económico es responsabilidad exclusiva de los usuarios.
                        
                        6. Bienestar Animal: El uso de esta herramienta debe priorizar siempre la integridad y el respeto hacia los animales.
                        
                        7. Protección de Datos: No compartiremos tus datos con terceros para fines publicitarios.
                    """.trimIndent(),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Entendido", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
@Composable
fun ProgressCircle(step: Int) {
    val progress = (360f / 5f) * (step - 1).coerceAtLeast(0)
    val sweepAngle by animateFloatAsState(targetValue = if(step == 1) 20f else progress, label = "")
    Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(120.dp)) {
            drawCircle(Color(0xFFF0F0F0), style = Stroke(10.dp.toPx()))
            drawArc(FigmaBlue, -90f, sweepAngle, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun CustomInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
    }
}
// --- PASOS ESPECÍFICOS ---
@Composable
fun PasoMascota(
    nombre: String,
    raza: String,
    type: String,
    gen: String,
    onNombre: (String) -> Unit,
    onRaza: (String) -> Unit,
    onType: (String) -> Unit,
    onGen: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Títulos
        Text("¿Qué mascota es?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Por favor, indique el tipo y sexo de su mascota", color = Color.Gray, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        // Campo: Nombre de la mascota
        Text("Nombre de la mascota", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = nombre,
            onValueChange = onNombre,
            placeholder = { Text("Nombre", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Selectores de Tipo (Perro/Gato) y Género (Hembra/Macho)
        // Usamos el componente que ya tienes definido
        SelectorDoble("Mascota", "Perro" to "Gato", type, onType)

        Spacer(Modifier.height(12.dp))

        SelectorDoble("Género", "Hembra" to "Macho", gen, onGen)

        Spacer(Modifier.height(12.dp))

        // Campo: Raza
        Text("Raza", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = raza,
            onValueChange = onRaza,
            placeholder = { Text("Raza de la mascota", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun SelectorDoble(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(Modifier.weight(1f).height(48.dp).border(1.dp, if(isSel) FigmaBlue else BorderGray, RoundedCornerShape(10.dp)).clickable { onSelect(op) }.background(if(isSel) FigmaBlue.copy(0.1f) else Color.Transparent), contentAlignment = Alignment.Center) { Text(op, color = if(isSel) FigmaBlue else Color.Gray) }
            }
        }
    }
}

@Composable
fun PasoFoto(uris: List<Uri>, onPhotosSelected: (List<Uri>) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { onPhotosSelected(it) }
    Column {
        Text("¿Cómo se ve?", fontWeight = FontWeight.Bold)
        Text("Adjunta fotos de su mascota. Esto aumentará la posibilidad de encontrarla.", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        if (uris.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uris) { uri -> AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) }
            }
            Spacer(Modifier.height(16.dp))
        }
        val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        Box(Modifier.fillMaxWidth().height(52.dp).drawBehind { drawRoundRect(Color.LightGray, style = stroke) }.clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
            Text("Añadir una foto", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoUbicacion(loc: String, date: Long, onLoc: (String) -> Unit, onDate: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = date)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    if (showPicker) {
        DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onDate(it) }; showPicker = false }) { Text("OK") } }) { DatePicker(state = state) }
    }
    Column {
        Text("¿Dónde lo perdiste?", fontWeight = FontWeight.Bold)
        Text("Por favor, indique la fecha y el lugar donde Perdio a la mascota.\u2028\u2028", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedCard(Modifier.fillMaxWidth().clickable { showPicker = true }, border = BorderStroke(1.dp, BorderGray)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(sdf.format(Date(date)), Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray)
            }
        }
        CustomInput("Ubicación", loc, onLoc, "Ejemplo: Cerca al Real Plaza...")
    }
}

@Composable
fun PasoDescripcion(desc: String, onDesc: (String) -> Unit) {
    Column {
        Text("¿Que más puedes contarnos?", fontWeight = FontWeight.Bold)
        Text("¿Tenía collar? ¿Es dócil? ¿Alguna marca especial?", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = desc, onValueChange = onDesc, placeholder = { Text("Escribe aquí...") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
    }
}

// --- PANTALLAS FINALES ---

@Composable
fun PantallaHecho(onFinished: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text("Tu anuncio ha sido publicado. Puedes previsualizarlo para comprobar que la información que has introducido es \n" +
                "correcta.\n" +
                "Además, no olvides consultar los anuncios de otros usuarios; es muy posible que alguien ya haya encontrado a tu mascota.\n" +
                "No se preocupe: volvera a casa muy pronto.", textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFinished, Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue)) { Text("Volver al Inicio") }
    }
}

@Composable
fun PantallaHechoEncontre(onFinished: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text("Tu anuncio ha sido publicado. Puedes revisarlo para comprobar que la información que ingresaste es correcta.\u2028\u2028Además, no olvides revisar los anuncios de otros usuarios; es posible que el dueño ya haya publicado un anuncio buscando una mascota.\u2028\u2028Estamos seguros de que pronto encontraremos al dueño.", textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFinished, Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue)) { Text("Finalizar") }
    }
}

// --- PASOS ENCONTRÉ ---

@Composable
fun PasoEncontreIdentificacion(type: String, gen: String, onType: (String) -> Unit, onGen: (String) -> Unit) {
    Column {
        Text("¿Qué encontraste?", fontWeight = FontWeight.Bold)
        Text("Por favor, indique la especie y el sexo de la mascota encontrada.", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        SelectorDoble("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDoble("Género", "Hembra" to "Macho", gen, onGen)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoEncontreUbicacion(loc: String, date: Long, onLoc: (String) -> Unit, onDate: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = date)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    if (showPicker) {
        DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onDate(it) }; showPicker = false }) { Text("OK") } }) { DatePicker(state = state) }
    }
    Column {
        Text("¿Dónde la encontraste?", fontWeight = FontWeight.Bold)
        Text("Por favor, indique la fecha y el lugar donde encontró a la mascota.\u2028\u2028", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedCard(Modifier.fillMaxWidth().clickable { showPicker = true }, border = BorderStroke(1.dp, BorderGray)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(sdf.format(Date(date)), Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray)
            }
        }
        CustomInput("Ubicación", loc, onLoc, "Ejemplo: Cerca al Real Plaza...")
    }
}

@Composable
fun PasoEncontreDescripcion(desc: String, onDesc: (String) -> Unit) {
    Column {
        Text("Detalles adicionales", fontWeight = FontWeight.Bold)
        Text("¿Tenía collar? ¿Es dócil? ¿Alguna marca especial?", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = desc, onValueChange = onDesc, placeholder = { Text("Descripción...") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(title: String, desc: String, confirm: String, dismiss: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(desc, textAlign = TextAlign.Center, color = TextGray)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onConfirm, Modifier.fillMaxWidth().height(50.dp)) { Text(confirm) }
            TextButton(onClick = onDismiss, Modifier.fillMaxWidth()) { Text(dismiss, color = Color.Red) }
        }
    }
}
// --- Adopciones ---
@Composable
fun PasoMascotaAdopcion(
    name: String,
    type: String,
    gender: String,
    breed: String,
    onName: (String) -> Unit,
    onType: (String) -> Unit,
    onGender: (String) -> Unit,
    onBreed: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Campo: Nombre de la mascota
        Text("Nombre de la mascota", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        LoginInput("", name, onName, "Nombre")

        Spacer(modifier = Modifier.height(16.dp))

        // Selector: Mascota (Perro / Gato)
        Text("Mascota", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomToggleButton(
                text = "Perro",
                isSelected = type == "Perro",
                onClick = { onType("Perro") },
                modifier = Modifier.weight(1f)
            )
            CustomToggleButton(
                text = "Gato",
                isSelected = type == "Gato",
                onClick = { onType("Gato") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector: (Hembra / Macho)
        Text("Genero", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomToggleButton(
                text = "Hembra",
                isSelected = gender == "Hembra",
                onClick = { onGender("Hembra") },
                modifier = Modifier.weight(1f)
            )
            CustomToggleButton(
                text = "Macho",
                isSelected = gender == "Macho",
                onClick = { onGender("Macho") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Raza
        Text("Raza", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        LoginInput("", breed, onBreed, "Raza de la mascota")
    }
}

// Botón personalizado para los selectores de tipo y género
@Composable
fun CustomToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF7C4DFF) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text)
    }
}

@Composable
fun PasoSalud(
    v: Boolean, s: Boolean, d: Boolean,
    onV: (Boolean) -> Unit, onS: (Boolean) -> Unit, onD: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("¿Esta vacunado?", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = v, onClick = { onV(true) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
            Text("Si", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = !v, onClick = { onV(false) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
            Text("No", color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("¿Esta esterilizado?", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = s, onClick = { onS(true) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
            Text("Si", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = !s, onClick = { onS(false) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
            Text("No", color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("¿Esta desparasitado?", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = d, onClick = { onD(true) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
            Text("Si", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = !d, onClick = { onD(false) }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
            Text("No", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun PasoCaracteristicas(
    size: String,
    temp: String,
    onSize: (String) -> Unit,
    onTemp: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tamaño", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        listOf("Pequeño", "Mediano", "Grande").forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = size == option,
                    onCheckedChange = { if(it) onSize(option) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C4DFF))
                )
                Text(option, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Temperamento", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        listOf("Tranquilo", "Protector", "Sociable", "Juguetón").forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = temp == option,
                    onCheckedChange = { if(it) onTemp(option) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C4DFF))
                )
                Text(option, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun PasoFotoYDesc(
    photos: List<Uri>,
    desc: String,
    onPhotos: (List<Uri>) -> Unit,
    onDesc: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> onPhotos(uris) }

    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Adjunta fotos de tu mascota",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (photos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(photos) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color.LightGray,
                        style = stroke,
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
                .clickable {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Añadir una foto",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Descripción",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = desc,
            onValueChange = onDesc,
            placeholder = { Text("Describe a la mascota...", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = false,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7C4DFF),
                unfocusedBorderColor = Color.LightGray
            )
        )
    }
}

@Composable
fun PasoContactoOrg(
    name: String,
    phone: String,
    email: String,
    terms: Boolean,
    onName: (String) -> Unit,
    onPhone: (String) -> Unit,
    onEmail: (String) -> Unit,
    onTerms: (Boolean) -> Unit
) {
    // Estado para controlar la visibilidad del diálogo de términos
    var showTermsDialog by remember { mutableStateOf(false) }

    Column {
        LoginInput("¿Nombre de la organización?", name, onName, "Nombre de la organización")
        Spacer(modifier = Modifier.height(12.dp))
        LoginInput("Número de teléfono", phone, onPhone, "Número de teléfono")
        Spacer(modifier = Modifier.height(12.dp))
        LoginInput("Correo", email, onEmail, "example@email.com")

        Spacer(modifier = Modifier.height(16.dp))

        // Fila de términos con el botón "Ver info"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = terms,
                onCheckedChange = onTerms,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C4DFF))
            )

            Column {
                Text(
                    text = "Acepto los términos de confidencialidad",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ver info",
                    fontSize = 12.sp,
                    color = Color(0xFF7C4DFF), // Morado para resaltar que es clickeable
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showTermsDialog = true }
                )
            }
        }
    }

    // Diálogo de términos unificado para Reencuentros (Perdí / Encontré)
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    "Términos de publicación y Seguridad",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = """
                        Al publicar un anuncio en TeEncontre para buscar o reportar una mascota, aceptas:
                        
                        1. Facilitar el Reencuentro: Tus datos de contacto serán públicos para que cualquier persona con información relevante pueda contactarte.
                        
                        2. Seguridad en Entregas: Recomendamos realizar entregas en lugares públicos y concurridos. No asistas solo(a) a las citas de reencuentro.
                        
                        3. Veracidad: Te comprometes a no publicar información falsa. Si la mascota es recuperada, debes desactivar el anuncio.
                        
                        4. Prohibición de Venta: Queda estrictamente prohibida la venta de animales a través de esta plataforma. TeEncontre es una red de apoyo gratuita; cualquier intento de comercialización resultará en la eliminación del anuncio.
                        
                        5. Gratitud y Recompensas: La plataforma no gestiona ni garantiza el pago de recompensas. Cualquier acuerdo económico es responsabilidad exclusiva de los usuarios.
                        
                        6. Bienestar Animal: El uso de esta herramienta debe priorizar siempre la integridad y el respeto hacia los animales.
                        
                        7. Protección de Datos: No compartiremos tus datos con terceros para fines publicitarios.
                    """.trimIndent(),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Entendido", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun PantallaHechoAdopcion(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu anuncio ha sido publicado. Puedes previsualizarlo para comprobar que la información que has introducido es correcta.\n\nNos da gusto que encuentres un lugar para los peluditos.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Abrir anuncio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿Seguro que no quieres subir una foto?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Las publicaciones con foto reciben 3 veces más atención.", modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Añadir Foto") }
            TextButton(onClick = onConfirm) { Text("Omitir de todas formas", color = Color.Gray) }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescripcionSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Descripción incompleta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Dar detalles ayuda a identificar a la mascota más rápido.", modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Escribir detalles") }
            TextButton(onClick = onConfirm) { Text("Omitir", color = Color.Gray) }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}