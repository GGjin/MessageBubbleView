package com.gg.messagebubbleview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.util.Log
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
            flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 最初不可获取焦点, 这样不影响底层应用接收触摸事件
        }
    }

    private var mIsTouch = false

    private var offsetX = 0f
    private var offsetY = 0f

    private val postDelayedTime = 50L

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val location = IntArray(2)
        mView.getLocationOnScreen(location)
        val bitmap = getBitmapFromView(mView)

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {


                mWindowManager.addView(mMessageBubbleView, mParams)


//                Log.w("location x", "---" + location[0])
//                Log.w("location y", "---" + location[1])
//                Log.w("mView.width", "---" + mView.width)
//                Log.w("mView.height", "---" + mView.height)
//                Log.w("mView.width", "---" + mView.measuredWidth)
//                Log.w("mView.height", "---" + mView.measuredHeight)
//                Log.w("event x", "---" + event.rawX)
//                Log.w("event y", "---" + event.rawY)
//                Log.w("status bar height", "---" + BubbleUtils.getStatusBarHeight(mContext))

                mIsTouch = true

                //初始中心点
                val initX = location[0] + mView.width / 2f
                val initY = location[1] + mView.height / 2f

                //偏移量
                offsetX = event.rawX - initX
                offsetY = event.rawY - initY

                mMessageBubbleView.initPoint(initX, initY)


                mMessageBubbleView.setDragBitmap(bitmap)


                mView.postDelayed({

                    mView.visibility = View.INVISIBLE

                }, postDelayedTime)


            }

            MotionEvent.ACTION_MOVE -> {
                mMessageBubbleView.updateDragPoint(event.rawX - offsetX, event.rawY - offsetY)

            }
            MotionEvent.ACTION_UP -> {
                mIsTouch = false
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
        mView.visibility = View.VISIBLE
        mView.postDelayed({

            mWindowManager.removeView(mMessageBubbleView)
        }, postDelayedTime)
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