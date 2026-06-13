package sky.kr.co.newtogetusa.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.ui.main.history.HistoryViewModel
import timber.log.Timber

object BindingUtils {

    @JvmStatic
    @BindingAdapter(value = ["src", "placeholderImage"], requireAll = false)
    fun loadImage(imageView: ImageView, src: Int?, placeholderImage: Drawable?) {
        if(placeholderImage != null){
            Glide.with(imageView.context).load(src).placeholder(placeholderImage).into(imageView)
        }else{
            Glide.with(imageView.context).load(src).into(imageView)
        }
    }

    @SuppressLint("CheckResult")
    @JvmStatic
    @BindingAdapter(value = ["loadProfile", "loadProfileNoCache"], requireAll = false)
    fun setProfile(imageView: ImageView, src: String?, noCache: Boolean? = false) {
        if (src.isEmptyProfileSrc()) {
            Glide.with(imageView.context).clear(imageView)
            imageView.setImageResource(R.drawable.profile)
            return
        }

        val radiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 40f, imageView.resources.displayMetrics
        ).toInt()

        val options = RequestOptions()
            .transform(CenterCrop(), RoundedCorners(radiusPx))

        val req = Glide.with(imageView.context)
            .load(src)
            .apply(options)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)

        if (noCache == true) {
            // 이번 로딩만 캐시 미사용
            req.skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        } else {
            // 변경 시에만 캐시 무효화를 원하면, 버전 키를 signature로
            // (updatedAt, revision 등 있으면 여기 넣으세요)
            // req.signature(ObjectKey(yourVersionKey))
        }

        req.into(imageView)
    }

    @JvmStatic
    @BindingAdapter("imageUri", "visibleWhenUri", requireAll = false)
    fun bindImageUri(view: ImageView, uri: Uri?, visibleWhenUri: Boolean?) {
        if (uri != null) {
            view.visibility = View.VISIBLE
            view.setImageURI(uri)
        } else {
            // visibleWhenUri 를 명시적으로 true 로 설정하면 GONE 하지 않음
            if (visibleWhenUri == true) {
                view.visibility = View.VISIBLE
                view.setImageURI(null)
            } else {
                view.visibility = View.GONE
            }
        }
    }

    @JvmStatic
    @SuppressLint("ClickableViewAccessibility")
    @BindingAdapter("drawableEndClickListener")
    fun setDrawableEndClickListener(editText: EditText, clickListener: (() -> Unit)?) {
        if (clickListener == null) {
            editText.setOnTouchListener(null)
            return
        }

        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val drawableStart = editText.right - editText.paddingEnd - drawableEnd.intrinsicWidth
                    if (event.rawX >= drawableStart) {
                        clickListener.invoke()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    @JvmStatic
    @BindingAdapter("setDrawableEndViewClickListener")
    fun setDrawableEndViewClickListener(editText: EditText, clickListener: View.OnClickListener?) {
        if (clickListener == null) {
            editText.setOnTouchListener(null)
            return
        }

        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val bounds = drawableEnd.bounds
                    val drawableWidth = bounds.width()

                    val touchX = event.x.toInt()
                    val width = editText.width
                    val paddingEnd = editText.paddingEnd

                    // EditText의 오른쪽 drawable 영역 터치했는지 확인
                    if (touchX >= width - paddingEnd - drawableWidth) {
                        clickListener.onClick(editText)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    @JvmStatic
    @BindingAdapter("drawableEndCompat")
    fun setDrawableEndCompat(editText: EditText, drawable: Drawable?) {
        val drawables = editText.compoundDrawablesRelative
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawables[0], // start
            drawables[1], // top
            drawable,     // end
            drawables[3]  // bottom
        )
    }

    @JvmStatic
    @BindingAdapter("lottieByPlayerMode")
    fun LottieAnimationView.setLottieByPlayerMode(isPlayerMode: Boolean?) {
        val resId = if (isPlayerMode == true) R.raw.mode_user else R.raw.mode_player
        setAnimation(resId); playAnimation()
    }

    @JvmStatic
    @BindingAdapter("deliveryStatusBadge")
    fun setDeliveryStatusBadge(textView: TextView, statusCd: String?) {
        DeliveryStatusBadgeUtil.apply(textView, statusCd)
    }

    
    private fun String?.isEmptyProfileSrc(): Boolean {
        val value = this?.trim().orEmpty()
        return value.isBlank() ||
                value.equals("null", ignoreCase = true) ||
                value.equals("undefined", ignoreCase = true) ||
                value.equals("none", ignoreCase = true) ||
                value == "-"
    }
}
