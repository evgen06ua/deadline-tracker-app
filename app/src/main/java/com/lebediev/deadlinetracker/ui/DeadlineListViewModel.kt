package com.lebediev.deadlinetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebediev.deadlinetracker.data.DeadlineRepository
import com.lebediev.deadlinetracker.data.FirestoreDeadlineRepository
import com.lebediev.deadlinetracker.data.model.Deadline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListUiState(
    val deadlines: List<Deadline> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DeadlineListViewModel(
    private val repo: DeadlineRepository = FirestoreDeadlineRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ListUiState())
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeMyDeadlines()
                .catch {
                    _state.update { s -> s.copy(isLoading = false, error = "Could not load deadlines") }
                }
                .collect { list ->
                    _state.update { s ->
                        s.copy(
                            deadlines = list.sortedBy { d -> d.dueDate },
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun delete(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repo.deleteDeadline(id).isSuccess)
        }
    }
}