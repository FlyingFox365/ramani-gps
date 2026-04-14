package com.example.ramanigps


import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ramanigps.ui.theme.RamaniGPSTheme
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.MapLibre

class MainActivity : ComponentActivity(), LocationListener {

    lateinit var locationManager: LocationManager

    lateinit var notificationManager: NotificationManager

    var styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")
    
    val viewModel: GPSViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        val channel = NotificationChannel(
            "LOCATION_CHANNEL",
            "GPS notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        setContent {
            RamaniGPSTheme {
                var latLng by remember { mutableStateOf(LatLng(50.9, -1.4))}
                var dialogVisible by remember { mutableStateOf(false) }
                viewModel.latLngLiveData.observe(this) {
                    latLng = it
                }
                MapLibre(
                    modifier = Modifier.fillMaxSize(),
                    cameraPosition = CameraPosition(
                        target = latLng,
                        zoom = 14.0
                    ),
                    styleBuilder = styleBuilder
                ) {

                    Circle(
                        center=latLng,
                        radius=20f,
                        opacity=0.3f,
                        color="#0000ff",
                        onClick = {
                            dialogVisible = true
                        }
                    )
                }

                if (dialogVisible) {
                    AlertDialog(
                        title = { Text("Alert") },
                        text = { Text("Marker represents current position") },
                        onDismissRequest = { dialogVisible = false },
                        dismissButton = {
                            Button(onClick = {
                                dialogVisible = false
                            }) { Text("Dismiss") }
                        },
                        confirmButton = {
                            Button(onClick = {
                                dialogVisible = false
                            }) { Text("Confirm") }
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if(permissions.all{ checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            startGps()
        } else {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
                if (isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    startGps()
                }
                if(isGranted[Manifest.permission.POST_NOTIFICATIONS] ==true) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_LONG).show()
                }
            }
            launcher.launch(permissions)
        }
    }

    @SuppressLint("MissingPermission")
    fun startGps() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationChanged(location: Location) {
        viewModel.latLng = LatLng(location.latitude, location.longitude)

        val notification = Notification.Builder(this, "LOCATION_CHANNEL")
            .setContentTitle("Location changed")
            .setContentText("Latitude: ${location.latitude}\nLongitude: ${location.longitude}")
            .setSmallIcon(org.maplibre.android.R.drawable.maplibre_logo_icon)
            .build()
        notificationManager.notify(2616164, notification)
    }
}


