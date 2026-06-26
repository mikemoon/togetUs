package sky.kr.co.newtogetusa.ui.main.history

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val workingMatrix = Matrix()
    private val displayRect = RectF()
    private val matrixValues = FloatArray(9)

    private var minScale = 1f
    private var maxScale = 4f
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            parent?.requestDisallowInterceptTouchEvent(true)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val current = currentScale()
            val target = (current * detector.scaleFactor).coerceIn(minScale, maxScale)
            val factor = target / current
            workingMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
            fixTranslation()
            imageMatrix = workingMatrix
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(event: MotionEvent): Boolean {
            val current = currentScale()
            val target = if (current > minScale + SCALE_EPSILON) minScale else min(minScale * DOUBLE_TAP_SCALE, maxScale)
            val factor = target / current
            workingMatrix.postScale(factor, factor, event.x, event.y)
            fixTranslation()
            imageMatrix = workingMatrix
            parent?.requestDisallowInterceptTouchEvent(target > minScale + SCALE_EPSILON)
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
        isClickable = true
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        post { resetToFitCenter() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { resetToFitCenter() }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(isZoomed())
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && isZoomed()) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    workingMatrix.postTranslate(dx, dy)
                    fixTranslation()
                    imageMatrix = workingMatrix
                    isDragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isZoomed()) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                isDragging = false
            }
        }

        return true
    }

    private fun resetToFitCenter() {
        val drawable = drawable ?: return
        if (width == 0 || height == 0 || drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) return

        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val scale = min(viewWidth / drawableWidth, viewHeight / drawableHeight)

        minScale = scale
        maxScale = maxOf(3f, minScale * 4f)

        val dx = (viewWidth - drawableWidth * scale) / 2f
        val dy = (viewHeight - drawableHeight * scale) / 2f

        workingMatrix.reset()
        workingMatrix.postScale(scale, scale)
        workingMatrix.postTranslate(dx, dy)
        imageMatrix = workingMatrix
    }

    private fun fixTranslation() {
        val rect = currentDisplayRect() ?: return
        var deltaX = 0f
        var deltaY = 0f

        if (rect.width() <= width) {
            deltaX = (width - rect.width()) / 2f - rect.left
        } else {
            if (rect.left > 0f) deltaX = -rect.left
            if (rect.right < width) deltaX = width - rect.right
        }

        if (rect.height() <= height) {
            deltaY = (height - rect.height()) / 2f - rect.top
        } else {
            if (rect.top > 0f) deltaY = -rect.top
            if (rect.bottom < height) deltaY = height - rect.bottom
        }

        workingMatrix.postTranslate(deltaX, deltaY)
    }

    private fun currentDisplayRect(): RectF? {
        val drawable = drawable ?: return null
        displayRect.set(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        workingMatrix.mapRect(displayRect)
        return displayRect
    }

    private fun currentScale(): Float {
        workingMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun isZoomed(): Boolean = currentScale() > minScale + SCALE_EPSILON

    companion object {
        private const val DOUBLE_TAP_SCALE = 2.5f
        private const val SCALE_EPSILON = 0.01f
    }
}
