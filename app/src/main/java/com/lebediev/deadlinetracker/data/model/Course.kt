package com.lebediev.deadlinetracker.data.model

import com.google.firebase.firestore.DocumentId

data class Course(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val ects: Int = 0
)