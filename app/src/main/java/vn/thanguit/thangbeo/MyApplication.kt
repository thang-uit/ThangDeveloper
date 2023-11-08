package vn.thanguit.thangbeo

import android.app.Application
import android.util.Log

class MyApplication : Application() {

    companion object {
        const val TAG = "THANGBEO"
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application")

        instance = this
    }
}