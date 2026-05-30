package com.example.teencontre.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.model.MascotasAdopcionModel

private val FigmaBlue = Color(0xFF2196F3)

// =================================================================
// COMPONENTE PRINCIPAL: WIZARD DE CREACIÓN (ADOPCIÓN)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAdopcion(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }

    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE CONTROL DE INTERFAZ ---
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showDescDialog by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS DE LA MASCOTA ---
    var nombreMascota by remember { mutableStateOf("") }
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var edadMascota by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // --- ATRIBUTOS EXCLUSIVOS DE ADOPCIÓN ---
    var vacunado by remember { mutableStateOf(false) }
    var esterilizado by remember { mutableStateOf(false) }
    var desparasitado by remember { mutableStateOf(false) }
    var tamano by remember { mutableStateOf("Mediano") }
    var temperamento by remember { mutableStateOf("Juguetón") }

    // --- ESTADOS DE CONTACTO ---
    var contactName by remember { mutableStateOf(sharedPreferences.getString("userName", "") ?: "") }
    var contactPhone by remember { mutableStateOf(sharedPreferences.getString("userPhone", "") ?: "") }
    var contactEmail by remember { mutableStateOf(sharedPreferences.getString("userEmail", "") ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (step in 1..totalSteps) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp, top = 8.dp)
                    ) {
                        NavigationButtonsAdopcion(
                            step = step,
                            accepted = acceptedTerms,
                            onNext = {
                                if (step < totalSteps) {
                                    step++
                                } else {
                                    var fotoBytes: ByteArray? = null
                                    if (selectedPhotos.isNotEmpty()) {
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(selectedPhotos[0])
                                            fotoBytes = inputStream?.readBytes()
                                            inputStream?.close()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                    val descripcionCompleta = buildString {
                                        if (edadMascota.isNotBlank()) append("Edad aproximada: $edadMascota. ")
                                        if (description.isNotBlank()) append(description)
                                    }

                                    val mascotaAdopcion = MascotasAdopcionModel(
                                        id = 0,
                                        especie = petType,
                                        genero = gender,
                                        raza = if (razaMascota.isNotBlank()) razaMascota else "Mestizo",
                                        vacunado = vacunado,
                                        esterilizado = esterilizado,
                                        desparasitado = desparasitado,
                                        tamano = tamano,
                                        temperamento = temperamento,
                                        foto = fotoBytes,
                                        descripcion = descripcionCompleta,
                                        nombreOrganizacion = if (contactName.isNotBlank()) contactName else "Particular",
                                        telefono = contactPhone,
                                        correo = contactEmail
                                    )

                                    dbHelper.insertAdopcion(mascotaAdopcion)
                                    step = 6
                                }
                            },
                            onBack = { if (step > 1) step-- else onBackToSelector() },
                            onOmit = {
                                if (step == 2) showPhotoDialog = true
                                if (step == 4) showDescDialog = true
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- BARRA SUPERIOR CON PROGRESO EN AZUL ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { if (step > 1 && step <= totalSteps) step-- else onBackToSelector() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (step <= totalSteps) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeWidth = 5.dp
                        )

                        val unCuartoDeVuelta = -90f
                        val proporcionProgreso = step.toFloat() / totalSteps.toFloat()

                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawArc(
                                color = FigmaBlue,
                                startAngle = unCuartoDeVuelta,
                                sweepAngle = 360f * proporcionProgreso,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 5.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (step <= totalSteps) "Dar en adopción" else "Hecho",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = if (step <= totalSteps) FigmaBlue else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTENIDO DEL FORMULARIO CON SCROLL SEGURO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (step) {
                    1 -> PasoMascotaAdopcion(
                        nombre = nombreMascota, raza = razaMascota, type = petType, gen = gender, edad = edadMascota,
                        onNombre = { nombreMascota = it }, onRaza = { razaMascota = it }, onType = { petType = it }, onGen = { gender = it }, onEdad = { edadMascota = it }
                    )
                    2 -> PasoFotoAdopcion(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoSaludYFisicoAdopcion(
                        vacunado, esterilizado, desparasitado, tamano, temperamento,
                        { vacunado = it }, { esterilizado = it }, { desparasitado = it }, { tamano = it }, { temperamento = it }
                    )
                    4 -> PasoDescripcionAdopcion(description) { description = it }
                    5 -> PasoContactoAdopcion(
                        name = contactName, phone = contactPhone, email = contactEmail, accepted = acceptedTerms,
                        onName = { contactName = it }, onPhone = { contactPhone = it }, onEmail = { contactEmail = it }, onAccepted = { acceptedTerms = it }
                    )
                    6 -> PantallaHechoAdopcion(onBackToSelector)
                }
            }
        }
    }

    // --- DIÁLOGOS EMERGENTES DE OMISIÓN ---
    if (showPhotoDialog) {
        OmitirFotoAdopcionDialog(onDismiss = { showPhotoDialog = false }, onConfirm = { showPhotoDialog = false; step++ })
    }
    if (showDescDialog) {
        OmitirDescAdopcionDialog(onDismiss = { showDescDialog = false }, onConfirm = { showDescDialog = false; step++ })
    }
}

// =================================================================
// SUBPANTALLAS DEL FLUJO "ADOPCIÓN"
// =================================================================

@Composable
fun PasoMascotaAdopcion(
    nombre: String, raza: String, type: String, gen: String, edad: String,
    onNombre: (String) -> Unit, onRaza: (String) -> Unit,
    onType: (String) -> Unit, onGen: (String) -> Unit, onEdad: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Cuéntanos sobre la mascota", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Ayuda a los futuros adoptantes a conocer a su nuevo compañero.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Text("Nombre de la mascota (Opcional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = nombre, onValueChange = onNombre, placeholder = { Text("Ej: Firulais, Pelusa o 'Sin nombre'", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))

        Spacer(Modifier.height(16.dp))
        SelectorDobleAdopcion("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDobleAdopcion("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(16.dp))

        Text("Edad aproximada", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = edad, onValueChange = onEdad, placeholder = { Text("Ej: 3 meses, 2 años...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))

        Spacer(Modifier.height(16.dp))

        Text("Raza", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = raza, onValueChange = onRaza, placeholder = { Text("Ej: Mestizo, Golden Retriever...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SelectorDobleAdopcion(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(Modifier.weight(1f).height(48.dp).border(1.dp, if (isSel) FigmaBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).clickable { onSelect(op) }.background(if (isSel) FigmaBlue.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(op, color = if (isSel) FigmaBlue else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PasoFotoAdopcion(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris -> if (uris.isNotEmpty()) onPhotosChanged(photos + uris) }
    Column(
        modifier = Modifier.fillMaxWidth()) {
        Text("Fotos de la mascota",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface)
        Text("¡Las fotos claras e iluminadas aumentan las posibilidades de adopción!",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        Text("Foto",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(6.dp))

        // --- CORRECCIÓN MODO NOCHE (Botón Añadir Foto) ---
        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(MaterialTheme.colorScheme.surfaceContainer,
            RoundedCornerShape(8.dp)).border(1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center) { Text("Añadir una foto",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 15.sp) }

        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()) {
                items(photos) { uri ->
                    // --- CORRECCIÓN MODO NOCHE (Miniaturas de Fotos) ---
                    Box(modifier = Modifier.size(80.dp).border(1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Image(painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop)
                        IconButton(onClick = { onPhotosChanged(photos.filter { it != uri }) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color(0xCC000000),
                                androidx.compose.foundation.shape.CircleShape)) { Icon(Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoSaludYFisicoAdopcion(
    vacunado: Boolean, esterilizado: Boolean, desparasitado: Boolean, tamano: String, temperamento: String,
    onVacunadoChanged: (Boolean) -> Unit, onEsterilizadoChanged: (Boolean) -> Unit, onDesparasitadoChanged: (Boolean) -> Unit,
    onTamanoChanged: (String) -> Unit, onTemperamentoChanged: (String) -> Unit
) {
    var expandTamano by remember { mutableStateOf(false) }
    val listaTamanos = listOf("Pequeño", "Mediano", "Grande")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Estado de salud y físico", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Especifica los cuidados médicos actuales de la mascota.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Está Vacunado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = vacunado, onCheckedChange = onVacunadoChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FigmaBlue, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Está Esterilizado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = esterilizado, onCheckedChange = onEsterilizadoChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FigmaBlue, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Está Desparasitado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = desparasitado, onCheckedChange = onDesparasitadoChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FigmaBlue, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
        }

        Spacer(Modifier.height(16.dp))
        Text("Tamaño", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        ExposedDropdownMenuBox(expanded = expandTamano, onExpandedChange = { expandTamano = !expandTamano }) {
            OutlinedTextField(value = tamano, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandTamano) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
            ExposedDropdownMenu(expanded = expandTamano, onDismissRequest = { expandTamano = false }) {
                listaTamanos.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { onTamanoChanged(item); expandTamano = false }) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Temperamento / Carácter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = temperamento, onValueChange = onTemperamentoChanged, placeholder = { Text("Ej: Juguetón, tranquilo, miedoso...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun PasoDescripcionAdopcion(desc: String, onDescChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Personalidad e historia", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("¿Cómo se comporta con niños u otros animales? Cuéntanos su historia.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = desc, onValueChange = onDescChanged, placeholder = { Text("Ej: Es súper cariñoso, ideal para casas familiares, convive bien con gatos...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(8.dp), maxLines = 5, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoContactoAdopcion(
    name: String, phone: String, email: String, accepted: Boolean,
    onName: (String) -> Unit, onPhone: (String) -> Unit, onEmail: (String) -> Unit, onAccepted: (Boolean) -> Unit
) {
    var terminosAceptados by remember { mutableStateOf(false) }
    var datosPublicosAceptados by remember { mutableStateOf(accepted) }
    var mostrarVentanaTerminos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tus datos de contacto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Los interesados te contactarán directamente a través de estos medios.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Text("Tu Nombre o Albergue", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = name, onValueChange = onName, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))

        Spacer(Modifier.height(12.dp))
        Text("Tu Teléfono", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = phone, onValueChange = onPhone, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))

        Spacer(Modifier.height(12.dp))
        Text("Tu Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(value = email, onValueChange = onEmail, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))

        Spacer(Modifier.height(20.dp))

        // --- CHECKBOX 1: ADOPCIÓN RESPONSABLE ---
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Checkbox(
                checked = datosPublicosAceptados,
                onCheckedChange = { nuevoValor ->
                    datosPublicosAceptados = nuevoValor
                    onAccepted(nuevoValor && terminosAceptados)
                },
                colors = CheckboxDefaults.colors(checkedColor = FigmaBlue, uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.offset(y = (-4).dp)
            )
            Text("Confirmo que doy a la mascota de forma responsable.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp), lineHeight = 16.sp)
        }

        Spacer(Modifier.height(4.dp))

        // --- CHECKBOX 2: TÉRMINOS DE USUARIO ---
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Checkbox(
                checked = terminosAceptados,
                onCheckedChange = { nuevoValor ->
                    terminosAceptados = nuevoValor
                    onAccepted(datosPublicosAceptados && nuevoValor)
                },
                colors = CheckboxDefaults.colors(checkedColor = FigmaBlue, uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.offset(y = (-4).dp)
            )
            Row(modifier = Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Acepto los ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Términos de organizacion",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaBlue,
                    modifier = Modifier.clickable { mostrarVentanaTerminos = true }
                )
                Text(".", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- DIÁLOGO EMERGENTE CENTRADO DE TÉRMINOS (Corrección Modo Noche) ---
    if (mostrarVentanaTerminos) {
        Dialog(onDismissRequest = { mostrarVentanaTerminos = false }) {
            Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Términos para organizaciones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { mostrarVentanaTerminos = false }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("1. VERIFICACIÓN DE IDENTIDAD: Como organización, es obligatorio proporcionar un RUC válido.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        Text("2. TIEMPO DE RESPUESTA: La confirmación de tu centro de adopción tardará un máximo de 1 semana mientras validamos tus datos.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        Text("3. COMPROMISO: Te comprometes a garantizar el bienestar de los animales publicados y a mantener la información actualizada.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        Text("4. PRIVACIDAD: Tu dirección y RUC serán almacenados de forma segura y usados solo para fines de transparencia institucional.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaHechoAdopcion(onFinished: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FigmaBlue, modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(24.dp))
        Text("¡Anuncio publicado!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        Text("Gracias por dar una segunda oportunidad. La comunidad interesada ya puede ver tu publicación.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(40.dp))
        Button(onClick = onFinished, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue), shape = RoundedCornerShape(12.dp)) { Text("Volver al Inicio", color = Color.White) }
    }
}

@Composable
fun NavigationButtonsAdopcion(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        val botonHabilitado = step != 5 || accepted

        Button(
            onClick = onNext,
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(containerColor = if(botonHabilitado) FigmaBlue else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if(botonHabilitado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (step == 5) "Publicar" else "Próximo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (step == 2 || step == 4) {
            Box(
                modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onOmit() },
                contentAlignment = Alignment.Center) {
                Text("Omitir", color = FigmaBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// =================================================================
// DIÁLOGOS EMERGENTES DE OMISIÓN CENTRADOS EN AZUL (Corrección Modo Noche)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoAdopcionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir fotos?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Las publicaciones con fotos reciben solicitudes de adopción casi de inmediato.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Añadir foto", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text("Omitir de todas formas", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescAdopcionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir descripción?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Colocar detalles sobre su comportamiento ayuda a las familias a decidirse más rápido.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Escribir detalles", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text("Omitir de todas formas", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            }
        }
    }
}