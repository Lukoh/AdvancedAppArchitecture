package com.goforer.advancedapparchitecture.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * EventBus is a central post/subscribe the data system for Kotlin and Android.
 * Stuff are posted to the Fragment, which is subscribed the data as subscribers, and delivers it to subscribers that declare it.
 * To receive the data, subscribers must declare it into Fragment.
 * Once declared, subscribers receive the data
 *
 * Converts a cold Flow into a hot SharedFlow that is started in the given coroutine scope,
 * sharing emissions from a single running instance of the upstream flow with multiple downstream subscribers,
 * and replaying a specified number of replay values to new subscribers.
 *
 * The shareIn operator is useful in situations when there is a cold flow that is expensive to create
 * and/or to maintain, but there are multiple subscribers that need to collect its values.
 *
 */
open class EventBus<Data> : ViewModel() {
    private var sharedData: SharedFlow<Data>? = null
    private var job: Job? = null

    internal fun post(data: Data, disposable: Boolean = true) {
        val replayCount: Int
        val replayExpirationMills: Long

        if (disposable) {
            replayCount = 0
            replayExpirationMills = 0
        } else {
            replayCount = 1
            replayExpirationMills = java.lang.Long.MAX_VALUE
        }

        sharedData = flowOf(data).shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0, replayExpirationMills),
            replay = replayCount
        )
    }

    internal fun post(lifecycle: Lifecycle, data: Data, disposable: Boolean = true) {
        val replayCount: Int
        val replayExpirationMills: Long

        if (disposable) {
            replayCount = 0
            replayExpirationMills = 0
        } else {
            replayCount = 1
            replayExpirationMills = java.lang.Long.MAX_VALUE
        }

        sharedData = flowOf(data).flowWithLifecycle(lifecycle).shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0, replayExpirationMills),
            replay = replayCount
        )
    }

    internal fun subscribe(
        isDisposable: Boolean = false,
        doOnResult: (data: Data) -> Unit
    ) {
        viewModelScope.launch {
            sharedData?.collectLatest {
                doOnResult(it)
                if (isDisposable)
                    sharedData = null
            }
        }
    }

    internal fun subscribe(
        lifecycle: Lifecycle,
        isDisposable: Boolean = false,
        doOnResult: (data: Data) -> Unit
    ) {
        viewModelScope.launch {
            sharedData?.flowWithLifecycle(lifecycle)?.collectLatest {
                doOnResult(it)
                if (isDisposable)
                    sharedData = null
            }
        }
    }

    internal fun cancel() {
        job?.cancel()
        this.viewModelScope.coroutineContext.job.cancel()
    }

    override fun onCleared() {
        super.onCleared()

        sharedData = null
    }
}