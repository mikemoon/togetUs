package sky.kr.co.newtogetusa.ui.main.delivery.product

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        outRect.right = space

        // 첫 번째 아이템에는 왼쪽 마진도 추가
        if (position == 0) {
            outRect.left = space
        }
    }
}