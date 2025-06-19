package sky.kr.co.newtogetusa.ui.main.delivery.product

import android.graphics.Color
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(private val adapter: ProductPickImageAdapter) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        // 롱 프레스로 드래그 시작
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        // 스와이프 비활성화
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 갤러리 버튼(첫 번째 아이템)은 이동 불가
        return if (viewHolder.itemViewType == ProductPickImageAdapter.VIEW_TYPE_GALLERY) {
            makeMovementFlags(0, 0) // 이동 불가
        } else {
            // 가로 방향 드래그로 변경 (LEFT, RIGHT)
            val dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            makeMovementFlags(dragFlags, 0)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // 갤러리 버튼으로/에서 이동은 불가
        if (source.itemViewType == ProductPickImageAdapter.VIEW_TYPE_GALLERY ||
            target.itemViewType == ProductPickImageAdapter.VIEW_TYPE_GALLERY) {
            return false
        }

        // 항목 위치 이동
        adapter.moveItem(source.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프 기능 사용하지 않음
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // 갤러리 뷰는 드래그 효과 적용하지 않음
        if (viewHolder?.itemViewType == ProductPickImageAdapter.VIEW_TYPE_GALLERY) {
            return
        }

        super.onSelectedChanged(viewHolder, actionState)

        // 드래그 중일 때 아이템 시각적 효과 적용
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            viewHolder.itemView.alpha = 0.7f
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        // 갤러리 뷰는 처리하지 않음
        if (viewHolder.itemViewType == ProductPickImageAdapter.VIEW_TYPE_GALLERY) {
            return
        }

        super.clearView(recyclerView, viewHolder)

        // 드래그가 끝났을 때 아이템 원래 상태로 복원
        viewHolder.itemView.alpha = 1.0f
        viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
    }
}