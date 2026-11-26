package com.lebediev.deadlinetracker.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Deadline(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val title: String = "",
    val dueDate: Date? = null,
    val priority: String = "medium",
    val done: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)