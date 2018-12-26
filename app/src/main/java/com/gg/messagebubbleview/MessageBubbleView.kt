package com.gg.messagebubbleview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.OvershootInterpolator


/**
 * Creator : GG
 * Time    : 2018/12/16
 * Mail    : gg.jin.yu@gmail.com
 * Explain :
 */
class MessageBubbleView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private lateinit var mDragPoint: PointF
    private lateinit var mFixationPoint: PointF

    private val mPaint: Paint  by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.RED
            isDither = true
        }
    }

    private var mDragRadius = dip2sp(10f)

    var mFixationRadius = 0f

    //固定圆初始半径
    private var mFixationRadiusMax = dip2sp(7f)
    //固定圆最小半径
    private var mFixationRadiusMin = dip2sp(3f)

    private lateinit var mDragBitmap: Bitmap

    private var bezierPath: Path? = null


    init {

    }


    private fun dip2sp(dip: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.displayMetrics)


    override fun onDraw(canvas: Canvas?) {
        if (!this::mDragPoint.isInitialized || !this::mFixationPoint.isInitialized)
            return
        //画拖拽圆
        canvas!!.drawCircle(mDragPoint.x, mDragPoint.y, mDragRadius, mPaint)

        //画固定圆


        bezierPath = getBezierPath()
        if (bezierPath != null) {
            canvas.drawCircle(mFixationPoint.x, mFixationPoint.y, mFixationRadius, mPaint)
            canvas.drawPath(bezierPath, mPaint)
        }
        if (this::mDragBitmap.isInitialized) {
            canvas.drawBitmap(
                mDragBitmap,
                mDragPoint.x - mDragBitmap.width / 2,
                mDragPoint.y - mDragBitmap.height / 2,
                null
            )
        }

    }

    private fun getBezierPath(): Path? {
        val distance = getDistance(mDragPoint, mFixationPoint)
        mFixationRadius = (mFixationRadiusMax - distance / 30).toFloat()
        if (mFixationRadius < mFixationRadiusMin) {
            return null
        }

        return Path().apply {
            val dy = mDragPoint.y - mFixationPoint.y
            val dx = mDragPoint.x - mFixationPoint.x
            val tanA: Double = (dy / dx).toDouble()
            val arcTanA: Double = Math.atan(tanA)


            // p0
            val p0x = (mFixationPoint.x + mFixationRadius * Math.sin(arcTanA)).toFloat()
            val p0y = (mFixationPoint.y - mFixationRadius * Math.cos(arcTanA)).toFloat()

            // p1
            val p1x = (mDragPoint.x + mDragRadius * Math.sin(arcTanA)).toFloat()
            val p1y = (mDragPoint.y - mDragRadius * Math.cos(arcTanA)).toFloat()

            // p2
            val p2x = (mDragPoint.x - mDragRadius * Math.sin(arcTanA)).toFloat()
            val p2y = (mDragPoint.y + mDragRadius * Math.cos(arcTanA)).toFloat()

            // p3
            val p3x = (mFixationPoint.x - mFixationRadius * Math.sin(arcTanA)).toFloat()
            val p3y = (mFixationPoint.y + mFixationRadius * Math.cos(arcTanA)).toFloat()


            moveTo(p0x, p0y)
            val pointF = getControlPointF()
            quadTo(pointF.x, pointF.y, p1x, p1y)
            lineTo(p2x, p2y)
            quadTo(pointF.x, pointF.y, p3x, p3y)
            close()
        }
    }

    private fun getControlPointF(): PointF =
        PointF((mDragPoint.x + mFixationPoint.x) / 2, (mDragPoint.y + mFixationPoint.y) / 2)

    /**
     * 获取两个圆之间的距离
     *
     * @param point1
     * @param point2
     * @return
     */
    private fun getDistance(point1: PointF, point2: PointF): Double {
        return Math.sqrt(((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y)).toDouble())
    }


//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//
//        when (event!!.action) {
//            MotionEvent.ACTION_DOWN -> {
//                val floatX = event.x
//                val floatY = event.y
//                initPoint(floatX, floatY)
//
//            }
//            MotionEvent.ACTION_MOVE -> {
//                updateDragPoint(event.x, event.y)
//            }
//            MotionEvent.ACTION_UP -> {
//
//            }
//        }
//
//        invalidate()
//        return true
//    }

    /**
     * 刷新拖拽圆的位置
     */
    fun updateDragPoint(x: Float, y: Float) {
        mDragPoint.x = x
        mDragPoint.y = y
        postInvalidate()
    }

    /**
     * 初始化两个圆
     */
    fun initPoint(floatX: Float, floatY: Float) {
        mDragPoint = PointF(floatX, floatY)
        mFixationPoint = PointF(floatX, floatY)
        postInvalidate()
    }

    fun setDragBitmap(bitmap: Bitmap) {
        mDragBitmap = bitmap
        postInvalidate()
    }

    fun handleActionUp() {
        if (mFixationRadius > mFixationRadiusMin) {
            ObjectAnimator.ofFloat(1f).apply {
                duration = 250
                val startPoint = PointF(mDragPoint.x, mDragPoint.y)
                val endPoint = PointF(mFixationPoint.x, mFixationPoint.y)
                addUpdateListener {
                    val percent: Float = it.animatedValue as Float
                    val pointF = BubbleUtils.getPointByPercent(startPoint, endPoint, percent)
                    updateDragPoint(pointF.x, pointF.y)
                }

                interpolator = OvershootInterpolator()

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        if (this@MessageBubbleView::mListener.isInitialized) {
                            mListener.restore()
                        }
                    }
                })

                start()
            }
        } else {
            if (this@MessageBubbleView::mListener.isInitialized) {
                mListener.dismiss(mDragPoint)
            }
        }
    }

    private lateinit var mListener: MessageBubbleListener

    fun setMessageBubbleListener(listener: MessageBubbleListener) {
        mListener = listener
    }

    companion object {
        fun attach(view: View, listener: BubbleMessageTouchListener.BubbleDisappearListener) {
            view.setOnTouchListener(BubbleMessageTouchListener(view, view.context, listener))
        }
    }


    interface MessageBubbleListener {
        fun dismiss(pointF: PointF)

        fun restore()
    }
}