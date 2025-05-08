package com.cloudcare.infonest.presentation.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cloudcare.infonest.data.viewmodel.NoteViewModel
import com.cloudcare.infonest.databinding.ActivityNoteEditorBinding
import com.google.android.material.color.DynamicColors
import com.cloudcare.infonest.data.model.Result
import com.cloudcare.infonest.data.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class NoteEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteEditorBinding
    private val viewModel: NoteViewModel by viewModels()
    private var noteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteId = intent.getStringExtra("NOTE_ID")
        if (noteId != null) {
            loadNote()
        }

        binding.btnSave.setOnClickListener { saveNote() }
        if (noteId != null) {
            binding.btnDelete.setOnClickListener { deleteNote() }
        } else {
            binding.btnDelete.isEnabled = false
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

    }

    private fun loadNote() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val note = viewModel.notes.first { notes ->
                    notes.any { it.id == noteId }
                }.find { it.id == noteId }
                note?.let {
                    binding.etTitle.setText(it.title)
                    binding.etContent.setText(it.content)
                }
            }
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (noteId == null) {
            viewModel.addNote(title, content) { result ->
                handleResult(result, "Note saved")
            }
        } else {
            val updatedNote = Note(
                id = noteId!!,
                title = title,
                content = content,
                userId = viewModel.notes.value.find { it.id == noteId }?.userId ?: ""
            )
            viewModel.updateNote(updatedNote) { result ->
                handleResult(result, "Note updated")
            }
        }
    }

    private fun deleteNote() {
        noteId?.let { id ->
            viewModel.deleteNote(id) { result ->
                handleResult(result, "Note deleted")
            }
        }
    }

    private fun handleResult(result: Result<Unit>, successMessage: String) {
        when (result) {
            is Result.Success -> {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                finish()
            }

            is Result.Error -> {
                Toast.makeText(
                    this,
                    result.exception.message ?: "Operation failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}