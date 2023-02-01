package com.goforer.advancedapparchitecture.presentation.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.advancedapparchitecture.data.Params
import com.goforer.advancedapparchitecture.domain.UseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
open class ProcessorSharedViewModel<Value>(useCase: UseCase<Value>, params: Params) : ViewModel() {
    val value = useCase.run(viewModelScope, params).flatMapLatest {
        flow {
            emit(it)
        }
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = params.replyCount
    )
}