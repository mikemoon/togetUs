package sky.kr.co.newtogetusa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun dialogFragmentShow(fm: FragmentManager, fragment: Fragment) {
    fm.beginTransaction().add(fragment, "").commitAllowingStateLoss()
}

@SuppressLint("CheckResult")
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes error: Int? = null,
    roundedCorner : Int? = null,
    crossFade: Boolean = false,
    isCircle: Boolean = false
) {
    Glide.with(context)
        .load(url)
        .apply {
            roundedCorner?.let {
                transform(RoundedCorners(roundedCorner))
            }
            placeholder?.let { placeholderId ->
                placeholder(placeholderId)
            }
            error?.let { errorId ->
                error(errorId)
            }
            if(isCircle){
                circleCrop()
            }
            if (crossFade) {
                transition(DrawableTransitionOptions.withCrossFade())
            }
        }
        .into(this)
}