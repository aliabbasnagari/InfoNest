package com.cloudcare.infonest.data

import android.util.Log
import com.cloudcare.infonest.data.model.LoggedInUser
import com.cloudcare.infonest.data.model.Note
import com.cloudcare.infonest.data.model.Result
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

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    fun isUserLoggedIn() = auth.currentUser != null

    fun userId() = auth.currentUser?.uid;

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    suspend fun login(email: String, password: String): Result<LoggedInUser> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            val loggedInUser = LoggedInUser(
                firebaseUser.uid,
                firebaseUser.email!!,
                firebaseUser.displayName ?: "Unknown User"
            )
            setLoggedInUser(loggedInUser)
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
            setLoggedInUser(loggedInUser)
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
        auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(IOException("Error registering: ", e))
    }


    fun logout() {
        notesListener?.remove()
        auth.signOut()
    }

    fun listenToNotes() {
        Log.d("CALLED", "listenToNotes")
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
        listenToNotes()
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
}