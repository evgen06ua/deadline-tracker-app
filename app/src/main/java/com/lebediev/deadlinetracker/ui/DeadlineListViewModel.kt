package com.lebediev.deadlinetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebediev.deadlinetracker.data.DeadlineRepository
import com.lebediev.deadlinetracker.data.FirestoreDeadlineRepository
import com.lebediev.deadlinetracker.data.model.Deadline
import com.lebediev.deadlinetracker.data.model.DeadlineInput
import com.lebediev.deadlinetracker.logic.DeadlineFilter
import com.lebediev.deadlinetracker.logic.DeadlineStats
import com.lebediev.deadlinetracker.logic.DeadlineStatsCalculator
import com.lebediev.deadlinetracker.logic.StatusFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class ListUiState(
    val deadlines: List<Deadline> = emptyList(),
    val allCount: Int = 0,
    val query: String = "",
    val hideDone: Boolean = false,
    val stats: DeadlineStats = DeadlineStats(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DeadlineListViewModel(
    private val repo: DeadlineRepository = FirestoreDeadlineRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ListUiState())
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    private var all: List<Deadline> = emptyList()

    init {
        viewModelScope.launch {
            repo.observeMyDeadlines()
                .catch {
                    _state.update { s -> s.copy(isLoading = false, error = "Could not load deadlines") }
                }
                .collect { list ->
                    all = list.sortedBy { d -> d.dueDate }
                    _state.update { s -> s.copy(isLoading = false, error = null) }
                    publish()
                }
        }
    }

    private fun publish() {
        val s = _state.value
        val shown = StatusFilter.visible(DeadlineFilter.search(all, s.query), s.hideDone)
        _state.update {
            it.copy(
                deadlines = shown,
                allCount = all.size,
                stats = DeadlineStatsCalculator.compute(all, Date())
            )
        }
    }

    fun onQuery(value: String) {
        _state.update { s -> s.copy(query = value) }
        publish()
    }

    fun onHideDone(value: Boolean) {
        _state.update { s -> s.copy(hideDone = value) }
        publish()
    }

    fun toggleDone(d: Deadline, onResult: (Boolean) -> Unit) {
        val millis = d.dueDate?.time
        if (millis == null) {
            onResult(false)
            return
        }
        val input = DeadlineInput(
            title = d.title,
            courseId = d.courseId,
            courseName = d.courseName,
            dueDateMillis = millis,
            priority = d.priority,
            done = !d.done
        )
        viewModelScope.launch {
            onResult(repo.updateDeadline(d.id, input).isSuccess)
        }
    }

    fun delete(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repo.deleteDeadline(id).isSuccess)
        }
    }
}