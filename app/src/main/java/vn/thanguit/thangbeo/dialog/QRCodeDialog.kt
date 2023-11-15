package vn.thanguit.thangbeo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import vn.thanguit.thangbeo.databinding.DialogQrCodeBinding

class QRCodeDialog(
    context: Context,
    val qrCode: String?
) : Dialog(context) {
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
    }

    private fun initView() {
//        binding.ivQRCode.setImageBitmap()
    }

    private fun listener() {
    }
}