package com.cloudcare.infonest.data

import com.cloudcare.infonest.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(private val auth: FirebaseAuth) {

    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        return try {
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
    }

    suspend fun register(email: String, password: String): Result<LoggedInUser> {
        return try {
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
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val authResult = auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(IOException("Error registering: ", e))
        }
    }

    fun logout() {
        auth.signOut()
    }
}