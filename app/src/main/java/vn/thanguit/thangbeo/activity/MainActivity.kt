package vn.thanguit.thangbeo.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.widget.Scroller
import androidx.core.content.ContextCompat
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
            binding.edtContent.setText(
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
                showToast("Feature are developing")
            }
        }
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