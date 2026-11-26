package com.lebediev.deadlinetracker.data

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.lebediev.deadlinetracker.data.model.Course
import com.lebediev.deadlinetracker.data.model.Deadline
import com.lebediev.deadlinetracker.data.model.DeadlineInput
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreDeadlineRepository : DeadlineRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")

    override fun observeMyDeadlines(): Flow<List<Deadline>> = callbackFlow {
        val registration = db.collection("deadlines")
            .whereEqualTo("ownerId", requireUid())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Deadline::class.java))
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getDeadline(id: String): Result<Deadline> = runCatching {
        val doc = db.collection("deadlines").document(id).get().await()
        doc.toObject(Deadline::class.java) ?: throw IllegalStateException("Deadline not found")
    }

    override suspend fun getCourses(): Result<List<Course>> = runCatching {
        db.collection("courses").get().await()
            .toObjects(Course::class.java)
            .sortedBy { it.name }
    }

    override suspend fun addDeadline(input: DeadlineInput): Result<String> = runCatching {
        val data: HashMap<String, Any> = hashMapOf(
            "ownerId" to requireUid(),
            "courseId" to input.courseId,
            "courseName" to input.courseName,
            "title" to input.title,
            "dueDate" to Timestamp(Date(input.dueDateMillis)),
            "priority" to input.priority,
            "done" to input.done,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("deadlines").add(data).await().id
    }

    override suspend fun updateDeadline(id: String, input: DeadlineInput): Result<Unit> = runCatching {
        val changes: HashMap<String, Any> = hashMapOf(
            "courseId" to input.courseId,
            "courseName" to input.courseName,
            "title" to input.title,
            "dueDate" to Timestamp(Date(input.dueDateMillis)),
            "priority" to input.priority,
            "done" to input.done,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("deadlines").document(id).update(changes).await()
        Unit
    }

    override suspend fun deleteDeadline(id: String): Result<Unit> = runCatching {
        db.collection("deadlines").document(id).delete().await()
        Unit
    }
}