package com.goforer.advancedapparchitecture.presentation.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.advancedapparchitecture.data.Params
import com.goforer.advancedapparchitecture.data.source.network.response.Resource
import com.goforer.advancedapparchitecture.data.source.network.response.Status
import com.goforer.advancedapparchitecture.domain.RepoUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class MediatorSharedViewModel(private val useCase: RepoUseCase, params: Params) : ViewModel() {
    /*
   val value = useCase.run(viewModelScope, params).flatMapLatest {
        flow {
            emit(it)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Resource().loading(LOADING)
    )

     */

    // You can implement code blow:
    // Just please visit below link if you'd like to know [StatFlow & SharedFlow]
    // Link : https://developer.android.com/kotlin/flow/stateflow-and-sharedflow

    fun invalidatePagingSource() = useCase.invalidatePagingSource()

    private val _value = MutableSharedFlow<Resource>()
    val value = _value

    init {
        viewModelScope.launch {
            Resource().loading(Status.LOADING)
            useCase.run(viewModelScope, params).collectLatest {
                it.loading(Status.LOADING)
                value.emit(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        Resource().loading(Status.LOADING)
    }
}