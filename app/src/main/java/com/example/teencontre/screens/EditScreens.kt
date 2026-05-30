package com.example.teencontre.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.model.MascotasAdopcionModel
import com.example.teencontre.data.model.MascotasEncontradasModel
import com.example.teencontre.data.model.MascotasPerdidasModel

// ============================================================================
// 1. PANTALLA: EDITAR MASCOTA PERDIDA
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPerdidoScreen(idMascota: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    // Estados para los campos específicos de addPerdido
    var nombreM by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var fotoBytes by remember { mutableStateOf<ByteArray?>(null) } // Mantiene la foto original

    // Cargar los datos actuales de la mascota al iniciar la pantalla
    LaunchedEffect(idMascota) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PERDIDOS} WHERE ${DatabaseHelper.PERDIDO_ID} = ?", arrayOf(idMascota.toString()))
        if (cursor.moveToFirst()) {
            nombreM = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_NOMBRE))
            especie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_ESPECIE))
            genero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_GENERO))
            raza = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_RAZA))
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_FECHA))
            lugar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_LUGAR))
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_DESCRIPCION))
            contacto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_CONTACTO))
            telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_TELEFONO))
            correo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_CORREO))
            fotoBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.PERDIDO_FOTO))
        }
        cursor.close()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Reporte Perdido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(value = nombreM, onValueChange = { nombreM = it }, label = { Text("Nombre de la mascota") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = especie, onValueChange = { especie = it }, label = { Text("Especie") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha y hora del extravío") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = lugar, onValueChange = { lugar = it }, label = { Text("Lugar / Zona") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = contacto, onValueChange = { contacto = it }, label = { Text("Persona de Contacto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción larga / Señas") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val updatedMascota = MascotasPerdidasModel(id = idMascota, nombreM = nombreM, especie = especie, genero = genero, raza = raza, foto = fotoBytes, fecha = fecha, lugar = lugar, descripcion = descripcion, contacto = contacto, telefono = telefono, correo = correo)
                        dbHelper.updatePerdido(updatedMascota)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Cambios", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// 2. PANTALLA: EDITAR MASCOTA ENCONTRADA
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEncontradaScreen(idMascota: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    // Estados para los campos específicos de addEncontrada
    var especie by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var fotoBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(idMascota) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ENCONTRADOS} WHERE ${DatabaseHelper.ENCONTRADO_ID} = ?", arrayOf(idMascota.toString()))
        if (cursor.moveToFirst()) {
            especie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_ESPECIE))
            genero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_GENERO))
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_FECHA))
            lugar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_LUGAR))
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_DESCRIPCION))
            contacto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_CONTACTO))
            telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_TELEFONO))
            correo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_CORREO))
            fotoBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.ENCONTRADO_FOTO))
        }
        cursor.close()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Reporte Encontrado", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(value = especie, onValueChange = { especie = it }, label = { Text("Especie (Perro, gato, etc.)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha y hora del hallazgo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = lugar, onValueChange = { lugar = it }, label = { Text("Lugar donde se encontró") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = contacto, onValueChange = { contacto = it }, label = { Text("Persona de Contacto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción o situación del animal") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val updatedMascota = MascotasEncontradasModel(id = idMascota, especie = especie, genero = genero, foto = fotoBytes, fecha = fecha, lugar = lugar, descripcion = descripcion, contacto = contacto, telefono = telefono, correo = correo)
                        dbHelper.updateEncontrada(updatedMascota)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Actualizar Publicación", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// 3. PANTALLA: EDITAR ANUNCIO DE ADOPCIÓN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAdopcionScreen(idMascota: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    // Estados para los campos específicos de addAdopcion
    var especie by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var vacunado by remember { mutableStateOf(false) }
    var esterilizado by remember { mutableStateOf(false) }
    var desparasitado by remember { mutableStateOf(false) }
    var tamano by remember { mutableStateOf("") }
    var temperamento by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var nombreOrganizacion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var fotoBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(idMascota) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ADOPCION} WHERE ${DatabaseHelper.ADOPCION_ID} = ?", arrayOf(idMascota.toString()))
        if (cursor.moveToFirst()) {
            especie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ESPECIE))
            genero = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_GENERO))
            raza = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_RAZA))
            vacunado = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_VACUNADO)) == 1
            esterilizado = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ESTERILIZADO)) == 1
            desparasitado = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_DESPARASITADO)) == 1
            tamano = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TAMANO))
            temperamento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TEMPERAMENTO))
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_DESCRIPCION))
            nombreOrganizacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_ORGANIZACION))
            telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_TELEFONO))
            correo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_CORREO))
            fotoBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.ADOPCION_FOTO))
        }
        cursor.close()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Caso de Adopción", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(value = especie, onValueChange = { especie = it }, label = { Text("Especie") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = tamano, onValueChange = { tamano = it }, label = { Text("Tamaño (Pequeño, Mediano, Grande)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = false)
            }
            item {
                OutlinedTextField(value = temperamento, onValueChange = { temperamento = it }, label = { Text("Temperamento (Ej: Juguetón)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = nombreOrganizacion, onValueChange = { nombreOrganizacion = it }, label = { Text("Nombre de la Organización / Dueño") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }

            // Selectores booleanos obligatorios en Material 3 (Checkboxes con fila integrada)
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Checkbox(checked = vacunado, onCheckedChange = { vacunado = it })
                    Text("¿Está Vacunado?", fontSize = 16.sp)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Checkbox(checked = esterilizado, onCheckedChange = { esterilizado = it })
                    Text("¿Está Esterilizado?", fontSize = 16.sp)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Checkbox(checked = desparasitado, onCheckedChange = { desparasitado = it })
                    Text("¿Está Desparasitado?", fontSize = 16.sp)
                }
            }

            item {
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Requisitos y descripción larga") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val updatedMascota = MascotasAdopcionModel(id = idMascota, especie = especie, genero = genero, raza = raza, vacunado = vacunado, esterilizado = esterilizado, desparasitado = desparasitado, tamano = tamano, temperamento = temperamento, foto = fotoBytes, descripcion = descripcion, nombreOrganizacion = nombreOrganizacion, telefono = telefono, correo = correo)
                        dbHelper.updateAdopcion(updatedMascota)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publicar Cambios", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}