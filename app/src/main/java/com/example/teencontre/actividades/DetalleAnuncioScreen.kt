package com.example.teencontre.actividades

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.teencontre.data.model.Comentario
import com.example.teencontre.data.remote.RetrofitClient
import com.example.teencontre.viewmodel.PublicacionSeleccionadaViewModel
import com.example.teencontre.sharedprefs.PreferenceManager // <-- Asegúrate de tener este import
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DetalleAnuncioScreen(
    onBack: () -> Unit,
    onVerUbicacion: (String) -> Unit
) {
    val viewModel: PublicacionSeleccionadaViewModel = viewModel()
    val publicacion = viewModel.publicacionSeleccionada.value
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val nombreUsuarioReal = prefs.getUserName() ?: "Usuario Anónimo"
    val scope = rememberCoroutineScope()

    // 1. Inicializamos PreferenceManager para obtener el usuario real


    // Lista reactiva vinculada directamente con Azure SQL
    val listaComentarios = remember { mutableStateListOf<Comentario>() }

    var nuevoComentarioTexto by remember { mutableStateOf("") }
    var cargandoComentarios by remember { mutableStateOf(true) }

    // GET: Carga los comentarios guardados al abrir la publicación
    LaunchedEffect(publicacion) {
        if (publicacion != null) {
            try {
                cargandoComentarios = true

                val response = RetrofitClient.instance.obtenerComentarios(
                    idPublicacion = publicacion.id,
                    tipoPublicacion = "PERDIDA"
                )

                if (response.isSuccessful) {
                    val comentarios = response.body() ?: emptyList()
                    listaComentarios.clear()
                    listaComentarios.addAll(comentarios)
                } else {
                    Toast.makeText(
                        context,
                        "Error del servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al cargar comentarios: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                cargandoComentarios = false
            }
        }
    }

    if (publicacion == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontró la publicación")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Button(onClick = onBack) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        publicacion.foto?.let { foto ->
            AsyncImage(
                model = foto,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = publicacion.nombreMascota ?: publicacion.especie,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = publicacion.tipo,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = publicacion.descripcion)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Especie: ${publicacion.especie}")
        Text("Género: ${publicacion.genero}")

        publicacion.raza?.let { Text("Raza: $it") }
        publicacion.fecha?.let { Text("Fecha: $it") }

        Spacer(modifier = Modifier.height(12.dp))

        publicacion.telefono?.let { Text("Teléfono: $it") }
        publicacion.correo?.let { Text("Correo: $it") }

        if (publicacion.tipo == "ADOPCION") {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Estado de salud", fontWeight = FontWeight.Bold)
            Text("Vacunado: ${if (publicacion.vacunado == true) "Sí" else "No"}")
            Text("Esterilizado: ${if (publicacion.esterilizado == true) "Sí" else "No"}")
            Text("Desparasitado: ${if (publicacion.desparasitado == true) "Sí" else "No"}")

            publicacion.tamano?.let { Text("Tamaño: $it") }
            publicacion.temperamento?.let { Text("Temperamento: $it") }
            publicacion.nombreOrganizacion?.let { Text("Organización: $it") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!publicacion.lugar.isNullOrBlank()) {
            Button(
                onClick = { onVerUbicacion(publicacion.lugar) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📍 Ver ubicación")
            }
        }

        // ====================================================================
        // SECCIÓN DE COMENTARIOS REALES (AZURE SQL)
        // ====================================================================
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Comentarios (${listaComentarios.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nuevoComentarioTexto,
                onValueChange = { nuevoComentarioTexto = it },
                placeholder = { Text("Escribe un comentario...") },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                enabled = !cargandoComentarios
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (nuevoComentarioTexto.isNotBlank()) {
                        val comentarioEnviado = nuevoComentarioTexto.trim()
                        nuevoComentarioTexto = ""

                        // 2. Generamos la hora exacta de Perú al momento de hacer clic
                        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("America/Lima")
                        val horaPeru = sdf.format(Date())

                        // POST: Inserta a través de tu archivo PHP en la base de datos Azure
                        scope.launch {
                            try {
                                val comentarioDto = Comentario(
                                    id_publicacion = publicacion.id,
                                    tipo_publicacion = publicacion.tipo,
                                    nombre_usuario = nombreUsuarioReal,
                                    mensaje = comentarioEnviado,
                                    tiempo = horaPeru
                                )

                                val response = RetrofitClient.instance.enviarComentario(comentarioDto)

                                if (response.isSuccessful && response.body()?.success == true) {

                                    listaComentarios.add(0, comentarioDto)

                                    Toast.makeText(
                                        context,
                                        "Comentario enviado con éxito",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error del servidor: ${response.body()?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error en la red: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !cargandoComentarios,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar comentario"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargandoComentarios) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))
            }
        } else if (listaComentarios.isEmpty()) {
            Text(
                text = "No hay comentarios aún. ¡Sé el primero!",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            listaComentarios.forEach { comentario ->
                ItemComentario(comentario = comentario)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ItemComentario(comentario: Comentario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comentario.nombre_usuario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = comentario.tiempo ?: "Ahora",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comentario.mensaje,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}