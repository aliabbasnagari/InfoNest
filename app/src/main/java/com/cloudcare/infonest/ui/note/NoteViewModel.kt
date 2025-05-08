package com.cloudcare.infonest.ui.note

import androidx.lifecycle.ViewModel
import com.cloudcare.infonest.data.FirebaseRepository
import com.cloudcare.infonest.data.model.Note
import kotlinx.coroutines.flow.StateFlow

class NoteViewModel(private val repository: FirebaseRepository = FirebaseRepository()) :
    ViewModel() {
    val notes: StateFlow<List<Note>> = repository.notes
}