package com.goforer.base.utils.keyboard

import android.app.Activity

class KeyboardObserver(activity: Activity) : BaseKeyboardObserver(activity) {
    fun addListener(action: (Int) -> Unit) {
        addListener(object : OnKeyboardListener {
            override fun onKeyboardChange(status: Int) {
                action(status)
            }
        })
    }
}