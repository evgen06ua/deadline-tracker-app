package com.lebediev.deadlinetracker.data.model

data class DeadlineInput(
    val title: String,
    val courseId: String,
    val courseName: String,
    val dueDateMillis: Long,
    val priority: String,
    val done: Boolean
)