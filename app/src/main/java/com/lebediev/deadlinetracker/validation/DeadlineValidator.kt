package com.lebediev.deadlinetracker.validation

import java.time.LocalDate
import java.time.ZoneId

data class DeadlineErrors(
    val title: String? = null,
    val course: String? = null,
    val dueDate: String? = null,
    val priority: String? = null
) {
    fun hasErrors() = title != null || course != null || dueDate != null || priority != null
}

object DeadlineValidator {
    val priorities = listOf("low", "medium", "high")

    fun validateTitle(title: String): String? = when {
        title.isBlank() -> "Title is required"
        title.length > 100 -> "Maximum 100 characters"
        else -> null
    }

    fun validateCourse(courseId: String): String? =
        if (courseId.isBlank()) "Pick a course" else null

    fun validatePriority(priority: String): String? =
        if (priority in priorities) null else "Pick a priority"

    fun parseDate(text: String): Long? = try {
        LocalDate.parse(text.trim())
            .atTime(23, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (e: Exception) {
        null
    }

    fun validateDueDate(text: String, done: Boolean, nowMillis: Long): String? {
        val millis = parseDate(text) ?: return "Use the format yyyy-mm-dd"
        if (millis < nowMillis && !done) {
            return "Date is in the past. Pick a later date or mark it done"
        }
        return null
    }

    fun validate(
        title: String,
        courseId: String,
        dueDateText: String,
        priority: String,
        done: Boolean,
        nowMillis: Long
    ) = DeadlineErrors(
        title = validateTitle(title),
        course = validateCourse(courseId),
        dueDate = validateDueDate(dueDateText, done, nowMillis),
        priority = validatePriority(priority)
    )
}