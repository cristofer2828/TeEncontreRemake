package com.example.teencontre.actividades

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.Locale

data class Ubicacion(
    val ubicacion: LatLng,
    val titulo: String,
    val descripcion: String
)

@Composable
fun MapScreen(
    direccionPublicacion: String?,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    var pantallaActiva by remember { mutableStateOf("mapa") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = pantallaActiva, // <-- IMPRESCINDIBLE: Pasas la ruta actual para que sepa qué iluminar
                onProfileClick = onProfileClick,
                onPublishClick = onPublishClick,
                onEncuentranosClick = {
                    pantallaActiva = "encuentranos" // Actualiza el estado local
                    onNavigate("encuentranos")
                },
                onMapaClick = {
                    pantallaActiva = "mapa" // <-- CORREGIDO: Actualiza el estado al hacer clic en el mapa
                    onNavigate("mapa") // Si usas navegación formal, ejecuta su acción aquí
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val ubicacionDefault = Ubicacion(
                LatLng(-11.9592875, -77.0052892),
                "Ubicación",
                direccionPublicacion ?: "Mascotas cerca de ti"
            )
            android.util.Log.d(
                "MAPA_TEST",
                "Direccion recibida = $direccionPublicacion"
            )
            MyMap(ubicacionDefault) { }


            Button(
                onClick = { onNavigate("selector") },
                modifier = Modifier
                    .padding(top = 45.dp, start = 16.dp)
                    .align(Alignment.TopStart),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Volver", color = Color.White)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MyMap(ubicacion: Ubicacion, onReady: (GoogleMap) -> Unit) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }
    //ubicacion public
    var ubicacionPublicacion by remember {
        mutableStateOf<LatLng?>(null)
    }
    LaunchedEffect(ubicacion.descripcion) {

        try {

            Log.d(
                "MAPA_TEST",
                "Buscando direccion: ${ubicacion.descripcion}"
            )

            val geocoder = Geocoder(
                context,
                Locale.getDefault()
            )

            val resultados = geocoder.getFromLocationName(
                ubicacion.descripcion,
                1
            )

            Log.d(
                "MAPA_TEST",
                "Resultados encontrados: ${resultados?.size}"
            )

            if (!resultados.isNullOrEmpty()) {

                ubicacionPublicacion = LatLng(
                    resultados[0].latitude,
                    resultados[0].longitude
                )

                Log.d(
                    "MAPA_TEST",
                    "LAT=${resultados[0].latitude} LNG=${resultados[0].longitude}"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "MAPA_TEST",
                "Error Geocoder",
                e
            )
        }
    }

// Espera a que el mapa y la ubicación estén listos
    LaunchedEffect(
        googleMapRef,
        ubicacionPublicacion
    ) {

        if (
            googleMapRef != null &&
            ubicacionPublicacion != null
        ) {

            Log.d(
                "MAPA_TEST",
                "MAPA Y UBICACION LISTOS"
            )

            // NO uses clear() por ahora

            googleMapRef!!.addMarker(
                MarkerOptions()
                    .position(ubicacionPublicacion!!)
                    .title("Mascota reportada")
                    .snippet(ubicacion.descripcion)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )
            )

            googleMapRef!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    ubicacionPublicacion!!,
                    17f
                )
            )
        }
    }

    fun tienePermisoUbicacion(): Boolean {
        val permisoFino = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return permisoFino || permisoAproximado
    }

    fun moverAMiUbicacion(googleMap: GoogleMap) {

        if (tienePermisoUbicacion()) {

            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->

                if (location != null) {

                    val miUbicacion = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(miUbicacion)
                            .title("Mi ubicación actual")
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE
                                )
                            )
                    )
                }
            }
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val permisoConcedido =
            permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (permisoConcedido) {
            googleMapRef?.let { moverAMiUbicacion(it) }
        }
    }

    lifecycle.addObserver(rememberMapLifeCycle(map = mapView))

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { googleMap ->
                    Log.d(
                        "MAPA_TEST",
                        "MAPA LISTO"
                    )
                    googleMapRef = googleMap
                    ubicacionPublicacion?.let { destino ->

                        Log.d(
                            "MAPA_TEST",
                            "AGREGANDO MARCADOR VERDE DESDE MAPA LISTO"
                        )

                        googleMap.addMarker(
                            MarkerOptions()
                                .position(destino)
                                .title("Mascota reportada")
                                .snippet(ubicacion.descripcion)
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN
                                    )
                                )
                        )

                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                destino,
                                17f
                            )
                        )
                    }
                    if (tienePermisoUbicacion()) {
                        moverAMiUbicacion(googleMap)
                    } else {
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(ubicacion.ubicacion, 15f)
                        )
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(ubicacion.ubicacion)
                                .title(ubicacion.titulo)
                                .snippet(ubicacion.descripcion)
                        )

                        permisoLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }

                    onReady(googleMap)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun rememberMapLifeCycle(map: MapView): LifecycleObserver {
    return remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> map.onCreate(Bundle())
                Lifecycle.Event.ON_START -> map.onStart()
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                Lifecycle.Event.ON_STOP -> map.onStop()
                Lifecycle.Event.ON_DESTROY -> map.onDestroy()
                else -> {}
            }
        }
    }
}