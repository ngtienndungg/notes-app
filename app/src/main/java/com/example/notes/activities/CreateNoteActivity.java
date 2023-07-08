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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CreateNoteActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private ImageView ivBack, ivSave;
    private EditText etInputTitle, etInputSubtitle, etInputNote;
    private TextView tvDateTime;
    private LinearLayout llMiscellaneous;
    private View vSubtitleIndicator;
    private ImageView ivNoteImage;
    private LinearLayout llNoteUrl;
    private TextView tvNoteUrl;
    private TextView tvAddUrl;
    private TextView tvAddImage;
    private ImageView ivRemoveImage;
    private ImageView ivRemoveUrl;
    private String selectedColor;
    private String selectedImagePath;
    private AlertDialog dialogAddUrl;
    private AlertDialog dialogDeleteNote;
    private AlertDialog dialogUnsavedNote;

    private Note alreadyExistNote;
    private boolean isChanged = false;

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
        llMiscellaneous = findViewById(R.id.layout_miscellaneous_llMiscellaneous);
        vSubtitleIndicator = findViewById(R.id.activity_create_note_vSubtitleIndicator);
        ivNoteImage = findViewById(R.id.activity_create_note_ivNoteImage);
        tvNoteUrl = findViewById(R.id.activity_create_note_tvNoteUrl);
        llNoteUrl = findViewById(R.id.activity_create_note_llNoteUrl);
        ivRemoveImage = findViewById(R.id.activity_create_note_ivRemoveImage);
        ivRemoveUrl = findViewById(R.id.activity_create_note_ivRemoveUrl);
        tvAddUrl = findViewById(R.id.layout_miscellaneous_tvAddUrl);
        tvAddImage = findViewById(R.id.layout_miscellaneous_tvAddImage);
    }

    private void eventHandling() {
        ivBack.setOnClickListener(v -> showUnsavedDialog());
        ivSave.setOnClickListener(v -> {
            saveNote();
        });
        ivRemoveImage.setOnClickListener(v -> removeImage());
        ivRemoveUrl.setOnClickListener(v -> removeUrl());
        handleMiscellaneous();
    }

    private void init() {
        tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date()));
        selectedColor = "#141414";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isReviewOrUpdate", false)) {
            alreadyExistNote = (Note) getIntent().getSerializableExtra("note");
            setReviewOrUpdateNote();
        }

        if (getIntent().getBooleanExtra("isFromQuickAction", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("image")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    ivNoteImage.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    ivNoteImage.setVisibility(View.VISIBLE);
                    ivRemoveImage.setVisibility(View.VISIBLE);
                } else if (type.equals("url")) {
                    tvNoteUrl.setText(getIntent().getStringExtra("url"));
                    tvNoteUrl.setVisibility(View.VISIBLE);
                    ivRemoveUrl.setVisibility(View.VISIBLE);
                    llNoteUrl.setVisibility(View.VISIBLE);
                }
            }
        }

        setSubtitleIndicatorColor();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isChanged = true;
                tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date()));
            }
        };

        etInputNote.addTextChangedListener(textWatcher);
        etInputTitle.addTextChangedListener(textWatcher);
        etInputSubtitle.addTextChangedListener(textWatcher);
        tvNoteUrl.addTextChangedListener(textWatcher);
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
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault());
            Date originalDatetime = originalFormat.parse(tvDateTime.getText().toString());
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            assert originalDatetime != null;
            String targetDatetime = targetFormat.format(originalDatetime);
            note.setDateTime(targetFormat.parse(targetDatetime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        note.setColor(selectedColor);

        if (!selectedImagePath.equals("")) {
            note.setImagePath(selectedImagePath);
        }

        if (tvNoteUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(tvNoteUrl.getText().toString());
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
        llMiscellaneous.findViewById(R.id.layout_miscellaneous_llOpenMiscellaneous).setOnClickListener(v -> {
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
            selectedColor = "#141414";
            ivColor1.setImageResource(R.drawable.ic_done);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor2).setOnClickListener(v -> {
            selectedColor = "#FAC904";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(R.drawable.ic_done);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor3).setOnClickListener(v -> {
            selectedColor = "#B885EB";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(R.drawable.ic_done);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor4).setOnClickListener(v -> {
            selectedColor = "#7684FF";
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(R.drawable.ic_done);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor5).setOnClickListener(v -> {
            selectedColor = "#66BD5B";
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
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                selectImage();
            }
        });

        if (alreadyExistNote != null && alreadyExistNote.getColor() != null && !alreadyExistNote.getColor().trim().isEmpty()) {
            switch (alreadyExistNote.getColor()) {
                case "#FAC904":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor2).performClick();
                    break;
                case "#B885EB":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor3).performClick();
                    break;
                case "#7684FF":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor4).performClick();
                    break;
                case "#66BD5B":
                    llMiscellaneous.findViewById(R.id.layout_miscellaneous_ivColor5).performClick();
                    break;
            }
        }

        llMiscellaneous.findViewById(R.id.layout_miscellaneous_llAddUrl).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddUrlDialog();
        });

        if (alreadyExistNote != null) {
            llMiscellaneous.findViewById(R.id.layout_miscellaneous_llDeleteNote).setVisibility(View.VISIBLE);
            llMiscellaneous.findViewById(R.id.layout_miscellaneous_llDeleteNote).setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteDialog();
            });
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
        tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(alreadyExistNote.getDateTime()));

        if (alreadyExistNote.getImagePath() != null && !alreadyExistNote.getImagePath().trim().isEmpty()) {
            ivNoteImage.setImageBitmap(BitmapFactory.decodeFile(alreadyExistNote.getImagePath()));
            ivNoteImage.setVisibility(View.VISIBLE);
            ivRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyExistNote.getImagePath();
            tvAddImage.setText(getResources().getString(R.string.change_image));
        }

        if (alreadyExistNote.getWebLink() != null && !alreadyExistNote.getWebLink().trim().isEmpty()) {
            tvNoteUrl.setText(alreadyExistNote.getWebLink());
            tvNoteUrl.setVisibility(View.VISIBLE);
            llNoteUrl.setVisibility(View.VISIBLE);
            tvAddUrl.setText(getResources().getString(R.string.change_url));
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
                        tvAddImage.setText(getResources().getString(R.string.change_image));
                        isChanged = true;
                        tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(new Date()));
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
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                    findViewById(R.id.layout_add_url));
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                final EditText etInputUrl = view.findViewById(R.id.layout_add_url_etInputUrl);

                if (llNoteUrl.getVisibility() == View.VISIBLE) {
                    etInputUrl.setText(tvNoteUrl.getText());
                }
                etInputUrl.requestFocus();

                view.findViewById(R.id.layout_add_url_tvAdd).setOnClickListener(v -> {
                    if (etInputUrl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, getResources().getString(R.string.toast_empty_url), Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(etInputUrl.getText().toString().trim()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, getResources().getString(R.string.toast_invalid_url), Toast.LENGTH_SHORT).show();
                    } else {
                        tvNoteUrl.setText(etInputUrl.getText().toString().trim());
                        tvNoteUrl.setVisibility(View.VISIBLE);
                        llNoteUrl.setVisibility(View.VISIBLE);
                        tvAddUrl.setText(getResources().getString(R.string.change_url));
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

    private void showDeleteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                                    .deleteNote(alreadyExistNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.layout_delete_note_tvCancel).setOnClickListener(v -> {
                dialogDeleteNote.dismiss();
                dialogDeleteNote = null;
            });
        }
        dialogDeleteNote.show();
    }

    private void showUnsavedDialog() {
        if (isChanged) {
            if (dialogUnsavedNote == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
                View view = LayoutInflater.from(this).inflate(
                        R.layout.layout_unsaved_note,
                        findViewById(R.id.layout_unsaved_note_container));
                builder.setView(view);
                dialogUnsavedNote = builder.create();
                if (dialogUnsavedNote.getWindow() != null) {
                    dialogUnsavedNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                view.findViewById(R.id.layout_unsaved_note_tvLeave).setOnClickListener(v -> finish());
                view.findViewById(R.id.layout_unsaved_note_tvCancel).setOnClickListener(v -> {
                    dialogUnsavedNote.dismiss();
                    dialogUnsavedNote = null;
                });
                dialogUnsavedNote.show();
            }
        } else {
            finish();
        }
    }

    private void removeImage() {
        selectedImagePath = "";
        ivNoteImage.setVisibility(View.GONE);
        ivRemoveImage.setVisibility(View.GONE);
        tvAddImage.setText(getResources().getString(R.string.add_image));
    }

    private void removeUrl() {
        tvNoteUrl.setText("");
        tvNoteUrl.setVisibility(View.GONE);
        llNoteUrl.setVisibility(View.GONE);
        tvAddUrl.setText(getResources().getString(R.string.add_url));
    }

    @Override
    public void onBackPressed() {
        showUnsavedDialog();
    }
}