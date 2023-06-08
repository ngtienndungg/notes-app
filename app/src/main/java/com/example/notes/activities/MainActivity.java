package com.example.notes.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    private ImageView ivAddNoteMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Map view from layout
        viewMapping();

        // Handle set text event
        onClickHandling();

        // Handle click event
        getNotes();
    }

    private void viewMapping() {
        ivAddNoteMain = findViewById(R.id.activity_main_ivAddNoteMain);
    }

    private void onClickHandling() {
        ivAddNoteMain.setOnClickListener(v -> startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
        ));
    }

    private void getNotes() {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            GetNotesTask() {
                super();
            }

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                Log.d("MY_NOTES", notes.toString());
            }
        }
        new GetNotesTask().execute();
    }
}