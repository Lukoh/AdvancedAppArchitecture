package com.goforer.advancedapparchitecture.di.module.activity

import com.goforer.advancedapparchitecture.di.module.fragment.home.HomeFragmentModule
import com.goforer.advancedapparchitecture.presentation.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(
        modules = [
            HomeFragmentModule::class
        ]
    )

    abstract fun contributeMainActivity(): MainActivity
}