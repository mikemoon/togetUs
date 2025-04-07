package sky.kr.co.newtogetusa.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecoration(
    private val context: Context,
    private val dividerDrawable: Drawable?
) : RecyclerView.ItemDecoration() {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (dividerDrawable == null) return

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        // 마지막 아이템을 제외하고 divider 그리기
        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val isLastItem = position == parent.adapter?.itemCount?.minus(1)

            if (!isLastItem) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + params.bottomMargin
                val bottom = top + dividerDrawable.intrinsicHeight

                dividerDrawable.setBounds(left, top, right, bottom)
                dividerDrawable.draw(c)
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val isLastItem = position == parent.adapter?.itemCount?.minus(1)

        if (!isLastItem) {
            outRect.bottom = dividerDrawable?.intrinsicHeight ?: 0
        }
    }
}