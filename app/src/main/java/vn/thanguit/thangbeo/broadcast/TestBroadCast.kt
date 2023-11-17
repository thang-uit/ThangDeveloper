package vn.thanguit.thangbeo.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TestBroadCast(
    private var iActionTestBroadCast: IActionTestBroadCast?
) : BroadcastReceiver() {
    companion object {
        const val TAG = "TestBroadCast"
    }

    interface IActionTestBroadCast {
        fun onReceiveTestBroadCast(context: Context?, intent: Intent?)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: " + TestBroadCast::class.java.name)

        iActionTestBroadCast?.onReceiveTestBroadCast(context, intent)

//        context?.let {
//            Toast.makeText(it, intent?.dataString ?: TAG, Toast.LENGTH_LONG);
//        }
    }
}