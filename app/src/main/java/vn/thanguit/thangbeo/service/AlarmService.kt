package vn.thanguit.thangbeo.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.gms.maps.model.LatLng
import vn.thanguit.thangbeo.utils.permission.PermissionHelper

class AlarmService : Service(), LocationListener {
    private var locationManager: LocationManager? = null
    private var targetLatLng: LatLng? = null
    private var alarmDistance: Float = 0f

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmDistance = intent?.getFloatExtra("alarmDistance", 0f) ?: 0f
        targetLatLng = intent?.getParcelableExtra("targetLatLng")

        if (!PermissionHelper.hasPermissions(
                this,
                *PermissionHelper.getPermissionLocationFull()
            )
        ) {
            return START_STICKY_COMPATIBILITY
        }

        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            10f,
            this
        )
        return START_STICKY
    }

    override fun onLocationChanged(location: Location) {
        targetLatLng?.let {
            val distance = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                it.latitude,
                it.longitude,
                distance
            )
            if (distance[0] <= alarmDistance) {
                triggerAlarm()
                stopSelf() // Dừng Service sau khi báo động
            }
        }
    }

    private fun triggerAlarm() {
        // Phát chuông hoặc rung để báo động
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    2000,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(2000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}