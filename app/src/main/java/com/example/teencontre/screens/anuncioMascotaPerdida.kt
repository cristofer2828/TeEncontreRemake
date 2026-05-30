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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.ui.text.TextStyle

// IMPORTACIONES DE TU CONFIGURACIÓN DE BASE DE DATOS
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.model.MascotasPerdidasModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardMascotaPerdida(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE LOS DATOS RECOGIDOS ---
    var nombreMascota by remember { mutableStateOf("") }
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var location by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var contactName by remember { mutableStateOf(sharedPreferences.getString("userName", "") ?: "") }
    var contactPhone by remember { mutableStateOf(sharedPreferences.getString("userPhone", "") ?: "") }
    var contactEmail by remember { mutableStateOf(sharedPreferences.getString("userEmail", "") ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    // Estados para controlar los Bottom Sheets de omisión
    var showOmitirFotoSheet by remember { mutableStateOf(false) }
    var showOmitirDescSheet by remember { mutableStateOf(false) }
    var showConfirmarDireccionSheet by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)
    if (showMapPicker) {
        SeleccionarUbicacionScreen(
            onConfirmar = { latLng ->
                selectedLatLng = latLng
                location = "Obteniendo ubicación..."

                scope.launch {
                    val direccion = obtenerDireccionDesdeCoordenadas(context, latLng)
                    location = direccion
                    showMapPicker = false
                }
            },
            onBack = {
                showMapPicker = false
            }
        )
        return
    }
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
            Spacer(modifier = Modifier.height(20.dp))

            // --- BARRA SUPERIOR (CÍRCULO GRANDE Y CENTRADO + FLECHA FLOTANTE ADAPTABLE) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Flecha Atrás
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

                // Indicador circular de progreso
                if (step <= totalSteps) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeWidth = 6.dp
                        )
                        val colorPrimario = MaterialTheme.colorScheme.primary
                        val unCuartoDeVuelta = -90f
                        val proporcionProgreso = step.toFloat() / totalSteps.toFloat()
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawArc(
                                color = colorPrimario,
                                startAngle = unCuartoDeVuelta,
                                sweepAngle = 360f * proporcionProgreso,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 6.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = if (step <= totalSteps) "Mascota perdida" else "Hecho",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTENEDOR DINÁMICO DE PASOS ---
            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                when (step) {
                    1 -> PasoMascota(
                        nombre = nombreMascota, raza = razaMascota, type = petType, gen = gender,
                        onNombre = { nombreMascota = it }, onRaza = { razaMascota = it },
                        onType = { petType = it }, onGen = { gender = it }
                    )
                    2 -> PasoFoto(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoUbicacion(
                        lugar = location,
                        fecha = selectedDate,
                        selectedLatLng = selectedLatLng,
                        mostrarModal = showConfirmarDireccionSheet,
                        onLugar = { location = it },
                        onFecha = { selectedDate = it },
                        onOpenMap = { showMapPicker = true },
                        onDireccionConfirmada = {
                            showConfirmarDireccionSheet = false
                            step++
                        },
                        onDismissModal = { showConfirmarDireccionSheet = false }
                    )
                    4 -> PasoDescripcion(description) { description = it }
                    5 -> PasoContacto(
                        nombre = contactName, telefono = contactPhone, correo = contactEmail, aceptado = acceptedTerms,
                        onNombre = { contactName = it }, onTelefono = { contactPhone = it }, onCorreo = { contactEmail = it }, onAceptado = { acceptedTerms = it }
                    )
                    6 -> PantallaHecho(onBackToSelector)
                }
            }

            // --- BOTONES DE NAVEGACIÓN ---
            if (step in 1..totalSteps) {
                Spacer(modifier = Modifier.height(40.dp))
                NavigationButtons(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step == 3) {
                            showConfirmarDireccionSheet = true
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

                            val mascotaReportada = MascotasPerdidasModel(
                                id = 0,
                                idUsuario = 0,
                                nombreM = nombreMascota,
                                especie = petType,
                                genero = gender,
                                raza = razaMascota,
                                foto = fotoBytes,
                                fecha = sdf.format(Date(selectedDate)),
                                lugar = location,
                                descripcion = description,
                                contacto = contactName,
                                telefono = contactPhone,
                                correo = contactEmail
                            )

                            // Guardado en SQLite local
                            val resultadoLocal = dbHelper.insertPerdido(mascotaReportada)

                            if (resultadoLocal > -1) {
                                step = 6 // Cambia al frame de éxito

                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        // Definimos los tipos de medio para mayor claridad
                                        val textType = "text/plain".toMediaTypeOrNull()
                                        val imageType = "image/jpeg".toMediaTypeOrNull()

                                        // 1. Preparamos los textos usando el método create clásico
                                        val idUsuarioPart = okhttp3.RequestBody.create(textType, "0")
                                        val nombreMPart = okhttp3.RequestBody.create(textType, mascotaReportada.nombreM)
                                        val especiePart = okhttp3.RequestBody.create(textType, mascotaReportada.especie)
                                        val generoPart = okhttp3.RequestBody.create(textType, mascotaReportada.genero)
                                        val razaPart = okhttp3.RequestBody.create(textType, mascotaReportada.raza)
                                        val fechaPart = okhttp3.RequestBody.create(textType, mascotaReportada.fecha)
                                        val lugarPart = okhttp3.RequestBody.create(textType, mascotaReportada.lugar)
                                        val descPart = okhttp3.RequestBody.create(textType, mascotaReportada.descripcion)
                                        val contactoPart = okhttp3.RequestBody.create(textType, mascotaReportada.contacto)
                                        val telefonoPart = okhttp3.RequestBody.create(textType, mascotaReportada.telefono)
                                        val correoPart = okhttp3.RequestBody.create(textType, mascotaReportada.correo)

                                        // 2. Preparamos la foto usando el método create clásico
                                        val fotoPart = mascotaReportada.foto?.let { bytes ->
                                            val requestFile = okhttp3.RequestBody.create(imageType, bytes)
                                            okhttp3.MultipartBody.Part.createFormData("foto", "mascota.jpg", requestFile)
                                        }

                                        // 3. Enviamos los parámetros separados
                                        val response = com.example.teencontre.data.remote.RetrofitClient.instance.subirPerdido(
                                            idUsuarioPart, nombreMPart, especiePart, generoPart, razaPart,
                                            fechaPart, lugarPart, descPart, contactoPart, telefonoPart, correoPart, fotoPart
                                        )

                                        if (response.isSuccessful) {
                                            println("Sincronización exitosa con Azure.")
                                        } else {
                                            println("Error de Azure: ${response.code()}")
                                        }
                                    } catch (e: Exception) {
                                        println("Error de red: ${e.message}")
                                    }
                                }
                            } else {
                                println("Error al insertar de manera local en SQLite")
                            }
                        }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showOmitirFotoSheet = true
                        if (step == 4) showOmitirDescSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- DESPLIEGUE DE HOJAS EMERGENTES DE OMISIÓN ---
    if (showOmitirFotoSheet) {
        OmitirFotoSheet(
            onDismiss = { showOmitirFotoSheet = false },
            onConfirm = {
                showOmitirFotoSheet = false
                step++
            }
        )
    }
    if (showOmitirDescSheet) {
        OmitirDescripcionSheet(
            onDismiss = { showOmitirDescSheet = false },
            onConfirm = {
                showOmitirDescSheet = false
                step++
            }
        )
    }
}

// =================================================================
// COMPONENTE: BOTONES DE NAVEGACIÓN
// =================================================================
@Composable
fun NavigationButtons(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val botonHabilitado = step != 5 || accepted
        Button(
            onClick = onNext,
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5E4BCE),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (step == 5) "Publicar un anuncio" else "Siguiente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (step == 2 || step == 4) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF5E4BCE)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Omitir", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// =================================================================
// SUBPANTALLAS DEL WIZARD (DIFERENTES PASOS)
// =================================================================

@Composable
fun PasoMascota(
    nombre: String, raza: String, type: String, gen: String,
    onNombre: (String) -> Unit, onRaza: (String) -> Unit,
    onType: (String) -> Unit, onGen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Qué mascota es?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Por favor, indique el tipo y sexo de su mascota", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        Text("Nombre de la mascota", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = nombre, onValueChange = onNombre,
            placeholder = { Text("Nombre", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(12.dp))
        SelectorDoble("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(12.dp))
        SelectorDoble("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(12.dp))

        Text("Raza", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = raza, onValueChange = onRaza,
            placeholder = { Text("Raza de la mascota", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
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
fun SelectorDoble(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(
                    Modifier
                        .weight(1f).height(48.dp)
                        .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onSelect(op) }
                        .background(if (isSel) MaterialTheme.colorScheme.primary.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(op, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PasoFoto(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onPhotosChanged(photos + uris)
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿Como se ve?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Adjunta fotos de su mascota. Esto aumentará la posibilidad de encontrarla.",
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PasoUbicacion(
    lugar: String,
    fecha: Long,
    selectedLatLng: LatLng?,
    mostrarModal: Boolean,
    onLugar: (String) -> Unit,
    onFecha: (Long) -> Unit,
    onOpenMap: () -> Unit,
    onDireccionConfirmada: () -> Unit,
    onDismissModal: () -> Unit
){
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿Dónde y cuándo se perdió?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ayuda a delimitar la zona de búsqueda.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Fecha de extravío",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = dateFormatter.format(java.util.Date(fecha))
                Text(
                    text = formattedDate,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Desplegar fecha",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Última ubicación conocida",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpenMap,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Seleccionar ubicación en el mapa")
        }

        if (selectedLatLng != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ubicación seleccionada correctamente",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = lugar,
            onValueChange = onLugar,
            placeholder = {
                Text(
                    "Ej. Distrito, calle, avenidas de referencia,\nparque, lote",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 150.dp), // Permite expandirse si la dirección es larga
            shape = RoundedCornerShape(8.dp),
            singleLine = false, // Permite que el texto tenga varias líneas
            maxLines = 5,       // Ajusta según lo que quieras mostrar
            textStyle = TextStyle(fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onFecha(it) }
                    showDatePicker = false
                }) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarModal) {
        ModalBottomSheet(
            onDismissRequest = onDismissModal,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Es la dirección correcta?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Una dirección incorrecta reducirá la efectividad de su solicitud de mascota.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDireccionConfirmada,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5E4BCE),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Sí, correcto", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismissModal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("No, quiero cambiar", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PasoDescripcion(descripcion: String, onDescripcion: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Descripción adicional", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Describe características particulares (collares, cicatrices, temperamento)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = descripcion, onValueChange = onDescripcion,
            placeholder = { Text("Ej: Lleva un collar rojo. Es asustadiza pero no agresiva...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
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

@Composable
fun PasoContacto(
    nombre: String, telefono: String, correo: String, aceptado: Boolean,
    onNombre: (String) -> Unit, onTelefono: (String) -> Unit, onCorreo: (String) -> Unit, onAceptado: (Boolean) -> Unit
) {
    var terminosAceptados by remember { mutableStateOf(false) }
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
            value = nombre,
            onValueChange = onNombre,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Teléfono / Celular",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = telefono,
            onValueChange = onTelefono,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Correo electrónico",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = correo,
            onValueChange = onCorreo,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(20.dp))

        var datosPublicosAceptados by remember { mutableStateOf(aceptado) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = datosPublicosAceptados,
                onCheckedChange = { nuevoValor ->
                    datosPublicosAceptados = nuevoValor
                    onAceptado(nuevoValor && terminosAceptados)
                }
            )
            Text(
                text = "Acepto que mis datos de contacto sean públicos para la resolución del caso.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = terminosAceptados,
                onCheckedChange = { nuevoValor ->
                    terminosAceptados = nuevoValor
                    onAceptado(datosPublicosAceptados && nuevoValor)
                }
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { mostrarModalTerminos = true }
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ver términos",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp).size(15.dp).clickable { mostrarModalTerminos = true }
                )
            }
        }
    }

    // --- DIÁLOGO DE TÉRMINOS ---
    if (mostrarModalTerminos) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { mostrarModalTerminos = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                            "1. USO RESPONSABLE: Esta plataforma es exclusivamente para facilitar la adopción y el reencuentro de mascotas.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            "2. DATOS PERSONALES: Al registrarte, aceptas que tus datos de contacto sean visibles para otros usuarios cuando reportes o busques una mascota.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            "3. PROHIBICIONES: Está estrictamente prohibido lucrar o vender animales a través de esta aplicación.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            "4. COMUNIDAD: Nos reservamos el derecho de eliminar cuentas que realicen reportes falsos.",
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
fun PantallaHecho(onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Anuncio publicado correctamente!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Esperamos que tu mascota regrese pronto a casa. La comunidad ya está alerta.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al Inicio", fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// COMPONENTES: HOJAS EMERGENTES (BOTTOM SHEETS) ESTILIZADAS
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir fotos?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Los anuncios con fotos tienen un 80% más de probabilidad de éxito.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5E4BCE),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Añadir foto ahora", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Omitir de todas formas", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescripcionSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir descripción?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Detalles como el color del collar o marcas ayudan a diferenciar a tu mascota.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5E4BCE),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Escribir detalles", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Omitir de todas formas", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SeleccionarUbicacionScreen(
    ubicacionInicial: LatLng = LatLng(-11.9592875, -77.0052892),
    onConfirmar: (LatLng) -> Unit,
    onBack: () -> Unit
) {
    var puntoSeleccionado by remember { mutableStateOf<LatLng?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapaSelector(
            ubicacionInicial = ubicacionInicial,
            onPuntoSeleccionado = { puntoSeleccionado = it }
        )

        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 45.dp, start = 16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Volver")
        }

        Button(
            onClick = {
                puntoSeleccionado?.let { onConfirmar(it) }
            },
            enabled = puntoSeleccionado != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirmar ubicación")
        }
    }
}

@Composable
fun MapaSelector(
    ubicacionInicial: LatLng,
    onPuntoSeleccionado: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { googleMap ->
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 15f)
                    )

                    googleMap.setOnMapClickListener { latLng ->
                        googleMap.clear()
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Última ubicación conocida")
                        )
                        onPuntoSeleccionado(latLng)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

suspend fun obtenerDireccionDesdeCoordenadas(
    context: Context,
    latLng: LatLng
): String = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale("es", "PE"))
        val direcciones = geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        )

        val direccion = direcciones?.firstOrNull()

        if (direccion != null) {
            val partes = listOfNotNull(
                direccion.thoroughfare,
                direccion.subLocality,
                direccion.locality,
                direccion.adminArea
            ).distinct()

            partes.joinToString(", ").ifBlank {
                direccion.getAddressLine(0) ?: "Ubicación seleccionada"
            }
        } else {
            "Ubicación seleccionada"
        }
    } catch (e: Exception) {
        "Ubicación seleccionada"
    }
}

fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}