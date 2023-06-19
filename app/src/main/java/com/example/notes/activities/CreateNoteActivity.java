package com.example.notes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.notes.R;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CreateNoteActivity extends AppCompatActivity {

    private ImageView ivBack, ivSave;
    private EditText etInputTitle, etInputSubtitle, etInputNote;
    private TextView tvDateTime;
    private LinearLayout llMiscellaneous;
    private View vSubtitleIndicator;
    private ImageView ivNoteImage;
    private LinearLayout llNoteUrl;
    private TextView tvWebUrl;
    private ImageView ivRemoveImage;
    private ImageView ivRemoveUrl;

    private String selectedColor;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog dialogAddUrl;

    private Note alreadyExistNote;

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
        ivNoteImage = findViewById(R.id.activity_create_note_ivNoteImage);
        tvWebUrl = findViewById(R.id.activity_create_note_tvWebUrl);
        llNoteUrl = findViewById(R.id.activity_create_note_llNoteUrl);
        ivRemoveImage = findViewById(R.id.activity_create_note_ivRemoveImage);
        ivRemoveUrl = findViewById(R.id.activity_create_note_ivRemoveUrl);
    }

    private void eventHandling() {
        ivBack.setOnClickListener(v -> onBackPressed());
        ivSave.setOnClickListener(v -> saveNote());
        ivRemoveImage.setOnClickListener(v -> removeImage());
        ivRemoveUrl.setOnClickListener(v -> removeUrl());
        handleMiscellaneous();
    }

    private void init() {
        tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
        selectedColor = "#333333";
        selectedImagePath = "";
        if (getIntent().getBooleanExtra("isReviewOrUpdate", false)) {
            alreadyExistNote = (Note) getIntent().getSerializableExtra("note");
            setReviewOrUpdateNote();
        }
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

        if (!selectedImagePath.equals("")) {
            note.setImagePath(selectedImagePath);
        }

        if (tvWebUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(tvWebUrl.getText().toString());
        }

        if (alreadyExistNote != null) {
            note.setId(alreadyExistNote.getId());
        }

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

    @SuppressLint("InlinedApi")
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

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_llAddImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_llAddUrl).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddUrlDialog();
        });

        if (alreadyExistNote != null && alreadyExistNote.getColor() != null && !alreadyExistNote.getColor().trim().toString().isEmpty()) {
            switch (alreadyExistNote.getColor()) {
                case "#FDBE3B":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor2).performClick();
                    break;
                case "#FF4842":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor3).performClick();
                    break;
                case "#3A52FC":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor4).performClick();
                    break;
                case "#000000":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor5).performClick();
                    break;
            }
        }
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) vSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.toast_something_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void setReviewOrUpdateNote() {
        etInputTitle.setText(alreadyExistNote.getTitle());
        etInputSubtitle.setText(alreadyExistNote.getSubtitle());
        etInputNote.setText(alreadyExistNote.getNoteContent());
        tvDateTime.setText(alreadyExistNote.getDateTime());

        if (alreadyExistNote.getImagePath() != null && !alreadyExistNote.getImagePath().trim().isEmpty()) {
            ivNoteImage.setImageBitmap(BitmapFactory.decodeFile(alreadyExistNote.getImagePath()));
            ivNoteImage.setVisibility(View.VISIBLE);
            ivRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyExistNote.getImagePath();
        }

        if (alreadyExistNote.getWebLink() != null && !alreadyExistNote.getWebLink().trim().isEmpty()) {
            tvWebUrl.setText(alreadyExistNote.getWebLink());
            tvWebUrl.setVisibility(View.VISIBLE);
            llNoteUrl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ivNoteImage.setImageBitmap(bitmap);
                        ivNoteImage.setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);
                        ivRemoveImage.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Toast.makeText(this, getResources().getString(R.string.toast_something_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            }
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

    private void showAddUrlDialog() {
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layout_add_url));
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                final EditText etInputUrl = view.findViewById(R.id.layout_add_url_etInputUrl);
                etInputUrl.requestFocus();

                view.findViewById(R.id.layout_add_url_tvAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etInputUrl.getText().toString().trim().isEmpty()) {
                            Toast.makeText(CreateNoteActivity.this, getResources().getString(R.string.toast_empty_url), Toast.LENGTH_SHORT).show();
                        } else if (!Patterns.WEB_URL.matcher(etInputUrl.getText().toString().trim()).matches()) {
                            Toast.makeText(CreateNoteActivity.this, getResources().getString(R.string.toast_invalid_url), Toast.LENGTH_SHORT).show();
                        } else {
                            tvWebUrl.setText(etInputUrl.getText().toString().trim());
                            tvWebUrl.setVisibility(View.VISIBLE);
                            llNoteUrl.setVisibility(View.VISIBLE);
                            dialogAddUrl.dismiss();
                            dialogAddUrl = null;
                        }
                    }
                });

                view.findViewById(R.id.layout_add_url_tvCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogAddUrl.dismiss();
                    }
                });
            }
            dialogAddUrl.show();
        }
    }

    private void removeImage() {
        selectedImagePath = "";
        ivNoteImage.setVisibility(View.GONE);
        ivRemoveImage.setVisibility(View.GONE);
    }

    private void removeUrl() {
        tvWebUrl.setText("");
        tvWebUrl.setVisibility(View.GONE);
        llNoteUrl.setVisibility(View.GONE);
    }
}