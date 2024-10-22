package vn.thanguit.thangbeo.utils.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.gun0912.tedpermission.rx3.TedPermission

object PermissionHelper {
    @SuppressLint("CheckResult")
    fun takePermission(
        permissions: Array<String>,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: (deniedPermissions: List<String>) -> Unit
    ) {
        TedPermission.create()
            .setPermissions(
                *permissions
            )
            .request()
            .subscribe({ tedPermissionResult ->
                if (tedPermissionResult.isGranted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied(tedPermissionResult.deniedPermissions)
                }
            }) { throwable ->
                throwable.printStackTrace()
            }
    }

    fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context == null) {
            return false
        }
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPermissionStorage(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun getPermissionLocation(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun getPermissionContact(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
    }
}