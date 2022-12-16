package com.goforer.advancedapparchitecture.di.component

import android.app.Application
import com.goforer.advancedapparchitecture.AdvancedApp
import com.goforer.advancedapparchitecture.di.module.AppModule
import com.goforer.advancedapparchitecture.di.module.activity.MainActivityModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class, AppModule::class, MainActivityModule::class
    ]
)

interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): AppComponent
    }

    fun inject(advancedApp: AdvancedApp)
}