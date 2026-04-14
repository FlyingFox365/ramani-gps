package com.example.ramanigps


import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.ramanigps.ui.theme.RamaniGPSTheme
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.MapLibre

class MainActivity : ComponentActivity(), LocationListener {

    lateinit var locationManager: LocationManager

    // You must create the style builder as an attribute of the activity and then
    // pass it to composables as a parameter. If you create the style builder on
    // each recompose, the app may crash!
    var styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")
    
    val viewModel: GPSViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
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
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if(checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            startGps()
        } else {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    startGps()
                } else {
                    Toast.makeText(this, "Cannot access GPS as permission denied", Toast.LENGTH_LONG).show()
                }
            }
            launcher.launch(permission)
        }
    }

    @SuppressLint("MissingPermission")
    fun startGps() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        viewModel.latLng = LatLng(location.latitude, location.longitude)
    }
}


