package com.goforer.advancedapparchitecture.presentation.ui

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.goforer.advancedapparchitecture.R
import com.goforer.advancedapparchitecture.data.source.network.response.Resource
import com.goforer.advancedapparchitecture.di.Injectable
import com.goforer.base.extension.gone
import com.goforer.base.extension.hide
import com.goforer.base.extension.show
import com.goforer.base.extension.upShow
import com.goforer.base.storage.Storage
import com.goforer.base.utils.ConnectionUtils
import com.goforer.base.utils.keyboard.BaseKeyboardObserver
import com.goforer.base.view.dialog.NormalDialog
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

abstract class BaseFragment<T : ViewBinding> : Fragment(), Injectable {
    private var _binding: T? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T

    internal val binding
        get() = _binding as T

    internal var isLoading = false

    internal lateinit var mainActivity: MainActivity

    private lateinit var context: Context

    private var errorDialogMsg = ""

    private var customTabsServiceConnection: CustomTabsServiceConnection? = null

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var isFromBackStack = mutableMapOf<String, Boolean>()

    // Temp Block
    //private lateinit var listener: PermissionCallback

    protected open lateinit var navController: NavController

    protected var client: CustomTabsClient? = null
    protected var customTabsSession: CustomTabsSession? = null

    @Inject
    internal lateinit var storage: Storage

    open fun needTransparentToolbar() = false
    open fun needSystemBarTextWhite() = false

    companion object {
        const val FRAGMENT_TAG = "fragment_tag"
        const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createCustomTabsServiceConnection()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = _binding ?: bindingInflater.invoke(inflater, container, false)
        mainActivity = (activity as MainActivity?)!!
        (activity as MainActivity).supportActionBar?.hide()
        navController =
            (mainActivity.supportFragmentManager.fragments.first() as NavHostFragment).navController

        return requireNotNull(_binding).root
    }

    override fun onResume() {
        super.onResume()

        mainActivity.addKeyboardListener()
        //setDarkMode(localStorage.enabledDarkMode)
    }

    override fun onPause() {
        mainActivity.removeKeyboardListener()
        hideKeyboard()

        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        customTabsServiceConnection?.let {
            mainActivity.unbindService(customTabsServiceConnection!!)
            customTabsServiceConnection = null
            client = null
            customTabsSession = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.context = context
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDetach() {
        super.onDetach()

        if (::onBackPressedCallback.isInitialized) {
            onBackPressedCallback.isEnabled = false
            onBackPressedCallback.remove()
        }
    }

    override fun getContext() = context

    open suspend fun doOnFromBackground() {
    }

    protected open fun checkSession() {
    }

    protected open fun handleBackPressed() {
    }

    protected fun isNavControllerInitialized() = ::navController.isInitialized

    protected fun setChangeViewOnKeyboard(
        originalView: View? = null,
        changedView: View,
        onKeyboardOpen: () -> Unit = {},
        onKeyboardClose: () -> Unit = {}
    ) {
        if (activity is MainActivity) {
            mainActivity.onKeyboardChange = {
                when (it) {
                    BaseKeyboardObserver.KEYBOARD_INIT -> {
                        originalView?.show()
                        if (originalView == null)
                            changedView.show()
                        else
                            changedView.gone()
                    }
                    BaseKeyboardObserver.KEYBOARD_OPEN -> {
                        originalView?.gone()
                        changedView.upShow()
                        onKeyboardOpen()
                    }
                    BaseKeyboardObserver.KEYBOARD_CLOSE -> {
                        changedView.gone()
                        originalView?.upShow()
                        onKeyboardClose()
                    }
                }
            }
        } else {
            mainActivity.onKeyboardChange = {
                when (it) {
                    BaseKeyboardObserver.KEYBOARD_INIT -> {
                        originalView?.show()
                        if (originalView == null)
                            changedView.show()
                        else
                            changedView.gone()
                    }
                    BaseKeyboardObserver.KEYBOARD_OPEN -> {
                        originalView?.gone()
                        changedView.upShow()
                        onKeyboardOpen()
                    }
                    BaseKeyboardObserver.KEYBOARD_CLOSE -> {
                        changedView.gone()
                        originalView?.upShow()
                        onKeyboardClose()
                    }
                }
            }
        }
    }

    protected fun setDarkMode(state: Boolean) {
        /*
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

         */

        if (state) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            resources.configuration.uiMode = Configuration.UI_MODE_NIGHT_YES
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            resources.configuration.uiMode = Configuration.UI_MODE_NIGHT_NO
        }
    }

    protected fun createCustomTabsServiceConnection() {
        customTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
                //Pre-warming
                client = customTabsClient
                client?.warmup(0L)
                customTabsSession = client?.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                client = null
            }
        }

        CustomTabsClient.bindCustomTabsService(
            this.context,
            CUSTOM_TAB_PACKAGE_NAME,
            customTabsServiceConnection!!
        )
    }

    protected fun loadContent(url: String, color: Int = Color.TRANSPARENT) {
        if (Patterns.WEB_URL.matcher(url).matches()) {
            val params = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .build()

            val colorScheme = if (storage.enabledDarkMode)
                CustomTabsIntent.COLOR_SCHEME_DARK
            else
                CustomTabsIntent.COLOR_SCHEME_LIGHT
            val customTabsIntent = CustomTabsIntent.Builder()
                .setColorSchemeParams(colorScheme, params)
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build()

            customTabsIntent.launchUrl(context, ConnectionUtils.getUri(url))
        }
    }

    protected fun shareNews(
        startActivityIntent: ActivityResultLauncher<Intent>,
        title: String,
        articleUrl: String
    ) {
        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_TEXT, articleUrl)
        }, null)

        startActivityIntent.launch(share)
    }

    protected fun handleErrorPopup(resource: Resource) {
        if (ConnectionUtils.isNetworkAvailable(context))
            showErrorPopup(resource.getMessage()!!) {}
    }

    protected fun showSnackBar(text: String, anchorView: View) {
        val snackbar = Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        textView.gravity = Gravity.START
        textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        textView.setTextColor(context.getColor(R.color.white))
        snackbar.view.background =
            AppCompatResources.getDrawable(context, R.drawable.shape_rounded2)
        snackbar.anchorView = anchorView
        snackbar.show()
    }

    internal fun hideKeyboard() {
        mainActivity.hideKeyboard()
    }

    internal fun hideKeyboardAndResetFocus(layout: ViewGroup) {
        hideKeyboard()
        layout.requestFocus()
    }

    internal fun setNewsEmpty(view: View, containerView: ConstraintLayout, isNewsEmpty: Boolean) {
        if (isNewsEmpty) {
            view.hide()
            containerView.show()
        } else {
            view.show()
            containerView.hide()
        }
    }

    internal suspend fun onFromBackground() {
        doOnFromBackground()
    }

    internal fun showErrorPopup(event: String, onDismiss: () -> Unit) {
        if (errorDialogMsg != event) {
            errorDialogMsg = event
            lifecycleScope.launchWhenResumed {
                showDefaultDialog(event, null) {
                    onDismiss()
                    errorDialogMsg = ""
                }
            }
        }
    }

    private fun showDefaultDialog(
        message: CharSequence, title: CharSequence? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        hideKeyboard()

        val builder = NormalDialog.Builder()

        builder.setContext(mainActivity)
        builder.setHorizontalMode(true)
        title?.let { builder.setTitle(title) }
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { _, _ ->
        }

        builder.setOnDismissListener(
            DialogInterface.OnDismissListener {
                onDismiss?.let { it1 -> it1() }
            }
        )

        if (!mainActivity.supportFragmentManager.isDestroyed)
            builder.show(mainActivity.supportFragmentManager)
    }
}