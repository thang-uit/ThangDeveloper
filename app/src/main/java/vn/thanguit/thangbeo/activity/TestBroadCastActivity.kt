package vn.thanguit.thangbeo.activity

import android.content.Intent
import android.os.Bundle
import vn.thanguit.thangbeo.base.BaseActivity
import vn.thanguit.thangbeo.databinding.ActivityTestBroadCastBinding


class TestBroadCastActivity : BaseActivity() {

    companion object {
        const val TAG = "TestBroadCastActivity"
    }

    private lateinit var binding: ActivityTestBroadCastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBroadCastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendBC.setOnClickListener {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            intent.action = "test.Broadcast"
            intent.putExtra(TAG, "Em dep lam")
            sendBroadcast(intent)
        }
    }
}