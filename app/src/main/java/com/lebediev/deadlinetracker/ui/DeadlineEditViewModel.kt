package com.lebediev.deadlinetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebediev.deadlinetracker.data.DeadlineRepository
import com.lebediev.deadlinetracker.data.FirestoreDeadlineRepository
import com.lebediev.deadlinetracker.data.model.Course
import com.lebediev.deadlinetracker.data.model.DeadlineInput
import com.lebediev.deadlinetracker.validation.DeadlineErrors
import com.lebediev.deadlinetracker.validation.DeadlineValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class EditUiState(
    val title: String = "",
    val dueDateText: String = "",
    val priority: String = "medium",
    val courseId: String = "",
    val courseName: String = "",
    val done: Boolean = false,
    val courses: List<Course> = emptyList(),
    val errors: DeadlineErrors = DeadlineErrors(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null,
    val saved: Boolean = false
)

class DeadlineEditViewModel(
    private val id: String?,
    private val repo: DeadlineRepository = FirestoreDeadlineRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState())
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val courses = repo.getCourses().getOrNull()
            if (courses == null) {
                _state.update { it.copy(isLoading = false, message = "Could not load courses") }
                return@launch
            }
            if (id == null) {
                _state.update { it.copy(courses = courses, isLoading = false) }
                return@launch
            }
            val deadline = repo.getDeadline(id).getOrNull()
            if (deadline == null) {
                _state.update {
                    it.copy(courses = courses, isLoading = false, message = "Could not load the deadline")
                }
                return@launch
            }
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            _state.update {
                it.copy(
                    courses = courses,
                    title = deadline.title,
                    courseId = deadline.courseId,
                    courseName = deadline.courseName,
                    dueDateText = deadline.dueDate?.let { d -> format.format(d) } ?: "",
                    priority = deadline.priority,
                    done = deadline.done,
                    isLoading = false
                )
            }
        }
    }

    fun onTitle(value: String) = _state.update { it.copy(title = value) }
    fun onDate(value: String) = _state.update { it.copy(dueDateText = value) }
    fun onPriority(value: String) = _state.update { it.copy(priority = value) }
    fun onDone(value: Boolean) = _state.update { it.copy(done = value) }
    fun onCourse(course: Course) =
        _state.update { it.copy(courseId = course.id, courseName = course.name) }

    fun save() {
        val s = _state.value
        val errors = DeadlineValidator.validate(
            title = s.title.trim(),
            courseId = s.courseId,
            dueDateText = s.dueDateText,
            priority = s.priority,
            done = s.done,
            nowMillis = System.currentTimeMillis()
        )
        if (errors.hasErrors()) {
            _state.update { it.copy(errors = errors, message = null) }
            return
        }
        val millis = DeadlineValidator.parseDate(s.dueDateText) ?: return
        val input = DeadlineInput(
            title = s.title.trim(),
            courseId = s.courseId,
            courseName = s.courseName,
            dueDateMillis = millis,
            priority = s.priority,
            done = s.done
        )
        _state.update { it.copy(errors = DeadlineErrors(), isSaving = true, message = null) }
        viewModelScope.launch {
            val result = if (id == null) {
                repo.addDeadline(input).map { }
            } else {
                repo.updateDeadline(id, input)
            }
            if (result.isSuccess) {
                _state.update { it.copy(isSaving = false, saved = true) }
            } else {
                _state.update { it.copy(isSaving = false, message = "Could not save. Please try again") }
            }
        }
    }
}