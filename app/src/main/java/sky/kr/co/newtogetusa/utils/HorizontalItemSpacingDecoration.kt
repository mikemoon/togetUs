package sky.kr.co.newtogetusa.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalItemSpacingDecoration(private val spacing: Int, private val showFirst:Boolean = false, private val showLast:Boolean= true) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // 첫 번째 아이템에는 왼쪽 간격 추가
        if (position == 0 && showFirst) {
            outRect.left = spacing
        }

        // 마지막 아이템에는 오른쪽 간격 추가
        if (position == itemCount - 1 && showLast) {
            outRect.right = spacing
        }

        // 아이템 사이 간격
        outRect.right = spacing
    }
}