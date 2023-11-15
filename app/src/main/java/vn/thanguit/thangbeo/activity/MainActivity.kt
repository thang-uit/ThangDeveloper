package vn.thanguit.thangbeo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Scroller
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.gun0912.tedpermission.rx3.TedPermission
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import vn.thanguit.thangbeo.R
import vn.thanguit.thangbeo.base.BaseActivity
import vn.thanguit.thangbeo.databinding.ActivityMainBinding
import vn.thanguit.thangbeo.utils.DeviceUtils


class MainActivity : BaseActivity() {
    companion object {
        const val TAG = "ALTEKActivity"
    }

    // ---------------------------------------------------------------------------------------------

    private lateinit var binding: ActivityMainBinding

    private var qrCodeLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract(),
        object : ActivityResultCallback<ScanIntentResult> {
            override fun onActivityResult(result: ScanIntentResult) {
                if (result != null) {
                    setContent(result.contents)
                }
            }
        })

    // ---------------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAppbar()

        initView()
        listener()

        handleTextFromAnotherApp()
    }

    private fun handleTextFromAnotherApp() {
        val receivedIntent = intent
        if ("text/plain" == receivedIntent?.type) {
            setContent(
                receivedIntent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            )
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // ---------------------------------------------------------------------------------------------

    private fun setUpAppbar() {
        setSupportActionBar(binding.tbToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun initView() {
        binding.edtContent.let {
            it.movementMethod = ScrollingMovementMethod()
            it.setScroller(Scroller(this))
            it.isVerticalScrollBarEnabled = true
            hideSoftKeyboardClickOutSide(this, it)
        }
    }

    private fun listener() {
        binding.appbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            when (verticalOffset) {
                0 -> {
                    changeColorStatusBar(true)
                }

                -appBarLayout.totalScrollRange -> {
                    changeColorStatusBar(false)
                }

                else -> {
                }
            }
        }

        binding.fabInfo.setOnClickListener {
            showBottomSheetDialog("DEVICE INFORMATION", DeviceUtils.getDeviceInfoToLog())
        }

        binding.ivInfo.setOnClickListener {
            showBottomSheetDialog(
                binding.tvTitleBox.text.toString(),
                "Link Web, Deeplink, ..."
            )
        }

        binding.ivClear.setOnClickListener {
            binding.edtContent.text.clear()
        }

        binding.btnConfirm.setOnClickListener {
            if (isValidText()) {
                handleConfirm()
            }
        }

        binding.btnCopy.setOnClickListener {
            if (isValidText()) {
                copyText(getContent())
            }
        }

        binding.btnShare.setOnClickListener {
            if (isValidText()) {
                shareText(getContent())
            }
        }

        binding.btnQRCode.setOnClickListener {
            if (isValidText()) {
                showQRCodeDialog(getContent())
            }
        }

        binding.btnScanQR.setOnClickListener {
            handleScanQR()
        }
    }

    @SuppressLint("CheckResult")
    private fun handleScanQR() {
        TedPermission.create()
//            .setRationaleTitle("Permission")
//            .setRationaleMessage("We need permission for use your Camera")
            .setPermissions(
                Manifest.permission.CAMERA
            )
            .request()
            .subscribe({ tedPermissionResult ->
                if (tedPermissionResult.isGranted) {
                    showQRCodeScreen()
                } else {
                    Log.d(
                        TAG,
                        """Permission Denied${tedPermissionResult.deniedPermissions}""".trimIndent()
                    )
                }
            }) { throwable ->
                throwable.printStackTrace()
            }
    }

    private fun showQRCodeScreen() {
        val scanOptions = ScanOptions()
        scanOptions.let {
            it.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            it.setPrompt("Scan QR")
            it.setCameraId(0)
            it.setBeepEnabled(false)
            it.setBarcodeImageEnabled(true)
            it.setOrientationLocked(false)
        }
        qrCodeLauncher.launch(scanOptions)
    }

    private fun handleConfirm() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getContent())))
        } catch (e: Exception) {
            showToast(e.message)
            e.printStackTrace()
        }
    }

    private fun getContent(): String {
        return binding.edtContent.text.toString().trim()
    }

    private fun setContent(content: String?) {
        binding.edtContent.setText(content ?: "")
    }

    private fun isValidText(): Boolean {
        return !TextUtils.isEmpty(getContent())
    }

    // ---------------------------------------------------------------------------------------------

    private fun changeColorStatusBar(isExpand: Boolean) {
        if (isExpand) {
            changeStatusBar(ContextCompat.getColor(this, R.color.white))
        } else {
            changeStatusBar(ContextCompat.getColor(this, R.color.color_developer_gradient_1))
        }
    }
}