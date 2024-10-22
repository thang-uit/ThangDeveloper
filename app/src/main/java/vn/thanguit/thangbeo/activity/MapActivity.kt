package vn.thanguit.thangbeo.activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import vn.thanguit.thangbeo.R
import vn.thanguit.thangbeo.base.BaseActivity
import vn.thanguit.thangbeo.databinding.ActivityMapBinding
import vn.thanguit.thangbeo.utils.permission.PermissionHelper
import java.util.Stack

class MapActivity : BaseActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "MapActivity"
    }

    private lateinit var binding: ActivityMapBinding

    private var mapFragment: SupportMapFragment? = null
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocationMarker: Marker? = null
    private val markerList = mutableListOf<Marker>()
    private val polylineList = mutableListOf<Polyline>()
    private val undoStack = Stack<UndoRedoAction>()
    private val redoStack = Stack<UndoRedoAction>()

    private val NORMAL_MARKER_SIZE = 1.0f
    private val SELECTED_MARKER_SIZE = 1.5f

//    private val CURRENT_LOCATION_MARKER_ICON = R.drawable.ic_marker_current_location_default
//    private val SELECTED_CURRENT_LOCATION_MARKER_ICON =
//        R.drawable.ic_marker_current_location_selected
//    private val NORMAL_MARKER_ICON = R.drawable.ic_marker_default
//    private val SELECTED_MARKER_ICON = R.drawable.ic_marker_selected

    // ---------------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        listener()
    }

    // ---------------------------------------------------------------------------------------------

    private fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnClearMarkers.isSelected = true
        binding.btnUndo.isSelected = true
        binding.btnRedo.isSelected = true
        binding.btnLoadMap.isSelected = true

        mapFragment = supportFragmentManager.findFragmentById(R.id.fmMap) as SupportMapFragment
        takePermissionLocation()
        reloadMap()
    }

    private fun listener() {
        binding.btnClearMarkers.setOnClickListener {
            clearAllMarkersAndLines()
        }

        binding.btnUndo.setOnClickListener {
            actionUndo()
        }

        binding.btnRedo.setOnClickListener {
            actionRedo()
        }

        binding.btnLoadMap.setOnClickListener {
            loadViewMarkerMap()
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun takePermissionLocation() {
        PermissionHelper.takePermission(PermissionHelper.getPermissionLocation(), {
            reloadMap()
            getDeviceLocation()
        }, { deniedPermissions -> null })
    }

    private fun reloadMap() {
        mapFragment?.getMapAsync(this)
    }

    // ---------------------------------------------------------------------------------------------

    override fun onMapReady(googleMap: GoogleMap) {
        if (googleMap == null) {
            return
        }

        mGoogleMap = googleMap

        updateLocationUI()

        mGoogleMap?.let { gm ->
            gm.setOnMapClickListener { latLng ->
                addMarker(latLng)
            }

            gm.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}

                override fun onMarkerDrag(marker: Marker) {
                    updatePolylinePosition(marker)
                }

                override fun onMarkerDragEnd(marker: Marker) {}
            })
        }
    }

    private fun updateLocationUI() {
        if (mGoogleMap == null) {
            return
        }
        try {
            if (PermissionHelper.hasPermissions(this, *PermissionHelper.getPermissionLocation())) {
                mGoogleMap?.isMyLocationEnabled = true
                mGoogleMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mGoogleMap?.isMyLocationEnabled = false
                mGoogleMap?.uiSettings?.isMyLocationButtonEnabled = false
                takePermissionLocation()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getDeviceLocation() {
        try {
            if (PermissionHelper.hasPermissions(this, *PermissionHelper.getPermissionLocation())) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        mGoogleMap?.let { gm ->
                            gm.clear()

                            val currentLocation = LatLng(location.latitude, location.longitude)

                            currentLocationMarker = gm.addMarker(
                                MarkerOptions().position(currentLocation).title("Your Location")
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_YELLOW
                                        )
                                    )
                            )!!

                            mGoogleMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation, 15f
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getBitmapDescriptorFromDrawable(drawableId: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addMarker(location: LatLng) {
        mGoogleMap?.let { gm ->
            val markerOptions = MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker())

            val marker = gm.addMarker(markerOptions)
            marker?.let {
                markerList.add(it)

                undoStack.push(
                    UndoRedoAction.AddMarker(it, markerOptions, gm, markerList, polylineList)
                )
                redoStack.clear()

                if (markerList.size > 1) {
                    val previousMarker = markerList[markerList.size - 2]
                    val polylineOptions = PolylineOptions()
                        .add(previousMarker.position, it.position)
                        .width(5f)
                        .color(Color.RED)

                    val polyline = gm.addPolyline(polylineOptions)
                    polylineList.add(polyline)

                    undoStack.push(
                        UndoRedoAction.DrawLine(
                            polyline,
                            polylineOptions,
                            gm,
                            markerList,
                            polylineList
                        )
                    )
                    redoStack.clear()
                }
            }
        }
    }


    private fun updatePolylinePosition(marker: Marker) {
        for (polyline in polylineList) {
            if (polyline.points[0] == marker.position || polyline.points[1] == marker.position) {
                val points = polyline.points
                if (points[0] == marker.position) {
                    points[0] = marker.position
                } else {
                    points[1] = marker.position
                }
                polyline.points = points
            }
        }
    }

    private fun clearAllMarkersAndLines() {
        for (marker in markerList) {
            marker.remove()
        }
        markerList.clear()

        for (polyline in polylineList) {
            polyline.remove()
        }
        polylineList.clear()
    }

    private fun loadViewMarkerMap() {
        val builder = LatLngBounds.Builder()
        for (marker in markerList) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        val padding = 100
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mGoogleMap?.animateCamera(cu)
    }


    fun actionUndo() {
        if (undoStack.isNotEmpty()) {
            val action = undoStack.pop()
            redoStack.push(action)
            action.undo()
        }
    }

    fun actionRedo() {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.pop()
            undoStack.push(action)
            action.redo()
        }
    }

    sealed class UndoRedoAction(
        val markerList: MutableList<Marker>,
        val polylineList: MutableList<Polyline>
    ) {
        abstract fun undo()
        abstract fun redo()

        class AddMarker(
            private val marker: Marker,
            private val markerOptions: MarkerOptions,
            private val map: GoogleMap,
            markerList: MutableList<Marker>,
            polylineList: MutableList<Polyline>
        ) : UndoRedoAction(markerList, polylineList) {
            override fun undo() {
                marker.remove()
                markerList.remove(marker)
            }

            override fun redo() {
                val newMarker = map.addMarker(markerOptions)
                markerList.add(newMarker!!)
            }
        }

        class DrawLine(
            private val polyline: Polyline,
            private val polylineOptions: PolylineOptions,
            private val map: GoogleMap,
            markerList: MutableList<Marker>,
            polylineList: MutableList<Polyline>
        ) : UndoRedoAction(markerList, polylineList) {
            override fun undo() {
                polyline.remove()
                polylineList.remove(polyline)
            }

            override fun redo() {
                val newPolyline = map.addPolyline(polylineOptions)
                polylineList.add(newPolyline)
            }
        }
    }

}