package vn.thanguit.thangbeo.dialog

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import vn.thanguit.thangbeo.base.BaseActivity
import vn.thanguit.thangbeo.databinding.DialogQrCodeBinding
import vn.thanguit.thangbeo.utils.generateQRCode

class QRCodeDialog(
    private var activity: BaseActivity,
    private val qrCode: String?
) : Dialog(activity) {
    private var bitMap: Bitmap? = null
    private lateinit var binding: DialogQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configDialog()

        initView()
        listener()
    }

    private fun configDialog() {
        window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
//            setBackgroundDrawableResource(R.color.transparent)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }

    private fun initView() {
        bitMap = generateQRCode(qrCode)
        if (bitMap != null) {
            binding.ivQRCode.setImageBitmap(bitMap)
        }
    }

    private fun listener() {
        binding.ivCancel.setOnClickListener {
            dismiss()
        }

        binding.ivShare.setOnClickListener {
            if (activity != null && bitMap != null) {
                activity.shareImageBitmap(bitMap)
            }
        }
    }
}