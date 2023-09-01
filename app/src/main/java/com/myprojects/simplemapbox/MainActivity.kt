package com.myprojects.simplemapbox

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

class MainActivity : AppCompatActivity() {
    private var mapView: MapView? = null
    var floatingActionButton: FloatingActionButton? = null
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            Toast.makeText(this@MainActivity, "Permission granted!", Toast.LENGTH_SHORT).show()
        }
    }
    private val onIndicatorBearingChangedListener =
        OnIndicatorBearingChangedListener { v ->
            mapView!!.getMapboxMap().setCamera(CameraOptions.Builder().bearing(v).build())
        }
    private val onIndicatorPositionChangedListener =
        OnIndicatorPositionChangedListener { point ->
            mapView!!.getMapboxMap()
                .setCamera(CameraOptions.Builder().center(point).zoom(20.0).build())
            mapView!!.gestures.focalPoint = mapView!!.getMapboxMap().pixelForCoordinate(point)
        }
    private val onMoveListener: OnMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            mapView!!.location.removeOnIndicatorBearingChangedListener(
                onIndicatorBearingChangedListener
            )
            mapView!!.location.removeOnIndicatorPositionChangedListener(
                onIndicatorPositionChangedListener
            )
            mapView!!.gestures.removeOnMoveListener(this)
            floatingActionButton!!.show()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        floatingActionButton = findViewById(R.id.focusLocation)
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        floatingActionButton!!.hide()
        mapView!!.getMapboxMap().loadStyleUri(Style.TRAFFIC_DAY) {
            mapView!!.getMapboxMap().setCamera(CameraOptions.Builder().zoom(10.0).build())
            val locationComponentPlugin = mapView!!.location
            locationComponentPlugin.enabled = true
            val locationPuck2D = LocationPuck2D()
            locationPuck2D.bearingImage = AppCompatResources.getDrawable(
                this@MainActivity,
                R.drawable.baseline_location_on_24
            )
            locationComponentPlugin.locationPuck = locationPuck2D
            locationComponentPlugin.addOnIndicatorBearingChangedListener(
                onIndicatorBearingChangedListener
            )
            locationComponentPlugin.addOnIndicatorPositionChangedListener(
                onIndicatorPositionChangedListener
            )
            mapView!!.gestures.addOnMoveListener(onMoveListener)
            floatingActionButton!!.setOnClickListener {
                locationComponentPlugin.addOnIndicatorBearingChangedListener(
                    onIndicatorBearingChangedListener
                )
                locationComponentPlugin.addOnIndicatorPositionChangedListener(
                    onIndicatorPositionChangedListener
                )
                mapView!!.gestures.addOnMoveListener(onMoveListener)
                floatingActionButton!!.hide()
            }
        }
    }
}