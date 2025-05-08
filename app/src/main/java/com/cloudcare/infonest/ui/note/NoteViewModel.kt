package com.cloudcare.infonest.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudcare.infonest.data.FirebaseRepository
import com.cloudcare.infonest.data.model.Note
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.cloudcare.infonest.data.Result;
import com.cloudcare.infonest.data.model.LoggedInUser

class NoteViewModel(private val repository: FirebaseRepository = FirebaseRepository()) :
    ViewModel() {
    val notes: StateFlow<List<Note>> = repository.notes

    init {
        if (repository.isUserLoggedIn()) {
            repository.listenToNotes()
        }
    }

    fun login(email: String, password: String, onResult: (Result<LoggedInUser>) -> LoggedInUser) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result is Result.Success) repository.listenToNotes()
            onResult(result)
        }
    }

    fun register(
        email: String,
        password: String,
        onResult: (Result<LoggedInUser>) -> LoggedInUser
    ) {
        viewModelScope.launch {
            val result = repository.register(email, password)
            if (result is Result.Success) repository.listenToNotes()
            onResult(result)
        }
    }

    fun logout() {
        repository.logout()
    }

    fun addNote(title: String, content: String, onResult: (Result<Unit>) -> Unit) {
        val uid = repository.userId()
        if (uid == null) {
            onResult(Result.Error(Exception("User ID is null")))
            return
        }

        viewModelScope.launch {
            try {
                val note = Note(title = title, content = content, userId = uid)
                val result = repository.addNote(note)
                onResult(result)
            } catch (e: Exception) {
                onResult(Result.Error(e))
            }
        }
    }


    fun updateNote(note: Note, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.updateNote(note))
        }
    }

    fun deleteNote(noteId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.deleteNote(noteId))
        }
    }
}