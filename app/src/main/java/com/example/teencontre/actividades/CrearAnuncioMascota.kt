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
                onProfileClick = {},
                onPublishClick = onBackToSelector,
                onEncuentranosClick = { })
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
                onProfileClick = {},
                onPublishClick = onBackToSelector,
                onEncuentranosClick = { })
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
    n: String, t: String, m: String, accepted: Boolean,
    onN: (String) -> Unit, onT: (String) -> Unit, onM: (String) -> Unit, onAccepted: (Boolean) -> Unit
) {
    Column {
        Text("Información de contacto", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Deja tu información de contacto para que puedan comunicarse contigo después de que encuentren a tu mascota.\n", color = TextGray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))
        CustomInput("¿Cómo puedo contactarte?", n, onN, "Nombre")
        CustomInput("Número de teléfono:", t, onT, "Teléfono")
        CustomInput("Correo:", m, onM, "example@email.com")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onAccepted(!accepted) }.padding(vertical = 8.dp)
        ) {
            Switch(checked = accepted, onCheckedChange = onAccepted, modifier = Modifier.scale(0.8f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Acepto los términos y condiciones.", fontSize = 14.sp)
        }
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
        OutlinedCard(Modifier.fillMaxWidth().clickable { showPicker = true }, border = BorderStroke(1.dp, BorderGray)) {
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