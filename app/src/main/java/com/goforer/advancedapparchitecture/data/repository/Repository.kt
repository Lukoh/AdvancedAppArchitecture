package com.goforer.advancedapparchitecture.data.repository

import com.goforer.advancedapparchitecture.data.Query
import com.goforer.advancedapparchitecture.data.source.network.NetworkErrorHandler
import com.goforer.advancedapparchitecture.data.source.network.api.RestAPI
import com.goforer.base.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
abstract class Repository<Resource> {
    @Inject
    lateinit var restAPI: RestAPI

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var storage: Storage

    companion object {
        internal const val ITEM_COUNT = 20
        internal const val FIRST_PAGE = 1
    }

    abstract fun handle(viewModelScope: CoroutineScope, replyCount: Int, query: Query): Flow<Resource>

    open fun invalidatePagingSource() {}

    protected fun handleNetworkError(errorMessage: String) {
        networkErrorHandler.handleError(errorMessage)
    }
}