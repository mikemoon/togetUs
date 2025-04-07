package sky.kr.co.newtogetusa.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

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
}