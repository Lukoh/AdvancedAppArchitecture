package com.goforer.advancedapparchitecture.data.source.network.mediator

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.goforer.advancedapparchitecture.data.repository.Repository.Companion.ITEM_COUNT
import com.goforer.advancedapparchitecture.data.source.network.response.Resource
import com.goforer.advancedapparchitecture.data.source.network.response.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn

/**
 * A generic class that can provide a resource backed by the network.
 * <p>
 * You can read more about it in the <a href="https://developer.android.com/arch">Architecture
 * Guide</a>.
 */
abstract class PagingDataMediator<Response> constructor(
    viewModelScope: CoroutineScope, replyCount: Int, private val enabledCache: Boolean
) {
    private val resource by lazy {
        Resource()
    }

    internal val asSharedFlow = flow {
        emit(resource.loading(Status.LOADING))
        clearCache()
        load().collect {
            if (enabledCache) {
                saveToCache(it)
                loadFromCache(false, ITEM_COUNT, 1)
            } else
                emit(resource.success(it))
        }
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = replyCount
    )

    @WorkerThread
    protected open suspend fun saveToCache(value: Response) {
    }

    @MainThread
    protected open fun loadFromCache(
        isLatest: Boolean,
        itemCount: Int,
        pages: Int
    ): Flow<Response>? = null

    @MainThread
    protected abstract fun load(): Flow<Response>

    protected open suspend fun clearCache() {}
}