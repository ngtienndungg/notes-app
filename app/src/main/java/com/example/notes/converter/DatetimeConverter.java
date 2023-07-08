package com.example.notes.converter;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatetimeConverter {
    @TypeConverter
    public static Date toDatetime(String stringDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault());
            Date originalDatetime = originalFormat.parse(stringDate);
            assert originalDatetime != null;
            String targetDatetime = targetFormat.format(originalDatetime);
            return targetFormat.parse(targetDatetime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @TypeConverter
    public static String toString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        return formatter.format(date);
    }
}
