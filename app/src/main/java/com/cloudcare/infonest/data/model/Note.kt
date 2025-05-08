package com.cloudcare.infonest.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import kotlin.Result

@Keep
data class Note(
    @DocumentId val id: String = "",
    val userId: String? = null,
    val title: String? = null,
    val content: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", null, null, null, 0L)
}
