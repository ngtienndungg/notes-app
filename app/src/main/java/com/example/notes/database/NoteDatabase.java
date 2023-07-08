package com.example.notes.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.notes.converter.DatetimeConverter;
import com.example.notes.dao.NoteDao;
import com.example.notes.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
@TypeConverters({DatetimeConverter.class})
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase noteDatabase;

    public static synchronized NoteDatabase getNoteDatabase(Context context) {
        if (noteDatabase == null) {
            noteDatabase = Room.databaseBuilder(context, NoteDatabase.class, "note_db")
                    .build();
        }
        return noteDatabase;
    }

    public abstract NoteDao noteDao();
}

