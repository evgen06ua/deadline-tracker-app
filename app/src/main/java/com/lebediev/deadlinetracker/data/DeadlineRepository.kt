package com.lebediev.deadlinetracker.data

import com.lebediev.deadlinetracker.data.model.Course
import com.lebediev.deadlinetracker.data.model.Deadline
import com.lebediev.deadlinetracker.data.model.DeadlineInput
import kotlinx.coroutines.flow.Flow

interface DeadlineRepository {
    fun observeMyDeadlines(): Flow<List<Deadline>>
    suspend fun getDeadline(id: String): Result<Deadline>
    suspend fun getCourses(): Result<List<Course>>
    suspend fun addDeadline(input: DeadlineInput): Result<String>
    suspend fun updateDeadline(id: String, input: DeadlineInput): Result<Unit>
    suspend fun deleteDeadline(id: String): Result<Unit>
}