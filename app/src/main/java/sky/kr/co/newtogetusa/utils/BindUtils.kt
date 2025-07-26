package sky.kr.co.newtogetusa.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
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

    
}