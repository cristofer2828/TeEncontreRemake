package com.example.teencontre.actividades
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teencontre.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teencontre.viewmodel.PublicacionesViewModel

@Composable
fun EncuentranosScreen(
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val publicacionesViewModel: PublicacionesViewModel = viewModel()

    val publicaciones = publicacionesViewModel.publicaciones
    var mostrarFiltros by remember { mutableStateOf(false) }

    var desaparecido by remember { mutableStateOf(false) }
    var encontrado by remember { mutableStateOf(false) }
    var adopcion by remember { mutableStateOf(false) }

    var perro by remember { mutableStateOf(false) }
    var gato by remember { mutableStateOf(false) }
    var otro by remember { mutableStateOf(false) }
    val publicacionesFiltradas = publicaciones.filter { publicacion ->

        val coincideEstado =

            (!desaparecido && !encontrado && !adopcion)

                    ||

                    (desaparecido && publicacion.tipo == "PERDIDA")

                    ||

                    (encontrado && publicacion.tipo == "ENCONTRADA")

                    ||

                    (adopcion && publicacion.tipo == "ADOPCION")

        val coincideTipo =

            (!perro && !gato && !otro)

                    ||

                    (perro && publicacion.especie.equals("Perro", true))

                    ||

                    (gato && publicacion.especie.equals("Gato", true))

                    ||

                    (
                            otro &&
                                    !publicacion.especie.equals("Perro", true)
                                    &&
                                    !publicacion.especie.equals("Gato", true)
                            )

        coincideEstado && coincideTipo
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = onProfileClick,
                onPublishClick = onPublishClick,
                onEncuentranosClick = { },
                onMapaClick = { onNavigate("mapa") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anuncios",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = null
                )
            }

            // FILTROS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarFiltros = !mostrarFiltros }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text("Filtros: ")

                    Text(
                        text = "No seleccionado",
                        color = Color(0xFF6C63FF),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (mostrarFiltros)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // CONTENIDO DESPLEGABLE
            if (mostrarFiltros) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text("Estado de la mascota", fontWeight = FontWeight.Bold)

                        FiltroItem("Desaparecido", desaparecido) { desaparecido = it }
                        FiltroItem("Encontrado", encontrado) { encontrado = it }
                        FiltroItem("Busca un nuevo dueño.", adopcion) { adopcion = it }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Tipo", fontWeight = FontWeight.Bold)

                        FiltroItem("Perro", perro) { perro = it }
                        FiltroItem("Gato", gato) { gato = it }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { mostrarFiltros = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Aplicar Filtros")
                            }

                        }
                    }
                }
            }
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                )
            ) {

                publicacionesFiltradas.forEach { publicacion ->

                    val colorEstado = when (publicacion.tipo) {
                        "PERDIDA" -> Color(0xFFE53935)
                        "ENCONTRADA" -> Color(0xFF43A047)
                        "ADOPCION" -> Color(0xFF1E88E5)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                onNavigate("detalle_anuncio")
                            },
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {

                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            AsyncImage(
                                model = publicacion.foto,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(95.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .weight(1f)
                            ) {

                                Text(
                                    text = publicacion.nombreMascota
                                        ?: publicacion.especie,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(publicacion.tipo)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = colorEstado.copy(alpha = 0.15f),
                                        labelColor = colorEstado
                                    )
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = publicacion.descripcion,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = "📍 ${publicacion.lugar ?: "Ubicación no registrada"}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "🐾 ${publicacion.especie} • ${publicacion.genero}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }


        }
    }
}

@Composable
fun FiltroItem(
    texto: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            texto,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}