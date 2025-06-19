package sky.kr.co.newtogetusa.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
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
}