package com.cloudcare.infonest.data.model

import com.google.firebase.firestore.DocumentId

data class Note(
    @DocumentId val id: String?,
    val userId: String?,
    val title: String?,
    val content: String?,
    val timestamp: Long = System.currentTimeMillis()
)