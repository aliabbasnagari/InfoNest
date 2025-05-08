package com.cloudcare.infonest.data

import com.cloudcare.infonest.data.model.LoggedInUser
import com.cloudcare.infonest.data.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val notesCollection = db.collection("notes")
    private var notesListener: ListenerRegistration? = null
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    suspend fun login(email: String, password: String): Result<LoggedInUser> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            val loggedInUser = LoggedInUser(
                firebaseUser.uid,
                firebaseUser.email!!,
                firebaseUser.displayName ?: "Unknown User"
            )
            Result.Success(loggedInUser)
        } else {
            Result.Error(IOException("Error logging in: User not found"))
        }
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.Error(IOException("Invalid email or password", e))
    } catch (e: FirebaseAuthInvalidUserException) {
        Result.Error(IOException("User does not exist or is disabled", e))
    } catch (e: Exception) {
        Result.Error(IOException("Error logging in: ${e.message}", e))
    }

    suspend fun register(email: String, password: String): Result<LoggedInUser> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(email.substringBefore("@"))
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            val loggedInUser = LoggedInUser(
                firebaseUser.uid,
                firebaseUser.email!!,
                firebaseUser.displayName ?: "Unknown User"
            )
            Result.Success(loggedInUser)
        } else {
            Result.Error(IOException("Error registering: Registration failed"))
        }
    } catch (e: FirebaseAuthWeakPasswordException) {
        Result.Error(IOException("Weak password: ${e.message}", e))
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.Error(IOException("Email already in use", e))
    } catch (e: Exception) {
        Result.Error(IOException("Error registering: ${e.message}", e))
    }


    suspend fun resetPassword(email: String): Result<Unit> = try {
        val authResult = auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(IOException("Error registering: ", e))
    }


    fun logout() {
        notesListener?.remove()
        auth.signOut()
    }

    fun listenToNotes() {
        val userId = auth.currentUser?.uid ?: return
        notesListener = notesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.let {
                    _notes.value = it.toObjects(Note::class.java)
                }
            }
    }

    suspend fun addNote(note: Note): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        notesCollection.add(note.copy(userId = userId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateNote(note: Note): Result<Unit> = try {
        notesCollection.document(note.id).set(note).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteNote(noteId: String): Result<Unit> = try {
        notesCollection.document(noteId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun isUserLoggedIn() = auth.currentUser != null

    fun userId() = auth.currentUser?.uid;
}