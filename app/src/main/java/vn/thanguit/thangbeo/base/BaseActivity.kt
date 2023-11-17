package vn.thanguit.thangbeo.base

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import vn.thanguit.thangbeo.dialog.DeviceInfoBottomSheetDialog
import vn.thanguit.thangbeo.dialog.QRCodeDialog
import java.io.ByteArrayOutputStream


abstract class BaseActivity : AppCompatActivity() {

    private var deviceInfoBottomSheetDialog: DeviceInfoBottomSheetDialog? = null
    private var qrCodeDialog: QRCodeDialog? = null

//    private var viewSnackBar: View? = null

    // ---------------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

//        viewSnackBar = findViewById(R.id.layoutRoot)
    }

    override fun onDestroy() {
        deviceInfoBottomSheetDialog?.dismiss()
        qrCodeDialog?.dismiss()

        super.onDestroy()
    }

    // ---------------------------------------------------------------------------------------------

    fun goDeeplink(data: String?) {
        if (!TextUtils.isEmpty(data)) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data)))
            } catch (e: Exception) {
                e.printStackTrace()
                showToast(e.message)
            }
        }
    }

    protected fun launchActivity(clazz: Class<out Activity>) {
        startActivity(Intent(this, clazz))
    }

    protected fun launchActivity(intent: Intent) {
        startActivity(intent)
    }

    fun showToast(message: String?) {
        if (!TextUtils.isEmpty(message)) {
            runOnUiThread {
                Toast.makeText(this, message?.trim(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun showSnackBar(view: View?, message: String?) {
        if (!TextUtils.isEmpty(message)) {
            view?.let {
                val snackBar = Snackbar
                    .make(it, message!!.trim(), Snackbar.LENGTH_LONG)
                snackBar.show()
            }
        }
    }

    fun getImageUri(bitmap: Bitmap, quality: Int = 100, title: String = "Share image"): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, title, null)
        return Uri.parse(path)
    }

    fun shareImageBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(it))
                startActivity(Intent.createChooser(shareIntent, "Share image"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun copyText(content: String?) {
        if (!TextUtils.isEmpty(content)) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copy", content)
            clipboard.setPrimaryClip(clip)
        }
    }

    fun shareText(text: String?) {
        if (!TextUtils.isEmpty(text)) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, text)
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "Share"))
        }
    }

    fun hideSoftKeyboardClickOutSide(context: Context?, editText: EditText?) {
        // https://stackoverflow.com/a/19828165
        if (context == null) return
        editText?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager =
                    context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    fun changeStatusBar(color: Int) {
        window?.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            it.statusBarColor = color
        }
    }

    fun showBottomSheetDialog(title: String?, content: String?) {
        deviceInfoBottomSheetDialog?.dismiss()

        deviceInfoBottomSheetDialog = DeviceInfoBottomSheetDialog(this, title, content)
        deviceInfoBottomSheetDialog?.setOnCancelListener {
            deviceInfoBottomSheetDialog = null
        }
        if (!isFinishing && !isDestroyed) {
            deviceInfoBottomSheetDialog?.show()
        }
    }

    fun showQRCodeDialog(qrCode: String?) {
        qrCodeDialog?.dismiss()

        qrCodeDialog = QRCodeDialog(this, qrCode)
        qrCodeDialog?.setOnCancelListener {
            qrCodeDialog = null
        }
        if (!isFinishing && !isDestroyed) {
            qrCodeDialog?.show()
        }
    }
}