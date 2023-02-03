package com.goforer.base.extension

import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.amulyakhare.textdrawable.TextDrawable
import com.goforer.fitpettest.R

class SafeClickListener(
    private val interval: Long,
    private inline val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked > interval) {
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun Window.setSystemBarTextDark() {
    val view = findViewById<View>(android.R.id.content)
    val flags = view.systemUiVisibility
    view.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}


fun View.show(): View {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }

    return this
}

/**
 * Hide the view. (visibility = View.INVISIBLE)
 */
fun View.hide(): View {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }

    return this
}

/**
 * Remove the view (visibility = View.GONE)
 */
fun View.gone(): View {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }

    return this
}

fun View.upShow(duration: Long = 500L) {
    show()
    translationY = height.toFloat()
    animate()
        .setDuration(duration)
        .translationY(0f)
        .setListener(object : AnimatorListenerAdapter() {
        })
        .start()
}

fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
    val params = this.layoutParams as ConstraintLayout.LayoutParams

    params.setMargins(
        left,
        top,
        right,
        bottom
    )
    this.layoutParams = params
}

/**
 * Extension method to show a keyboard for View.
 */
fun View.showKeyboard() {
    if (this.requestFocus()) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

/**
 * Try to hide the keyboard and returns whether it worked
 * https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
 */
fun View.hideKeyboard(): Boolean {
    runCatching {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        return inputMethodManager.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }.onFailure { e ->
        e.printStackTrace()
    }

    this.clearFocus()

    return false
}

inline fun View.setSafeOnClickListener(interval: Long = 1200, crossinline onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener(interval = interval) {
        onSafeClick(it)
    }

    setOnClickListener(safeClickListener)
}

fun ImageView.convertTextDrawable(
    text: String,
    textColor: Int,
    height: Int,
    width: Int,
    isBold: Boolean,
    fontSize: Int? = null,
) {
    fontSize.isNull({
        val textDrawable = TextDrawable.builder()
            .beginConfig()
            .useFont(Typeface.create("sans-serif", Typeface.NORMAL))
            .textColor(textColor)
            .width(width)
            .height(height)
            .endConfig()
            .buildRect(text, context.getColor(R.color.colorTransparent))

        this.setImageDrawable(textDrawable)
    }, {
        if (isBold) {
            val textDrawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.create("sans-serif", Typeface.BOLD))
                .fontSize(it)
                .textColor(textColor)
                .width(width)
                .height(height)
                .endConfig()
                .buildRect(text, context.getColor(R.color.colorTransparent))

            this.setImageDrawable(textDrawable)
        } else {
            val textDrawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.create("sans-serif", Typeface.NORMAL))
                .fontSize(it)
                .textColor(textColor)
                .width(width)
                .height(height)
                .endConfig()
                .buildRect(text, context.getColor(R.color.colorTransparent))

            this.setImageDrawable(textDrawable)
        }
    })
}

fun TextView.setSpans(proportion: Float = 1.05F, spanMap: MutableMap<String, ClickableSpan>) {
    val tvt = text.toString()
    val ssb = SpannableStringBuilder(tvt)

    spanMap.forEach {
        val key = it.key
        val value = it.value

        if (tvt.contains(key, true)) {
            val start = tvt.indexOf(key, 0, true)
            val end = start + key.length

            ssb.setSpan(value, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(
                RelativeSizeSpan(proportion),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    setText(ssb, TextView.BufferType.SPANNABLE)
    movementMethod = LinkMovementMethod.getInstance()
}

inline fun EditText.addAfterTextChangedListener(
    filter: InputFilter? = null,
    crossinline onTextChanged: (String) -> Unit
) {
    if (filter != null)
        this.filters = arrayOf(filter)

    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            onTextChanged(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun TextView.setTextUnderline() {
    val content = SpannableString(this.text.toString())

    content.setSpan(UnderlineSpan(), 0, content.length, 0)
    this.text = content
}

inline fun getHighlightSpanMap(
    textToHighlight: String,
    color: Int?,
    typeface: Typeface?,
    isUnderlineText: Boolean,
    bgColor: Int? = null,
    textSize: Float? = null,
    crossinline onClickListener: (textView: View) -> Unit
): MutableMap<String, ClickableSpan> {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(textView: View) {
            onClickListener(textView)
        }

        override fun updateDrawState(textPaint: TextPaint) {
            super.updateDrawState(textPaint)

            bgColor?.let {
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.bgColor = it
            }

            textSize?.let {
                textPaint.textSize = it
            }

            color?.let {
                textPaint.color = it
            }

            typeface?.let {
                textPaint.typeface = it
            }

            textPaint.isUnderlineText = isUnderlineText
        }
    }

    return mutableMapOf(Pair(textToHighlight, clickableSpan))
}

fun Dialog.setDefaultWindowTheme() {
    window?.apply {
        setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        statusBarColor = Color.TRANSPARENT

        setSystemBarTextDark()
        setDimAmount(0.3f)
    }
}