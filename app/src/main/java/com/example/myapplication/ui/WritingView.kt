package com.example.myapplication.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class WritingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    init {
        isFocusable = false
        isFocusableInTouchMode = false
    }

    enum class ToolType {
        PEN,
        ERASER
    }

    private var currentToolType = ToolType.PEN

    private val penPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        strokeWidth = 12f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val eraserPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 40f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private val path = Path()
    private var currentX = 0f
    private var currentY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // Save old bitmap before creating new one
            val oldBitmap = if (::bitmap.isInitialized) bitmap else null

            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap)

            // Restore old drawing if it existed
            if (oldBitmap != null && oldBitmap.width == w && oldBitmap.height == h) {
                bitmapCanvas.drawBitmap(oldBitmap, 0f, 0f, null)
            }

            oldBitmap?.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (::bitmap.isInitialized) {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
    }

    override fun onHoverEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Don't handle touch events if view is disabled
        if (!isEnabled) {
            return false
        }

        val toolType = event.getToolType(0)
        if (toolType != MotionEvent.TOOL_TYPE_STYLUS && toolType != MotionEvent.TOOL_TYPE_ERASER) {
            return false
        }

        val x = event.x
        val y = event.y

        val isEraser = toolType == MotionEvent.TOOL_TYPE_ERASER || currentToolType == ToolType.ERASER
        val currentPaint = if (isEraser) eraserPaint else penPaint

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentX = x
                currentY = y
                path.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                currentX = x
                currentY = y
                bitmapCanvas.drawPath(path, currentPaint)
                // Mark that drawing has been made (only for pen, not eraser)
                if (!isEraser) {
                    markDrawingStarted()
                }
            }
            MotionEvent.ACTION_UP -> {
                path.reset()
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun setToolType(toolType: ToolType) {
        currentToolType = toolType
    }

    fun setPenWidth(width: Float) {
        penPaint.strokeWidth = width
    }

    fun setEraserWidth(width: Float) {
        eraserPaint.strokeWidth = width
    }

    fun clear() {
        path.reset()
        if (::bitmap.isInitialized) {
            bitmap.eraseColor(Color.TRANSPARENT)
        }
        resetDrawingState()
        invalidate()
    }

    /**
     * Save the current drawing to restore later
     */
    fun saveBitmap(): Bitmap? {
        return if (::bitmap.isInitialized) {
            bitmap.copy(bitmap.config, true)
        } else {
            null
        }
    }

    /**
     * Restore a previously saved bitmap
     */
    fun restoreBitmap(savedBitmap: Bitmap) {
        if (::bitmap.isInitialized && savedBitmap.width == bitmap.width && savedBitmap.height == bitmap.height) {
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(savedBitmap, 0f, 0f, null)
            invalidate()
        }
    }

    // Track if any drawing has been made (more efficient than checking pixels)
    private var hasDrawing = false

    /**
     * Check if the canvas is empty (no drawing)
     * Uses efficient tracking instead of pixel scanning
     */
    fun isEmpty(): Boolean {
        if (!::bitmap.isInitialized) return true
        return !hasDrawing
    }

    /**
     * Mark that drawing has started (called internally during touch)
     */
    private fun markDrawingStarted() {
        hasDrawing = true
    }

    /**
     * Reset drawing state (called when canvas is cleared)
     */
    private fun resetDrawingState() {
        hasDrawing = false
    }
}