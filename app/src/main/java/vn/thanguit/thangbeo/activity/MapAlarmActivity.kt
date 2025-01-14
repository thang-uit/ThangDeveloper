package vn.thanguit.thangbeo.activity

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Scroller
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import vn.thanguit.thangbeo.BuildConfig
import vn.thanguit.thangbeo.R
import vn.thanguit.thangbeo.base.BaseActivity
import vn.thanguit.thangbeo.databinding.ActivityMapAlarmBinding
import vn.thanguit.thangbeo.utils.adaptViewForInserts
import vn.thanguit.thangbeo.utils.permission.PermissionHelper

class MapAlarmActivity : BaseActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "MapAlarmActivity"
    }

    private lateinit var binding: ActivityMapAlarmBinding

    private var mapFragment: SupportMapFragment? = null
    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocationMarker: Marker? = null
    private var targetLocationMarker: Marker? = null
    private var polyline: Polyline? = null
    private var alarmDistance: Float = 0f  // khoảng cách để báo thức

    private val client = OkHttpClient()
    private val apiKey = BuildConfig.MAPS_API_KEY

    // =============================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTextColor(isDarkText = true, isTransparent = true)
        binding.llHeader.adaptViewForInserts()

        initView()
        listener()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // =============================================================================================

    private fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.fmMap) as SupportMapFragment
        takePermissionLocation()
        reloadMap()
    }

    private fun listener() {
        binding.edtLocation.let {
            it.movementMethod = ScrollingMovementMethod()
            it.setScroller(Scroller(this))
            it.isVerticalScrollBarEnabled = true

            hideSoftKeyboardClickOutSide(this, it)

            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s?.toString()
                    setShowIconClearText(!text.isNullOrEmpty())
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
        }

        binding.ivClearText.setOnClickListener {
            setEditTextLocation("")
        }
    }

    private fun setShowIconClearText(isShow: Boolean) {
        binding.ivClearText.visibility = if (isShow) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setEditTextLocation(value: String?) {
        binding.edtLocation.setText(value)
    }

    private fun takePermissionLocation() {
        PermissionHelper.takePermission(PermissionHelper.getPermissionLocationFull(), {
            reloadMap()
            getDeviceLocation()
        }, { deniedPermissions -> null })
    }

    private fun reloadMap() {
        mapFragment?.getMapAsync(this)
    }

    private fun getDeviceLocation() {
        try {
            if (PermissionHelper.hasPermissions(
                    this,
                    *PermissionHelper.getPermissionLocationFull()
                )
            ) {
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

    private fun updateLocationUI() {
        try {
            mGoogleMap?.let {
                if (PermissionHelper.hasPermissions(
                        this,
                        *PermissionHelper.getPermissionLocationFull()
                    )
                ) {
                    it.isMyLocationEnabled = true
                    it.uiSettings.isMyLocationButtonEnabled = true
                } else {
                    it.isMyLocationEnabled = false
                    it.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun addMarker(location: LatLng) {
        mGoogleMap?.let { gm ->
            val markerOptions = MarkerOptions()
                .position(location)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker())

            targetLocationMarker?.remove()
            targetLocationMarker = gm.addMarker(markerOptions)

            getShortestRouteDistance(currentLocationMarker?.position, location)
        }
    }

    private fun getShortestRouteDistance(origin: LatLng?, destination: LatLng?) {
        if (origin == null || destination == null) {
            return
        }
        val url = getDirectionsUrl(origin, destination)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = makeNetworkRequest(url)
                withContext(Dispatchers.Main) {
                    parseDirectionsResult(response)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching directions: ", e)
            }
        }
    }

    private fun getDirectionsUrl(origin: LatLng, destination: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}&mode=driving&key=$apiKey"
    }

    private fun makeNetworkRequest(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            return response.body?.string() ?: throw Exception("No response body")
        }
    }

    private fun parseDirectionsResult(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() == 0) {
                Log.e("Directions", "No routes found")
                return
            }

            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val leg = legs.getJSONObject(0)
            val distance = leg.getJSONObject("distance").getString("text")
            val duration = leg.getJSONObject("duration").getString("text")

            val polylinePoints = route.getJSONObject("overview_polyline").getString("points")
            drawPolyline(polylinePoints)

            targetLocationMarker?.let {
                it.snippet = "Khoảng cách: $distance, Thời gian: $duration"
                it.showInfoWindow()
            }
        } catch (e: Exception) {
            Log.e("Directions", "Error parsing directions", e)
        }
    }

    private fun drawPolyline(encodedPolyline: String) {
        val decodedPath = PolyUtil.decode(encodedPolyline)
        mGoogleMap?.addPolyline(
            PolylineOptions().addAll(decodedPath).color(Color.BLUE).width(10f)
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (googleMap == null) {
            return
        }

        mGoogleMap = googleMap

        mGoogleMap?.let { gm ->
            gm.setOnMapClickListener { latLng ->
                addMarker(latLng)
            }

            gm.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}

                override fun onMarkerDrag(marker: Marker) {
                }

                override fun onMarkerDragEnd(marker: Marker) {}
            })
        }
    }
}