package com.goforer.advancedapparchitecture.presentation.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.goforer.advancedapparchitecture.R
import com.goforer.advancedapparchitecture.databinding.ActivityMainBinding
import com.goforer.base.extension.setSystemBarTextDark
import com.goforer.base.utils.keyboard.KeyboardObserver
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

open class MainActivity : AppCompatActivity(), HasAndroidInjector {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    internal var onKeyboardChange: ((status: Int) -> Unit) = {}

    private lateinit var keyboardObserver: KeyboardObserver

    @Inject
    internal lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        window.setSystemBarTextDark()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_container)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    internal fun addKeyboardListener() {
        if (::keyboardObserver.isInitialized) {
            keyboardObserver.addListener { status ->
                onKeyboardChange(status)
            }
        }
    }

    internal fun removeKeyboardListener() {
        if (::keyboardObserver.isInitialized)
            keyboardObserver.removeListener()
    }

    internal fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        currentFocus?.windowToken?.let {
            inputManager.hideSoftInputFromWindow(it, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun init() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (::binding.isInitialized) {
            with(binding) {
                setContentView(root)
                setSupportActionBar(toolbar)
                supportActionBar?.setDisplayShowTitleEnabled(false)
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_container) as NavHostFragment
                navController = navHostFragment.navController
                appBarConfiguration = AppBarConfiguration(navController.graph)
                setupActionBarWithNavController(navController, appBarConfiguration)
            }
        }
    }
}