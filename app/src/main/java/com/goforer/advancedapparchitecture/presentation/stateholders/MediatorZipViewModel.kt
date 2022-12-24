package com.goforer.advancedapparchitecture.presentation.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.advancedapparchitecture.data.Params
import com.goforer.advancedapparchitecture.data.source.network.response.DataZip
import com.goforer.advancedapparchitecture.data.source.network.response.Resource
import com.goforer.advancedapparchitecture.data.source.network.response.Status
import com.goforer.advancedapparchitecture.domain.RepoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

open class MediatorZipViewModel(
    private val firstUseCase: RepoUseCase,
    private val secondUseCase: RepoUseCase,
    private val firstParams: Params,
    private val secondParams: Params
) : ViewModel() {
    companion object {
        private const val DELIMITER = "!?!"
    }
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

    fun invalidateFirstPagingSource() = firstUseCase.invalidatePagingSource()
    fun invalidateSecondPagingSource() = secondUseCase.invalidatePagingSource()

    private val _value = MutableStateFlow(Resource().loading(Status.LOADING))
    private val value = _value

    init {
        viewModelScope.launch {
            firstUseCase.run(viewModelScope, firstParams).zip(secondUseCase.run(viewModelScope, secondParams)) { firstResource, secondResource ->
                val resource = Resource()

                resource.data = DataZip(firstResource.data, secondResource.data)
                resource.errorCode = firstResource.errorCode or secondResource.errorCode
                resource.message = firstResource.message + DELIMITER + secondResource.message

                return@zip resource
            }.collectLatest {
                value.value = it
            }
        }

    }

    override fun onCleared() {
        super.onCleared()

        value.value = Resource().loading(Status.LOADING)
    }
}