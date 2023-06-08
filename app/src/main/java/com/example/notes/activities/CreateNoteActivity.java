package com.example.notes.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CreateNoteActivity extends AppCompatActivity {

    private ImageView ivBack, ivSave;
    private EditText etInputTitle, etInputSubtitle, etInputNote;
    private TextView tvDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // Map view from layout
        viewMapping();

        // Handle set text event
        onSetTextHandling();

        // Handle click event
        onClickHandling();

    }

    private void viewMapping() {
        ivBack = findViewById(R.id.activity_create_note_ivBack);
        etInputNote = findViewById(R.id.activity_create_note_etInputNote);
        etInputTitle = findViewById(R.id.activity_create_note_etInputTitle);
        etInputSubtitle = findViewById(R.id.activity_create_note_etInputSubtitle);
        tvDateTime = findViewById(R.id.activity_create_note_tvDateTime);
        ivSave = findViewById(R.id.activity_create_note_ivSave);
    }

    private void onClickHandling() {
        ivBack.setOnClickListener(v -> onBackPressed());
        ivSave.setOnClickListener(v -> saveNote());
    }

    private void onSetTextHandling() {
        tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
    }

    private void saveNote() {
        if (etInputTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.toast_title_empty), Toast.LENGTH_SHORT).show();
            return;
        } else if (etInputSubtitle.getText().toString().trim().isEmpty() && etInputNote.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.toast_note_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(etInputTitle.getText().toString());
        note.setSubtitle(etInputSubtitle.getText().toString());
        note.setNoteContent(etInputNote.getText().toString());
        note.setDateTime(tvDateTime.getText().toString());

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            SaveNoteTask() {
                super();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }
}