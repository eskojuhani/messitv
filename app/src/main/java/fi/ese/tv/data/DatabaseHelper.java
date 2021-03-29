/*
 * Copyright (c) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.ese.tv.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import fi.ese.tv.data.ChannelContract.VideoEntry;

/**
 * DatabaseHelper manages the creation and upgrade of the database used in this sample.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Change this when you change the database schema.
    private static final int DATABASE_VERSION = 1;

    // The name of our database.
    private static final String DATABASE_NAME = "leanback.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void createVideo(SQLiteDatabase db) {
        // Create a table to hold videos.
        final String createTable = "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                VideoEntry._ID + " INTEGER PRIMARY KEY," +
                VideoEntry.COLUMN_CATEGORY + " TEXT, " +
                VideoEntry.COLUMN_STREAM_URL + " TEXT, " + // Make the URL unique.
                VideoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_DESC + " TEXT, " +
                VideoEntry.COLUMN_AUTH + " TEXT, " +
                VideoEntry.COLUMN_BG_IMAGE_URL + " TEXT, " +
                VideoEntry.COLUMN_CARD_IMG + " TEXT, " +
                VideoEntry.COLUMN_IS_LIVE + " INTEGER DEFAULT 0, " +
                VideoEntry.COLUMN_IS_RECORDING + " INTEGER DEFAULT 0, " +
                VideoEntry.COLUMN_FILENAME + " TEXT, " +
                VideoEntry.COLUMN_LENGTH + " TEXT " +
                " );";

        // Do the creating of the databases.
        db.execSQL(createTable);
    }

    public void createEpg(SQLiteDatabase db) {
        // Create a table to hold videos.
        final String createTable = "CREATE TABLE " + EpgContract.EpgEntry.TABLE_NAME + " (" +
                EpgContract.EpgEntry._ID + " INTEGER PRIMARY KEY," +
                EpgContract.EpgEntry.COLUMN_SNAME + " TEXT, " +
                EpgContract.EpgEntry.COLUMN_TITLE + " TEXT, " +
                EpgContract.EpgEntry.COLUMN_BEGIN_TIMESTAMP + " INTEGER NOT NULL, " +
                EpgContract.EpgEntry.COLUMN_NOW_TIMESTAMP + " INTEGER, " +
                EpgContract.EpgEntry.COLUMN_SREF + " TEXT, " +
                EpgContract.EpgEntry.COLUMN_BREF + " TEXT, " +
                EpgContract.EpgEntry.COLUMN_GENRE + " TEXT, " +
                EpgContract.EpgEntry.COLUMN_DURATION_SEC + " INTEGER, " +
                EpgContract.EpgEntry.COLUMN_SHORTDESC + " TEXT, " +
                EpgContract.EpgEntry.COLUMN_GENREID + " INTEGER, " +
                EpgContract.EpgEntry.COLUMN_LONGDESC + " TEXT " +
                " );";

        // Do the creating of the databases.
        db.execSQL(createTable);
    }

    public void createProfile(SQLiteDatabase db) {
        // Create a table to hold videos.
        final String createTable = "CREATE TABLE " + ProfileContract.ProfileEntry.TABLE_NAME + " (" +
                ProfileContract.ProfileEntry._ID + " INTEGER PRIMARY KEY," +
                ProfileContract.ProfileEntry.COLUMN_NAME + " TEXT, " +
                ProfileContract.ProfileEntry.COLUMN_USERNAME + " TEXT, " +
                ProfileContract.ProfileEntry.COLUMN_PASSWORD + " TEXT, " +
                ProfileContract.ProfileEntry.COLUMN_AUTH + " TEXT, " +
                ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS + " TEXT, " +
                ProfileContract.ProfileEntry.COLUMN_WEB_PORT + " INTEGER NOT NULL, " +
                ProfileContract.ProfileEntry.COLUMN_STREAM_PORT + " INTEGER NOT NULL" +
                " );";

        // Do the creating of the databases.
        db.execSQL(createTable);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createVideo(db);
        createEpg(db);
        createProfile(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simply discard all old data and start over when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EpgContract.EpgEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileContract.ProfileEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do the same thing as upgrading...
        onUpgrade(db, oldVersion, newVersion);
    }
}
