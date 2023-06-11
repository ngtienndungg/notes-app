package com.example.notes.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CreateNoteActivity extends AppCompatActivity {

    private ImageView ivBack, ivSave;
    private EditText etInputTitle, etInputSubtitle, etInputNote;
    private TextView tvDateTime;
    private LinearLayout llMiscellaneous;
    private View vSubtitleIndicator;

    private String selectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // Map view from layout
        viewMapping();

        // Setup initiation views
        init();

        // Handle click event
        eventHandling();

    }

    private void viewMapping() {
        ivBack = findViewById(R.id.activity_create_note_ivBack);
        etInputNote = findViewById(R.id.activity_create_note_etInputNote);
        etInputTitle = findViewById(R.id.activity_create_note_etInputTitle);
        etInputSubtitle = findViewById(R.id.activity_create_note_etInputSubtitle);
        tvDateTime = findViewById(R.id.activity_create_note_tvDateTime);
        ivSave = findViewById(R.id.activity_create_note_ivSave);
        llMiscellaneous = findViewById(R.id.llMiscellaneous);
        vSubtitleIndicator = findViewById(R.id.activity_create_note_vSubtitleIndicator);
    }

    private void eventHandling() {
        ivBack.setOnClickListener(v -> onBackPressed());
        ivSave.setOnClickListener(v -> saveNote());
        handleMiscellaneous();
    }

    private void init() {
        tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
        selectedColor = "#333333";
        setSubtitleIndicatorColor();
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
        note.setColor(selectedColor);

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

    private void handleMiscellaneous() {
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(llMiscellaneous);
        llMiscellaneous.findViewById(R.id.layout_miscellaneous_tvMiscellaneous).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView ivColor1 = llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor1);
        final ImageView ivColor2 = llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor2);
        final ImageView ivColor3 = llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor3);
        final ImageView ivColor4 = llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor4);
        final ImageView ivColor5 = llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor5);

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor1).setOnClickListener(v -> {
            selectedColor = "#333333";
            ivColor1.setImageResource(R.drawable.ic_done);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor2).setOnClickListener(v -> {
            selectedColor = "#FDBE3B";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(R.drawable.ic_done);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor3).setOnClickListener(v -> {
            selectedColor = "#FF4842";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(R.drawable.ic_done);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor4).setOnClickListener(v -> {
            selectedColor = "#3A52FC";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(R.drawable.ic_done);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor5).setOnClickListener(v -> {
            selectedColor = "#000000";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicatorColor();
        });
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) vSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }
}