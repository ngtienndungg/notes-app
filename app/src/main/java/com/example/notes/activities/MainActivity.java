package com.example.notes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.adapters.NoteAdapter;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    private ImageView ivAddNoteMain;
    private RecyclerView rvNotes;

    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private EditText etInputSearch;
    private ImageView ivAddImage;
    private ImageView ivAddUrl;
    private ImageView ivAddNote;

    private int noteClickedPosition;
    private AlertDialog dialogAddUrl;
    AlertDialog dialogDeleteNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Map view from layout
        viewMapping();

        // Handle set text event
        eventHandling();

        // Setup recycler view
        setupRecyclerView();

        // Handle click event
        getNotes(REQUEST_CODE_SHOW_NOTE, false);
    }

    private void viewMapping() {
        ivAddNoteMain = findViewById(R.id.activity_main_ivAddNoteMain);
        rvNotes = findViewById(R.id.activity_main_rvNotes);
        etInputSearch = findViewById(R.id.activity_main_etInputSearch);
        ivAddImage = findViewById(R.id.activity_main_ivAddImage);
        ivAddUrl = findViewById(R.id.activity_main_ivAddNoteUrl);
        ivAddNote = findViewById(R.id.activity_main_ivAddNote);
    }

    private void eventHandling() {
        ivAddNoteMain.setOnClickListener(v -> addNote());
        ivAddNote.setOnClickListener(v -> addNote());
        ivAddImage.setOnClickListener(v -> addNoteImage());
        ivAddUrl.setOnClickListener(v -> showAddUrlDialog());
        searchNote();
    }

    private void addNote() {
        startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE);
    }

    private void searchNote() {
        etInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    noteAdapter.searchNote(s.toString());
                }
            }
        });
    }

    @SuppressLint("InlinedApi")
    private void addNoteImage() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            selectImage();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.toast_something_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        openNote(note);
    }

    @Override
    public void onNoteLongClicked(Note note, int position) {
        noteClickedPosition = position;
        showPopupWindow(note);
    }

    @Override
    public void onMoreClicked(Note note, int position) {
        noteClickedPosition = position;
        showPopupWindow(note);
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
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        rvNotes.setAdapter(noteAdapter);
    }

    private void showAddUrlDialog() {
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                    findViewById(R.id.layout_add_url));
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                final EditText etInputUrl = view.findViewById(R.id.layout_add_url_etInputUrl);
                etInputUrl.requestFocus();

                view.findViewById(R.id.layout_add_url_tvAdd).setOnClickListener(v -> {
                    if (etInputUrl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_empty_url), Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(etInputUrl.getText().toString().trim()).matches()) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_invalid_url), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("quickActionType", "url");
                        intent.putExtra("url", etInputUrl.getText().toString().trim());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        dialogAddUrl.dismiss();
                        dialogAddUrl = null;
                    }
                });

                view.findViewById(R.id.layout_add_url_tvCancel).setOnClickListener(v -> {
                    dialogAddUrl.dismiss();
                    dialogAddUrl = null;
                });
            }
            dialogAddUrl.show();
        }
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
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    try {
                        String selectedPath = getPathFromUri(selectedUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedPath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception e) {
                        Toast.makeText(this, getResources().getString(R.string.toast_something_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            selectImage();
        }
    }

    private void showPopupWindow(Note note) {
        PopupWindow popupWindow;
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_menu_note_item, findViewById(R.id.layout_menu_note_item_llMenu));
        popupWindow = new PopupWindow(view, 320, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAsDropDown(Objects.requireNonNull(rvNotes.findViewHolderForAdapterPosition(noteClickedPosition)).itemView.findViewById(R.id.item_container_note_ivMore), 0, 0);
        view.findViewById(R.id.layout_menu_note_item_clOpenNote).setOnClickListener(v -> {
            openNote(note);
            popupWindow.dismiss();
        });
        view.findViewById(R.id.layout_menu_note_item_clDelete).setOnClickListener(v -> {
            showDeleteDialog(note);
            popupWindow.dismiss();
        });
    }

    private void openNote(Note note) {
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isReviewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void showDeleteDialog(Note note) {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layout_delete_note_container)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.layout_delete_note_tvConfirmDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        DeleteNoteTask() {
                            super();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(note);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                        }
                    }
                    new DeleteNoteTask().execute();
                    noteList.remove(noteClickedPosition);
                    noteAdapter.notifyItemRemoved(noteClickedPosition);
                    dialogDeleteNote.dismiss();
                    dialogDeleteNote = null;
                }
            });

            view.findViewById(R.id.layout_delete_note_tvCancel).setOnClickListener(v -> {
                dialogDeleteNote.dismiss();
                dialogDeleteNote = null;
            });
        }
        dialogDeleteNote.show();
    }
}