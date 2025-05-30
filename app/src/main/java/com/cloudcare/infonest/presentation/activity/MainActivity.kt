package com.cloudcare.infonest.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudcare.infonest.R
import com.cloudcare.infonest.databinding.ActivityMainBinding
import com.cloudcare.infonest.ui.adapter.NoteAdapter
import com.cloudcare.infonest.data.viewmodel.NoteViewModel
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        observeNotes()

        binding.fabAddNote.setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
            Log.d("CALLED", "______################################")
        }
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            Log.d("CALLED", "______################################")
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note ->
                val intent = Intent(this, NoteEditorActivity::class.java)
                intent.putExtra("NOTE_ID", note.id)
                startActivity(intent)
            },
            onDeleteClick = { note ->
                viewModel.deleteNote(note.id) { /* Handle result if needed */ }
            }
        )
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun observeNotes() {
        lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                adapter.submitList(notes)
            }
        }
    }
}