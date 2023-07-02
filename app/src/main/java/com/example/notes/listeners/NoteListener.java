package com.example.notes.listeners;

import com.example.notes.entities.Note;

public interface NoteListener {
    void onNoteClicked(Note note, int position);
    void onNoteLongClicked(Note note, int position);
    void onMoreClicked(Note note, int position);
}
