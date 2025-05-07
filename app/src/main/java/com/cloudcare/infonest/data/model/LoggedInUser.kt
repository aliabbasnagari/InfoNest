package com.cloudcare.infonest.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val email: String,
    val displayName: String
)