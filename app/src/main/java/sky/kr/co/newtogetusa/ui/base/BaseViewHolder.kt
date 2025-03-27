package sky.kr.co.newtogetusa.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun onBindViewHolder(data: Any?) {}
    open fun onBindViewHolder(data: Any?, position: Int) {}
}