package vn.thanguit.thangbeo.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import vn.thanguit.thangbeo.R
import vn.thanguit.thangbeo.databinding.BottomSheetDialogDeviceInfoBinding

class DeviceInfoBottomSheetDialog(
    context: Context,
    private var title: String? = "",
    private var content: String? = "",
) : BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme) {

    private lateinit var binding: BottomSheetDialogDeviceInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = BottomSheetDialogDeviceInfoBinding.inflate(layoutInflater)
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
//            setGravity(Gravity.BOTTOM)
//            setBackgroundDrawableResource(R.color.transparent)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun initView() {
        binding.tvTitle.text = title
        binding.tvContent.text = content
    }

    private fun listener() {
        binding.tvContent.setOnLongClickListener {
            true
        }
    }
}