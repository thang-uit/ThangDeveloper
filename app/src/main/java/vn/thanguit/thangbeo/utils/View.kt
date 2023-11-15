package vn.thanguit.thangbeo.utils

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun View.cancelTransition() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        transitionName = null
    }
}

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.GONE
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

fun ImageView.loadFromUrl(url: String?) =
    Glide.with(this.context.applicationContext)
        .load(url ?: "")
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)

fun ImageView.loadImage(redId: Int) =
    Glide.with(this.context.applicationContext).load(redId).into(this)

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.absX(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[0]
}

fun View.absY(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[1]
}

/*
    https://stackoverflow.com/a/57799999
 */
fun Drawable.overrideColor(context: Context, colorInt: Int) {
    when (val muted = this.mutate()) {
        is GradientDrawable -> muted.setColor(ContextCompat.getColor(context, colorInt))
        is ShapeDrawable -> muted.paint.color = ContextCompat.getColor(context, colorInt)
        is ColorDrawable -> muted.color = ContextCompat.getColor(context, colorInt)
        else -> Log.d("Drawable", "Not a valid background type")
    }
}

fun setColorDrawable(context: Context, view: View?, color: Int) {
    try {
        view?.background?.overrideColor(context, color)
    } catch (e: Exception) {
        e.stackTrace
    }
}

fun View.startAnimShake() {
    val shake = TranslateAnimation(0F, 10F, 0F, 0F)
    shake.duration = 500
    shake.interpolator = CycleInterpolator(7F)
    return startAnimation(shake)
}