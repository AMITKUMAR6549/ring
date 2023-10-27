package com.example.ring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AlarmDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 1;

    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the "alarms" table
        db.execSQL(AlarmContract.AlarmEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
        db.execSQL("DROP TABLE IF EXISTS " + AlarmContract.AlarmEntry.TABLE_NAME);
        onCreate(db);
    }

    public void insertAlarm(int hour, int minute, boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AlarmContract.AlarmEntry.COLUMN_HOUR, hour);
        values.put(AlarmContract.AlarmEntry.COLUMN_MINUTE, minute);
        values.put(AlarmContract.AlarmEntry.COLUMN_ENABLED, enabled ? 1 : 0);
        db.insert(AlarmContract.AlarmEntry.TABLE_NAME, null, values);
        db.close();
    }

    public void deleteAlarm(int hour, int minute) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AlarmContract.AlarmEntry.TABLE_NAME,
                AlarmContract.AlarmEntry.COLUMN_HOUR + " = ? AND " +
                        AlarmContract.AlarmEntry.COLUMN_MINUTE + " = ?",
                new String[]{String.valueOf(hour), String.valueOf(minute)});
        db.close();
    }

    public void updateAlarmState(int hour, int minute, boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AlarmContract.AlarmEntry.COLUMN_ENABLED, enabled ? 1 : 0);

        db.update(AlarmContract.AlarmEntry.TABLE_NAME, values,
                AlarmContract.AlarmEntry.COLUMN_HOUR + " = ? AND " +
                        AlarmContract.AlarmEntry.COLUMN_MINUTE + " = ?",
                new String[]{String.valueOf(hour), String.valueOf(minute)});
        db.close();
    }

    public boolean hasAlarm(int hour, int minute) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = AlarmContract.AlarmEntry.COLUMN_HOUR + " = ? AND " +
                AlarmContract.AlarmEntry.COLUMN_MINUTE + " = ?";
        String[] selectionArgs = {String.valueOf(hour), String.valueOf(minute)};
        Cursor cursor = db.query(
                AlarmContract.AlarmEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean alarmExists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return alarmExists;
    }

    public void insertOrUpdateAlarm(int hour, int minute) {
        if (hasAlarm(hour, minute)) {
            updateAlarmState(hour, minute, true);
        } else {
            insertAlarm(hour, minute, true);
        }
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + AlarmContract.AlarmEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmEntry._ID)));
                alarm.setHour(cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_HOUR)));
                alarm.setMinute(cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_MINUTE)));
                alarm.setEnabled(cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_ENABLED)) == 1);
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return alarms;
    }
}
