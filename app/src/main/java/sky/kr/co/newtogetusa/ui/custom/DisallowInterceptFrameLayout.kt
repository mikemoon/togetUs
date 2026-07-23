package sky.kr.co.newtogetusa.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * 지도 등 내부 드래그 제스처를 사용하는 뷰를 감싸는 컨테이너.
 * 터치가 시작되면 부모(NestedScrollView 등)의 터치 가로채기를 막아
 * 지도 드래그 시 루트 스크롤이 동작하지 않도록 한다.
 */
class DisallowInterceptFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN ->
                parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                parent?.requestDisallowInterceptTouchEvent(false)
        }
        return super.dispatchTouchEvent(ev)
    }
}
