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
import coil.compose.AsyncImage
import com.example.teencontre.sharedprefs.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

// --- CONFIGURACIÓN DE COLORES ---
val FigmaBlue = Color(0xFF5A57FF)
val BorderGray = Color(0xFFE0E0E0)
val TextGray = Color(0xFF757575)

// --- WIZARD 1: PERDÍ A MI MASCOTA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAnuncio(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var step by remember { mutableIntStateOf(1) }

    // Estados de Datos
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

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { },
                onPublishClick = onBackToSelector,
                onEncuentranosClick = { },
                onMapaClick = { } // <-- Agrega esta línea para quitar el error
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // HABILITA SCROLL
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            ProgressCircle(step = step)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = if (step < 6) "Mascota Perdida" else "Hecho", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> PasoMascota(petType, gender, { petType = it }, { gender = it })
                    2 -> PasoFoto(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoUbicacion(location, selectedDate, { location = it }, { selectedDate = it })
                    4 -> PasoDescripcion(description) { description = it }
                    5 -> PasoContacto(contactName, contactPhone, contactEmail, acceptedTerms, { contactName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it })
                    6 -> PantallaHecho(onBackToSelector)
                }
            }

            if (step in 1..5) {
                Spacer(modifier = Modifier.height(24.dp))
                NavigationButtons(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step == 3) showAddressSheet = true
                        else if (step < 5) step++
                        else { prefs.saveAd(contactName, contactPhone, contactEmail); step = 6 }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPhotoSheet) CustomBottomSheet("¿Absolutamente ninguna foto?", "Una foto en un anuncio aumenta significativamente las posibilidades de encontrar una mascota.", "Añadir Foto", "Omitir", { showPhotoSheet = false }, { showPhotoSheet = false; step = 3 })
    if (showAddressSheet) CustomBottomSheet("¿Es la dirección correcta?", "Una dirección incorrecta reducirá la efectividad de su solicitud de mascota.", "Sí, es cierto", "No, quiero cambiar", { showAddressSheet = false; step = 4 }, { showAddressSheet = false })
    if (showDescSheet) CustomBottomSheet("¿Sin descripción?", "Una descripción puede acelerar el proceso de encontrar a su mascota.", "Añadir una descripción", "Omitir", { showDescSheet = false }, { showDescSheet = false; step = 5 })
}

// --- WIZARD 2: ENCONTRÉ UNA MASCOTA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardEncontreMascota(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var step by remember { mutableIntStateOf(1) }

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

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { /* tu lógica */ },
                onPublishClick = { /* tu lógica */ },
                onEncuentranosClick = { /* tu lógica */ },
                onMapaClick = { } // <-- Agrega esta línea también aquí
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // HABILITA SCROLL
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            ProgressCircle(step = step)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = if (step < 6) "Mascota Encontrada" else "¡Gracias!", fontSize = 24.sp, fontWeight = FontWeight.Black)
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
                        if (step == 3) showAddressSheet = true
                        else if (step < 5) step++
                        else step = 6
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPhotoSheet) CustomBottomSheet("¿Seguro que no hay fotos?", "Las fotos ayudan al dueño a reconocer a su mascota.", "Añadir una foto", "Omitir", { showPhotoSheet = false }, { showPhotoSheet = false; step = 3 })
    if (showAddressSheet) CustomBottomSheet("¿Es correcta la dirección?", "Es importante para que el dueño sepa dónde buscar.", "Sí, es cierto", "No, quiero cambiar", { showAddressSheet = false; step = 4 }, { showAddressSheet = false })
    if (showDescSheet) CustomBottomSheet("¿Está seguro?", "Cualquier detalle (collar, color) ayuda mucho.", "Añadir una descripción", "Omitir", { showDescSheet = false }, { showDescSheet = false; step = 5 })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAdopcion(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var step by remember { mutableIntStateOf(1) }

    // --- ESTADOS DE DATOS (image_11f5d9.png) ---
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var breed by remember { mutableStateOf("") }

    var isVaccinated by remember { mutableStateOf(false) }
    var isSterilized by remember { mutableStateOf(false) }
    var isDewormed by remember { mutableStateOf(false) }

    var selectedSize by remember { mutableStateOf("Mediano") }
    var temperament by remember { mutableStateOf("Tranquilo") }

    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var description by remember { mutableStateOf("") }

    var orgName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    var showPhotoSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                text = if (step < 6) {
                    when(step) {
                        1 -> "Mascota en adopción"
                        2 -> "Estado de salud"
                        3 -> "Características"
                        4 -> "Fotos de la mascota"
                        5 -> "Contáctanos"
                        else -> ""
                    }
                } else "Hecho",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> PasoMascotaAdopcion(petName, petType, gender, breed, { petName = it }, { petType = it }, { gender = it }, { breed = it })
                    2 -> PasoSalud(isVaccinated, isSterilized, isDewormed, { isVaccinated = it }, { isSterilized = it }, { isDewormed = it })
                    3 -> PasoCaracteristicas(selectedSize, temperament, { selectedSize = it }, { temperament = it })
                    4 -> PasoFotoYDesc(selectedPhotos, description, { selectedPhotos = it }, { description = it })
                    5 -> PasoContactoOrg(orgName, contactPhone, contactEmail, acceptedTerms, { orgName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it })
                    6 -> PantallaHechoAdopcion(onBackToSelector)
                }
            }

            if (step in 1..5) {
                Spacer(modifier = Modifier.height(24.dp))
                NavigationButtons(
                    step = step,
                    accepted = if (step == 5) acceptedTerms else true,
                    onNext = {
                        if (step < 5) step++
                        else {
                            prefs.saveAd(orgName, contactPhone, contactEmail)
                            step = 6
                        }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step in 2..4) step++
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPhotoSheet) {
        CustomBottomSheet("¿Sin fotos?", "Un anuncio con fotos es más efectivo.", "Añadir", "Omitir", { showPhotoSheet = false }, { showPhotoSheet = false; step = 5 })
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
        if (step == 2 || step == 4) {
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
                    "Términos de Publicación y Seguridad",
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
fun PasoMascota(type: String, gen: String, onType: (String) -> Unit, onGen: (String) -> Unit) {
    Column {
        Text("¿Qué mascota es?", fontWeight = FontWeight.Bold)
        Text("Por favor, indique el tipo y sexo de su mascota", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        SelectorDoble("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDoble("Género", "Hembra" to "Macho", gen, onGen)
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
        Text("Por favor, indique la fecha y el lugar donde perdió a su mascota", color = TextGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedCard(Modifier.fillMaxWidth(), border = BorderStroke(1.dp, BorderGray)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(sdf.format(Date(date)), Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray)
            }
        }
        CustomInput("Especificar ubicación", value = loc, onValueChange = onLoc, placeholder = "Ejemplo: Miraflores, Lima...")
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
// --- adopciones ---
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
    Column {
        Text(
            text = "Adjunta fotos de tu mascota",
            color = MaterialTheme.colorScheme.onBackground
        )
        Button(
            onClick = { /* Lógica de galería */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir una foto")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Eliminamos singleLine para evitar el error de compilación
        LoginInput(
            label = "Descripción",
            value = desc,
            onValueChange = onDesc,
            placeholder = "Describe a la mascota..."
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
                    "Términos de Publicación y Seguridad",
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