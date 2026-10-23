package com.lebediev.deadlinetracker

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val isLoggedInFlow: Flow<Boolean>
    fun isLoggedIn(): Boolean
    fun currentEmail(): String?
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    fun logout()
}

class FirebaseAuthRepository : AuthRepository {
    private val auth = Firebase.auth

    override val isLoggedInFlow: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser != null) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun isLoggedIn() = auth.currentUser != null

    override fun currentEmail() = auth.currentUser?.email

    override suspend fun register(email: String, password: String): Result<Unit> =
        runCatching { auth.createUserWithEmailAndPassword(email, password).await(); Unit }

    override suspend fun login(email: String, password: String): Result<Unit> =
        runCatching { auth.signInWithEmailAndPassword(email, password).await(); Unit }

    override fun logout() = auth.signOut()
}