package com.example.notes.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.notes.R;
import com.example.notes.adapters.NoteAdapter;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;

    private ImageView ivAddNoteMain;
    private RecyclerView rvNotes;

    private List<Note> noteList;
    private NoteAdapter noteAdapter;

    private int noteClickedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Map view from layout
        viewMapping();

        // Handle set text event
        onClickHandling();

        // Setup recycler view
        setupRecyclerView();

        // Handle click event
        getNotes(REQUEST_CODE_SHOW_NOTE, false);
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isReviewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void viewMapping() {
        ivAddNoteMain = findViewById(R.id.activity_main_ivAddNoteMain);
        rvNotes = findViewById(R.id.activity_main_rvNotes);
    }

    private void onClickHandling() {
        ivAddNoteMain.setOnClickListener(v -> startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
        ));
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            GetNotesTask() {
                super();
            }

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    rvNotes.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (!isNoteDeleted) {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                    } else {
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    private void setupRecyclerView() {
        rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        rvNotes.setAdapter(noteAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

}