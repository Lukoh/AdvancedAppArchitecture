package com.goforer.advancedapparchitecture.di.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.goforer.advancedapparchitecture.AdvancedApp
import com.goforer.advancedapparchitecture.di.Injectable
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber

/**
 * Helper class to automatically inject fragments if they implement [Injectable].
 */
object AppInjector {
    fun init(advancedApp: AdvancedApp) {
        DaggerAppComponent.factory().create(advancedApp).inject(advancedApp)
        advancedApp.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            var activityReferences = 0
            var isActivityChangingConfigurations = true

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                handleActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                if (advancedApp.isInForeground) {
                    advancedApp.isInForeground = false
                }

                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                advancedApp.activity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
                advancedApp.activity = null
            }

            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    Timber.d("[TEST] onActivityStopped ${activity::class.simpleName}")
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    private fun handleActivity(activity: Activity) {
        if (activity is HasAndroidInjector) {
            AndroidInjection.inject(activity)
        }

        (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentCreated(
                    fm: FragmentManager,
                    fragment: Fragment,
                    savedInstanceState: Bundle?
                ) {
                    if (fragment is Injectable) {
                        AndroidSupportInjection.inject(fragment)
                    }
                }
            }, true
        )
    }
}