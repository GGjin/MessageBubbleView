package com.gg.messagebubbleview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MessageBubbleView.attach(hello, object : BubbleMessageTouchListener.BubbleDisappearListener {
            override fun dismiss(view: View) {
                Toast.makeText(this@MainActivity,"hahaha",Toast.LENGTH_LONG).show()
            }

        })
    }
}
