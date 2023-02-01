package com.goforer.advancedapparchitecture.presentation.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.advancedapparchitecture.data.Params
import com.goforer.advancedapparchitecture.data.source.network.response.Resource
import com.goforer.advancedapparchitecture.data.source.network.response.Status
import com.goforer.advancedapparchitecture.domain.RepoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

open class MediatorStatedViewModel(private val useCase: RepoUseCase, params: Params) : ViewModel() {
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

    private val _value = MutableStateFlow(Resource().loading(Status.LOADING))
    val value = _value

    init {
        viewModelScope.launch {
            useCase.run(viewModelScope, params).distinctUntilChanged { old, new ->
                old != new
            } .collectLatest {
                _value.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        value.value = Resource().loading(Status.LOADING)
    }
}