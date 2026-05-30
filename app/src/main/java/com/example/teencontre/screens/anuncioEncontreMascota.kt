package com.example.teencontre.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// IMPORTACIONES DE TU PAQUETE DE DATOS
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.model.MascotasEncontradasModel

// COLOR VERDE SÉPTIMO PASO (CONSERVA TU IDENTIDAD VISUAL)
private val FigmaGreen = Color(0xFF4CAF50)

// =================================================================
// COMPONENTE PRINCIPAL: WIZARD DE CREACIÓN (MASCOTA ENCONTRADA)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardEncontreAnuncio(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }

    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE CONTROL DE INTERFAZ ---
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }
    var showLocationConfirmSheet by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS DE LA MASCOTA ---
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var contactName by remember { mutableStateOf(sharedPreferences.getString("userName", "") ?: "") }
    var contactPhone by remember { mutableStateOf(sharedPreferences.getString("userPhone", "") ?: "") }
    var contactEmail by remember { mutableStateOf(sharedPreferences.getString("userEmail", "") ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- BARRA SUPERIOR (CÍRCULO GRANDE Y CENTRADO + FLECHA FLOTANTE ADAPTABLE) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Flecha Atrás: Posicionada al inicio del Box de forma flotante
                IconButton(
                    onClick = { if (step > 1 && step <= totalSteps) step-- else onBackToSelector() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Círculo indicador de progreso
                if (step <= totalSteps) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = 1f,
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeWidth = 6.dp
                        )

                        val unCuartoDeVuelta = -90f
                        val proporcionProgreso = step.toFloat() / totalSteps.toFloat()

                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawArc(
                                color = FigmaGreen,
                                startAngle = unCuartoDeVuelta,
                                sweepAngle = 360f * proporcionProgreso,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 6.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round // Extremos redondeados estilizados
                                )
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = if (step <= totalSteps) "Mascota encontrada" else "Hecho",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = if (step <= totalSteps) FigmaGreen else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTENEDOR DINÁMICO DE PASOS ---
            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                when (step) {
                    1 -> PasoMascotaEncontrada(
                        raza = razaMascota, type = petType, gen = gender,
                        onRaza = { razaMascota = it }, onType = { petType = it }, onGen = { gender = it }
                    )
                    2 -> PasoFotoEncontrada(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoUbicacionEncontrada(location, selectedDate, { location = it }, { selectedDate = it })
                    4 -> PasoDescripcionEncontrada(description) { description = it }
                    5 -> PasoContactoEncontrada(
                        contactName, contactPhone, contactEmail, acceptedTerms,
                        { contactName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it }
                    )
                    6 -> PantallaHechoEncontrada(onBackToSelector)
                }
            }

            // --- BOTONES DE NAVEGACIÓN ---
            if (step in 1..totalSteps) {
                Spacer(modifier = Modifier.height(40.dp))
                NavigationButtonsEncontrada(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step == 3) {
                            showLocationConfirmSheet = true
                        } else if (step < totalSteps) {
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

                            val detallesIntroducidos = StringBuilder()
                            if (razaMascota.isNotBlank()) detallesIntroducidos.append("Raza/Rasgos: $razaMascota. ")
                            detallesIntroducidos.append(description)

                            val mascotaHallada = MascotasEncontradasModel(
                                id = 0,
                                especie = petType,
                                genero = gender,
                                foto = fotoBytes,
                                fecha = sdf.format(Date(selectedDate)),
                                lugar = location,
                                descripcion = detallesIntroducidos.toString().trim(),
                                contacto = contactName,
                                telefono = contactPhone,
                                correo = contactEmail
                            )

                            dbHelper.insertEncontrada(mascotaHallada)
                            step = 6
                        }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPhotoSheet) {
        OmitirFotoDialog(onDismiss = { showPhotoSheet = false }, onConfirm = { showPhotoSheet = false; step++ })
    }
    if (showDescSheet) {
        OmitirDescDialog(onDismiss = { showDescSheet = false }, onConfirm = { showDescSheet = false; step++ })
    }
    if (showLocationConfirmSheet) {
        ConfirmarUbicacionDialog(
            onDismiss = { showLocationConfirmSheet = false },
            onConfirm = {
                showLocationConfirmSheet = false
                step++
            }
        )
    }
}

// =================================================================
// SUBPANTALLAS DEL FLUJO "ENCONTRADA" (CON COLORES DEL TEMA)
// =================================================================
@Composable
fun PasoMascotaEncontrada(
    raza: String, type: String, gen: String,
    onRaza: (String) -> Unit, onType: (String) -> Unit, onGen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Qué mascota encontraste?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Por favor, indique el tipo y sexo aproximado de la mascota", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        SelectorDobleEncontrada("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDobleEncontrada("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(16.dp))

        Text("Raza o rasgos parecidos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = raza, onValueChange = onRaza,
            placeholder = { Text("Ej: Cruzado, Pitbull, Siamés...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun SelectorDobleEncontrada(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(
                    Modifier
                        .weight(1f).height(48.dp)
                        .border(1.dp, if (isSel) FigmaGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onSelect(op) }
                        .background(if (isSel) FigmaGreen.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(op, color = if (isSel) FigmaGreen else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PasoFotoEncontrada(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onPhotosChanged(photos + uris)
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Fotografía de la mascota",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sube imágenes claras para que el dueño original pueda reconocerla rápidamente.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Foto",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))

        // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL (Caja de "Añadir una foto") ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Añadir una foto",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }

        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(photos) { uri ->
                    // --- AQUÍ ESTÁ EL SEGUNDO CAMBIO (Miniaturas de las fotos cargadas) ---
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Preview Mascota",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onPhotosChanged(photos.filter { it != uri }) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color(0xCC000000), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoUbicacionEncontrada(loc: String, date: Long, onLoc: (String) -> Unit, onDate: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = date)
    val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDate(it) }
                    showPicker = false
                }) { Text("OK", color = FigmaGreen) }
            }
        ) { DatePicker(state = state) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Dónde lo encontraste?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Indica la fecha exacta y zona donde se le vio o rescató.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().padding(vertical = 4.dp).height(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = sdf.format(Date(date)), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Lugar de avistamiento", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = loc, onValueChange = onLoc,
            placeholder = { Text("Ej: Parque del Periodista, cuadra 4...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun PasoDescripcionEncontrada(desc: String, onDescChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Detalles adicionales", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("¿Tiene collar? ¿Está herido? ¿Es manso o temeroso?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = desc, onValueChange = onDescChanged,
            placeholder = { Text("Describe el estado de la mascota...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoContactoEncontrada(
    name: String, phone: String, email: String, accepted: Boolean,
    onName: (String) -> Unit, onPhone: (String) -> Unit, onEmail: (String) -> Unit, onAccepted: (Boolean) -> Unit
) {
    // Estados internos para calzar con la lógica de tu otro componente
    var terminosAceptados by remember { mutableStateOf(false) }
    var datosPublicosAceptados by remember { mutableStateOf(accepted) }
    var mostrarModalTerminos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Información de contacto",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "¿Cómo te contactarán si encuentran a tu mascota?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        Text(
            "Nombre de contacto",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Teléfono / Celular",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhone,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Correo electrónico",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmail,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(Modifier.height(20.dp))

        // --- CHECKBOX 1: DATOS PÚBLICOS ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = datosPublicosAceptados,
                onCheckedChange = { nuevoValor ->
                    datosPublicosAceptados = nuevoValor
                    onAccepted(nuevoValor && terminosAceptados)
                },
                colors = CheckboxDefaults.colors(checkedColor = FigmaGreen)
            )
            Text(
                text = "Acepto los términos de ayuda y protección animal.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // --- CHECKBOX 2: TÉRMINOS DE USUARIO ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = terminosAceptados,
                onCheckedChange = { nuevoValor ->
                    terminosAceptados = nuevoValor
                    onAccepted(datosPublicosAceptados && nuevoValor)
                },
                colors = CheckboxDefaults.colors(checkedColor = FigmaGreen)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Acepto los ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Términos de Usuario",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaGreen,
                    modifier = Modifier.clickable { mostrarModalTerminos = true }
                )
                Text(
                    text = ".",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // --- VENTANA EMERGENTE CENTRADA (DIALOG CLÁSICO) ---
    if (mostrarModalTerminos) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { mostrarModalTerminos = false }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Términos de Usuario",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { mostrarModalTerminos = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "1. USO RESPONSABLE: Esta plataforma es exclusivamente para facilitar la adopción y el reencuentro de mascotas.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "2. DATOS PERSONALES: Al registrarte, aceptas que tus datos de contacto sean visibles para otros usuarios cuando reportes o busques una mascota.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "3. PROHIBICIONES: Está estrictamente prohibido lucrar o vender animales a través de esta aplicación.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "4. COMUNIDAD: Nos reservamos el derecho de eliminar cuentas que realicen reportes falsos.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaHechoEncontrada(onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = FigmaGreen,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "¡Anuncio de avistamiento publicado!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Gracias por reportarlo. El dueño o la comunidad que busca a este pequeño podrá ver tu información para coordinar el reencuentro de inmediato.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al Inicio", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// COMPONENTE: BOTONES DE NAVEGACIÓN (CORREGIDOS ESTILO FIGMA)
// =================================================================
@Composable
fun NavigationButtonsEncontrada(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val botonHabilitado = step != 5 || accepted
        Button(
            onClick = onNext,
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (botonHabilitado) FigmaGreen else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (botonHabilitado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (step == 5) "Publicar" else "Próximo",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        if (step == 2 || step == 4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onOmit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Omitir",
                    color = FigmaGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// =================================================================
// DIÁLOGOS EMERGENTES TIPO BOTTOM SHEET (DESGLOZAN DESDE ABAJO)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
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
                text = "Subir una foto aumenta radicalmente la velocidad de reconocimiento por parte de su dueño original.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Añadir foto", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Omitir de todas formas", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir detalles?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mencionar si lleva collar o algún color en específico ayuda un montón a identificarlo.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Escribir detalles", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Omitir de todas formas", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmarUbicacionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Es correcta la dirección?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Es importante para que el dueño sepa dónde buscar.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sí, cierto", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = "No, quiero cambiar",
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}