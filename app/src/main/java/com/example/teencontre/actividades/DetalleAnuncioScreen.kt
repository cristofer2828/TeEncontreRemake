package com.example.teencontre.actividades

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.teencontre.R
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.TopAppBar

data class Comentario(
    val nombre: String,
    val mensaje: String,
    val tiempo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAnuncioScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {

    var visible by remember { mutableStateOf(false) }

    var nuevoComentario by remember { mutableStateOf("") }
    var comentarios by remember {
        mutableStateOf(
            listOf(
                Comentario("Carlos", "Creo que lo vi cerca del parque", "hace 2 horas"),
                Comentario("María", "Está igualito al que vi ayer", "hace 5 horas"),
                Comentario("Luis", "Voy a estar atento por mi zona", "hace 1 día")
            )
        )
    }

    //Estado cambiaria y se veria el modal de dueño y cel
    var mostrarModal by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del anuncio") },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigate("encuentranos")
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { onNavigate("profile") },
                onPublishClick = { onNavigate("selector") },
                onEncuentranosClick = { onNavigate("encuentranos") },
                onMapaClick = { onNavigate("mapa") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Image(
                painter = painterResource(R.drawable.adopta),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "[Tipo] [Estado], [Ciudad]",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "[DESCRIPCION]",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { mostrarModal = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Mostrar número")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Compartir")
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, contentDescription = null)
                }
            }

            Divider()

            InfoRow("ID Anuncio", "4257893")
            InfoRow("Raza", "-")
            InfoRow("Género", "Macho")
            InfoRow("Fecha anuncio", "[FECHA]")
            InfoRow("Fecha pérdida", "[FECHA]")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = nuevoComentario,
                    onValueChange = { nuevoComentario = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un comentario...") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (nuevoComentario.isNotBlank()) {

                            val nuevo = Comentario(
                                nombre = "Tú",
                                mensaje = nuevoComentario,
                                tiempo = "ahora"
                            )

                            comentarios = listOf(nuevo) + comentarios // 🔥 agrega arriba
                            nuevoComentario = ""
                        }
                    }
                ) {
                    Text("Enviar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Comentarios",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            comentarios.forEachIndexed { index, comentario ->

                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    visible = true
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(1000)
                    ) +
                            scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(500)
                            ) +
                            expandVertically(
                                animationSpec = tween(500))
                ) {
                    ComentarioItem(comentario)
                }
            }

        }

        if (mostrarModal) {
            ModalBottomSheet(
                onDismissRequest = { mostrarModal = false }
            ) {
                ContenidoModal(
                    nombre = "Juan Pérez",
                    telefono = "987654321"
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value)
    }
}



@Composable
fun ContenidoModal(nombre: String, telefono: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Contacto",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Dueño: $nombre")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Teléfono: $telefono")

        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun ComentarioItem(comentario: Comentario) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )

    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = comentario.nombre,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = comentario.tiempo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = comentario.mensaje)
        }
    }
}