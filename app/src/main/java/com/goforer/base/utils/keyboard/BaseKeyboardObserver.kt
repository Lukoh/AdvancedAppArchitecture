package com.goforer.base.utils.keyboard

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import timber.log.Timber

abstract class BaseKeyboardObserver(private val activity: Activity) {
    private lateinit var onKeyboardListener: OnKeyboardListener

    private var originalWindowHeight: Int = 0

    private val onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener { onGlobalLayout() }

    private fun setOriginalWindowHeight() {
        val r = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(r)
        originalWindowHeight = r.bottom
    }

    private fun getWindowHeight(): Int {
        val r = Rect()
        activity.findViewById<View>(android.R.id.content).rootView.getWindowVisibleDisplayFrame(r)
        return r.bottom
    }

    protected fun addListener(onKeyboardListener: OnKeyboardListener) {
        setOriginalWindowHeight()

        this.onKeyboardListener = onKeyboardListener
        activity.findViewById<View>(android.R.id.content).rootView.viewTreeObserver.addOnGlobalLayoutListener(
            onGlobalLayoutListener
        )
        onKeyboardListener.onKeyboardChange(KEYBOARD_INIT)
    }

    fun removeListener() {
        activity.findViewById<View>(android.R.id.content).rootView.viewTreeObserver.removeOnGlobalLayoutListener(
            onGlobalLayoutListener
        )
    }

    private var isShown: Boolean? = null
    private fun onGlobalLayout() {
        runCatching {
            val windowHeight = getWindowHeight()
            if (originalWindowHeight < windowHeight)
                originalWindowHeight = windowHeight

            val show = originalWindowHeight > windowHeight

            if (isShown == null)
                onKeyboardListener.onKeyboardChange(KEYBOARD_INIT)
            else if (isShown != show)
                onKeyboardListener.onKeyboardChange(if (show) KEYBOARD_OPEN else KEYBOARD_CLOSE)

            isShown = show
        }.onFailure { e ->
            Timber.e(e.toString())
        }
    }

    interface OnKeyboardListener {
        fun onKeyboardChange(status: Int)
    }

    companion object {
        const val KEYBOARD_INIT = 0
        const val KEYBOARD_OPEN = 1
        const val KEYBOARD_CLOSE = 2
    }
}