package com.gg.messagebubbleview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Creator : GG
 * Time    : 2018/12/19
 * Mail    : gg.jin.yu@gmail.com
 * Explain :
 */
class BubbleMessageTouchListener(view: View, context: Context, listener: BubbleDisappearListener?) :
        View.OnTouchListener, MessageBubbleView.MessageBubbleListener {

    private val mContext: Context by lazy { context }

    private val mWindowManager: WindowManager by lazy {
        (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).apply {
        }
    }

    private val mView: View by lazy { view }

    private val mBombImageView: ImageView by lazy {
        ImageView(mContext).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        }
    }

    private val mBombView: FrameLayout by lazy {
        FrameLayout(mContext).apply {
            addView(mBombImageView)
        }
    }

    private val mListener: BubbleDisappearListener?  by lazy { listener }

    private val mMessageBubbleView: MessageBubbleView by lazy {
        MessageBubbleView(mContext).apply {
            setMessageBubbleListener(this@BubbleMessageTouchListener)
        }
    }

    private val mParams: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSPARENT
//            type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            // 可在全屏幕布局, 不受状态栏影响
            flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 最初不可获取焦点, 这样不影响底层应用接收触摸事件
        }
    }

    init {
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {


                mWindowManager.addView(mMessageBubbleView, mParams)


                val location = IntArray(2)
                mView.getLocationOnScreen(location)



//                Log.w("location x", "---" + location[0])
//                Log.w("location y", "---" + location[1])
//                Log.w("mView x", "---" + mView.width)
//                Log.w("mView y", "---" + mView.height)
//                Log.w("status bar height", "---" + BubbleUtils.getStatusBarHeight(mContext))
                mView.visibility = View.INVISIBLE

                mMessageBubbleView.initPoint(
                        location[0] + mView.width / 2f,
                        location[1].toFloat() + mView.height / 2f
                )


                val bitmap = getBitmapFromView(mView)
                mMessageBubbleView.setDragBitmap(bitmap)
            }

            MotionEvent.ACTION_MOVE -> {
                mMessageBubbleView.updateDragPoint(event.rawX, event.rawY)
            }
            MotionEvent.ACTION_UP -> {
                mMessageBubbleView.handleActionUp()
            }
        }

        return true
    }

    override fun dismiss(pointF: PointF) {
        mWindowManager.removeView(mMessageBubbleView)
        mWindowManager.addView(mBombView, mParams)
        mBombImageView.setBackgroundResource(R.drawable.anim_bubble_pop)
        val drawable: AnimationDrawable = mBombImageView.background as AnimationDrawable
        mBombImageView.x = pointF.x - drawable.intrinsicWidth / 2
        mBombImageView.y = pointF.y - drawable.intrinsicHeight / 2
        drawable.start()

        mBombImageView.postDelayed({
            mWindowManager.removeView(mBombView)
            if (mListener != null) {
                mListener?.dismiss(mView)
            }
        }, getAnimationDrawableTime(drawable))
    }

    private fun getAnimationDrawableTime(drawable: AnimationDrawable): Long {
        val count = drawable.numberOfFrames
        var time = 0L
        for (i in 0..count) {
            time += drawable.getDuration(i)
        }
        return time
    }

    override fun restore() {
        mWindowManager.removeView(mMessageBubbleView)
        mView.visibility = View.VISIBLE
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_4444)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }


    interface BubbleDisappearListener {
        fun dismiss(view: View)
    }


}