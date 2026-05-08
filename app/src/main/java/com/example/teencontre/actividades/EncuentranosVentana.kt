package com.example.teencontre.actividades

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

@Composable
fun EncuentranosScreen(
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigate: (String) -> Unit
) {

    var mostrarFiltros by remember { mutableStateOf(false) }

    var desaparecido by remember { mutableStateOf(false) }
    var encontrado by remember { mutableStateOf(false) }
    var adopcion by remember { mutableStateOf(false) }

    var perro by remember { mutableStateOf(false) }
    var gato by remember { mutableStateOf(false) }
    var otro by remember { mutableStateOf(false) }

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

            // LISTA DE ANUNCIOS
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                onNavigate("detalle_anuncio")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {

                            Image(
                                painter = painterResource(id = R.drawable.mascota1),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                Text("[DESCRIPCION]", fontWeight = FontWeight.Bold)
                                Text("ESTADO", color = Color(0xFF7C4DFF))
                                Text("Ubicación, fecha", fontSize = 12.sp)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                onNavigate("detalle_anuncio")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {

                            Image(
                                painter = painterResource(id = R.drawable.mascota2),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                Text("[DESCRIPCION]", fontWeight = FontWeight.Bold)
                                Text("ESTADO", color = Color(0xFF7C4DFF))
                                Text("Ubicación, fecha", fontSize = 12.sp)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                onNavigate("detalle_anuncio")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {

                            Image(
                                painter = painterResource(id = R.drawable.mascota3),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                Text("[DESCRIPCION]", fontWeight = FontWeight.Bold)
                                Text("ESTADO", color = Color(0xFF7C4DFF))
                                Text("Ubicación, fecha", fontSize = 12.sp)
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