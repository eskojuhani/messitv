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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 * DatabaseProvider is a ContentProvider that provides videos, epgs for the rest of applications.
 */
public class DatabaseProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper mOpenHelper;

    // These codes are returned from sUriMatcher#match when the respective Uri matches.
    private static final int VIDEO = 1;
    private static final int VIDEO_WITH_CATEGORY = 2;
    private static final int EPG = 3;
    private static final int EPG_WITH_SREF = 4;
    private static final int PROFILE = 5;
    private static final int PROFILE_WITH_NAME = 6;
    private static final int RECORDING = 7;
    private static final int RECORDING_WITH_CATEGORY = 8;
    private static final int SEARCH_SUGGEST = 9;
    private static final int REFRESH_SHORTCUT = 10;

    private ContentResolver mContentResolver;

    private static final SQLiteQueryBuilder sVideosContainingQueryBuilder;
    private static final String[] sVideosContainingQueryColumns;

    private static final SQLiteQueryBuilder sEpgsContainingQueryBuilder;
    public static final String[] sEpgsContainingQueryColumns;

    private static final SQLiteQueryBuilder sProfilesContainingQueryBuilder;
    public static final String[] sProfilesContainingQueryColumns;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mContentResolver = context.getContentResolver();
        mOpenHelper = new DatabaseHelper(context);
        return true;
    }

    static {
        sVideosContainingQueryBuilder = new SQLiteQueryBuilder();
        sVideosContainingQueryBuilder.setTables(ChannelContract.VideoEntry.TABLE_NAME);
        sVideosContainingQueryBuilder.setProjectionMap(buildVideoColumnMap());
        sVideosContainingQueryColumns = new String[]{
                ChannelContract.VideoEntry._ID,
                ChannelContract.VideoEntry.COLUMN_NAME,
                ChannelContract.VideoEntry.COLUMN_CATEGORY,
                ChannelContract.VideoEntry.COLUMN_DESC,
                ChannelContract.VideoEntry.COLUMN_STREAM_URL,
                ChannelContract.VideoEntry.COLUMN_AUTH,
                ChannelContract.VideoEntry.COLUMN_BG_IMAGE_URL,
                ChannelContract.VideoEntry.COLUMN_CARD_IMG,
                ChannelContract.VideoEntry.COLUMN_IS_LIVE,
                ChannelContract.VideoEntry.COLUMN_IS_RECORDING,
                ChannelContract.VideoEntry.COLUMN_FILENAME,
                ChannelContract.VideoEntry.COLUMN_LENGTH,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
        sEpgsContainingQueryBuilder = new SQLiteQueryBuilder();
        sEpgsContainingQueryBuilder.setTables(EpgContract.EpgEntry.TABLE_NAME);
        sEpgsContainingQueryBuilder.setProjectionMap(buildEpgColumnMap());
        sEpgsContainingQueryColumns = new String[]{
                EpgContract.EpgEntry._ID,
                EpgContract.EpgEntry.COLUMN_SNAME,
                EpgContract.EpgEntry.COLUMN_TITLE,
                EpgContract.EpgEntry.COLUMN_BEGIN_TIMESTAMP,
                EpgContract.EpgEntry.COLUMN_NOW_TIMESTAMP,
                EpgContract.EpgEntry.COLUMN_SREF,
                EpgContract.EpgEntry.COLUMN_BREF,
                EpgContract.EpgEntry.COLUMN_GENRE,
                EpgContract.EpgEntry.COLUMN_DURATION_SEC,
                EpgContract.EpgEntry.COLUMN_SHORTDESC,
                EpgContract.EpgEntry.COLUMN_GENREID,
                EpgContract.EpgEntry.COLUMN_LONGDESC
        };
        sProfilesContainingQueryBuilder = new SQLiteQueryBuilder();
        sProfilesContainingQueryBuilder.setTables(ProfileContract.ProfileEntry.TABLE_NAME);
        sProfilesContainingQueryBuilder.setProjectionMap(buildProfileColumnMap());
        sProfilesContainingQueryColumns = new String[]{
                ProfileContract.ProfileEntry._ID,
                ProfileContract.ProfileEntry.COLUMN_NAME,
                ProfileContract.ProfileEntry.COLUMN_USERNAME,
                ProfileContract.ProfileEntry.COLUMN_PASSWORD,
                ProfileContract.ProfileEntry.COLUMN_AUTH,
                ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS,
                ProfileContract.ProfileEntry.COLUMN_WEB_PORT,
                ProfileContract.ProfileEntry.COLUMN_STREAM_PORT
        };

    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ChannelContract.CONTENT_AUTHORITY;

        // For each type of URI to add, create a corresponding code.
        matcher.addURI(authority, ChannelContract.PATH_VIDEO, VIDEO);
        matcher.addURI(authority, ChannelContract.PATH_VIDEO + "/*", VIDEO_WITH_CATEGORY);

        matcher.addURI(authority, ChannelContract.PATH_RECORDING, RECORDING);
        matcher.addURI(authority, ChannelContract.PATH_RECORDING + "/*", RECORDING_WITH_CATEGORY);

        matcher.addURI(authority, EpgContract.PATH_EPG, EPG);
        matcher.addURI(authority, EpgContract.PATH_EPG + "/*", EPG_WITH_SREF);

        matcher.addURI(authority, ProfileContract.PATH_PROFILE, PROFILE);
        matcher.addURI(authority, ProfileContract.PATH_PROFILE + "/*", PROFILE_WITH_NAME);

        // Search related URIs.
        matcher.addURI(authority, "search/" + SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(authority, "search/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
        return sVideosContainingQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                sVideosContainingQueryColumns,
                ChannelContract.VideoEntry.COLUMN_NAME + " LIKE ? OR " +
                        ChannelContract.VideoEntry.COLUMN_DESC + " LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"},
                null,
                null,
                null
        );
    }

    private static HashMap<String, String> buildVideoColumnMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put(ChannelContract.VideoEntry._ID, ChannelContract.VideoEntry._ID);
        map.put(ChannelContract.VideoEntry.COLUMN_NAME, ChannelContract.VideoEntry.COLUMN_NAME);
        map.put(ChannelContract.VideoEntry.COLUMN_DESC, ChannelContract.VideoEntry.COLUMN_DESC);
        map.put(ChannelContract.VideoEntry.COLUMN_CATEGORY, ChannelContract.VideoEntry.COLUMN_CATEGORY);
        map.put(ChannelContract.VideoEntry.COLUMN_STREAM_URL, ChannelContract.VideoEntry.COLUMN_STREAM_URL);
        map.put(ChannelContract.VideoEntry.COLUMN_AUTH, ChannelContract.VideoEntry.COLUMN_AUTH);
        map.put(ChannelContract.VideoEntry.COLUMN_BG_IMAGE_URL, ChannelContract.VideoEntry.COLUMN_BG_IMAGE_URL);
        map.put(ChannelContract.VideoEntry.COLUMN_CARD_IMG, ChannelContract.VideoEntry.COLUMN_CARD_IMG);
        map.put(ChannelContract.VideoEntry.COLUMN_IS_LIVE, ChannelContract.VideoEntry.COLUMN_IS_LIVE);
        map.put(ChannelContract.VideoEntry.COLUMN_IS_RECORDING, ChannelContract.VideoEntry.COLUMN_IS_RECORDING);
        map.put(ChannelContract.VideoEntry.COLUMN_FILENAME, ChannelContract.VideoEntry.COLUMN_FILENAME);
        map.put(ChannelContract.VideoEntry.COLUMN_LENGTH, ChannelContract.VideoEntry.COLUMN_LENGTH);
        return map;
    }

    private static HashMap<String, String> buildEpgColumnMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put(EpgContract.EpgEntry._ID, EpgContract.EpgEntry._ID);
        map.put(EpgContract.EpgEntry.COLUMN_SNAME, EpgContract.EpgEntry.COLUMN_SNAME);
        map.put(EpgContract.EpgEntry.COLUMN_TITLE, EpgContract.EpgEntry.COLUMN_TITLE);
        map.put(EpgContract.EpgEntry.COLUMN_BEGIN_TIMESTAMP, EpgContract.EpgEntry.COLUMN_BEGIN_TIMESTAMP);
        map.put(EpgContract.EpgEntry.COLUMN_NOW_TIMESTAMP, EpgContract.EpgEntry.COLUMN_NOW_TIMESTAMP);
        map.put(EpgContract.EpgEntry.COLUMN_SREF, EpgContract.EpgEntry.COLUMN_SREF);
        map.put(EpgContract.EpgEntry.COLUMN_BREF, EpgContract.EpgEntry.COLUMN_BREF);
        map.put(EpgContract.EpgEntry.COLUMN_GENRE, EpgContract.EpgEntry.COLUMN_GENRE);
        map.put(EpgContract.EpgEntry.COLUMN_DURATION_SEC, EpgContract.EpgEntry.COLUMN_DURATION_SEC);
        map.put(EpgContract.EpgEntry.COLUMN_SHORTDESC, EpgContract.EpgEntry.COLUMN_SHORTDESC);
        map.put(EpgContract.EpgEntry.COLUMN_GENREID, EpgContract.EpgEntry.COLUMN_GENREID);
        map.put(EpgContract.EpgEntry.COLUMN_LONGDESC, EpgContract.EpgEntry.COLUMN_LONGDESC);
        return map;
    }

    private static HashMap<String, String> buildProfileColumnMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put(ProfileContract.ProfileEntry._ID, ProfileContract.ProfileEntry._ID);
        map.put(ProfileContract.ProfileEntry.COLUMN_NAME, ProfileContract.ProfileEntry.COLUMN_NAME);
        map.put(ProfileContract.ProfileEntry.COLUMN_USERNAME, ProfileContract.ProfileEntry.COLUMN_USERNAME);
        map.put(ProfileContract.ProfileEntry.COLUMN_PASSWORD, ProfileContract.ProfileEntry.COLUMN_PASSWORD);
        map.put(ProfileContract.ProfileEntry.COLUMN_AUTH, ProfileContract.ProfileEntry.COLUMN_AUTH);
        map.put(ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS, ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS);
        map.put(ProfileContract.ProfileEntry.COLUMN_WEB_PORT, ProfileContract.ProfileEntry.COLUMN_WEB_PORT);
        map.put(ProfileContract.ProfileEntry.COLUMN_STREAM_PORT, ProfileContract.ProfileEntry.COLUMN_STREAM_PORT);
        return map;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case SEARCH_SUGGEST: {
                String rawQuery = "";
                if (selectionArgs != null && selectionArgs.length > 0) {
                    rawQuery = selectionArgs[0];
                }
                retCursor = getSuggestions(rawQuery);
                break;
            }
            case RECORDING:
            case VIDEO: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ChannelContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case EPG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EpgContract.EpgEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PROFILE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ProfileContract.ProfileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        retCursor.setNotificationUri(mContentResolver, uri);
        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            // The application is querying the db for its own contents.
            case VIDEO_WITH_CATEGORY:
                return ChannelContract.VideoEntry.CONTENT_TYPE;
            case VIDEO:
                return ChannelContract.VideoEntry.CONTENT_TYPE;

            case RECORDING_WITH_CATEGORY:
                return ChannelContract.VideoEntry.CONTENT_TYPE_REC;
            case RECORDING:
                return ChannelContract.VideoEntry.CONTENT_TYPE_REC;

            case EPG_WITH_SREF:
                return EpgContract.EpgEntry.CONTENT_TYPE;
            case EPG:
                return EpgContract.EpgEntry.CONTENT_TYPE;

            case PROFILE_WITH_NAME:
                return ProfileContract.ProfileEntry.CONTENT_TYPE;
            case PROFILE:
                return ProfileContract.ProfileEntry.CONTENT_TYPE;

            // The Android TV global search is querying our app for relevant content.
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;

            // We aren't sure what is being asked of us.
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final Uri returnUri;
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case RECORDING:
            case VIDEO: {
                long _id = mOpenHelper.getWritableDatabase().insert(
                        ChannelContract.VideoEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ChannelContract.VideoEntry.buildVideoUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case EPG: {
                long _id = mOpenHelper.getWritableDatabase().insert(
                        EpgContract.EpgEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = EpgContract.EpgEntry.buildEpgUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PROFILE: {
                long _id = mOpenHelper.getWritableDatabase().insert(
                        ProfileContract.ProfileEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ProfileContract.ProfileEntry.buildProfileUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        mContentResolver.notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final int rowsDeleted;

        if (selection == null) {
            throw new UnsupportedOperationException("Cannot delete without selection specified.");
        }

        switch (sUriMatcher.match(uri)) {
            case RECORDING:
            case VIDEO: {
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        ChannelContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case EPG: {
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        EpgContract.EpgEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PROFILE: {
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        ProfileContract.ProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (rowsDeleted != 0) {
            mContentResolver.notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case RECORDING:
            case VIDEO: {
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        ChannelContract.VideoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case EPG: {
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        EpgContract.EpgEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PROFILE: {
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        ProfileContract.ProfileEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (rowsUpdated != 0) {
            mContentResolver.notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
            case RECORDING:
            case VIDEO: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int returnCount = 0;

                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(ChannelContract.VideoEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                mContentResolver.notifyChange(uri, null);
                return returnCount;
            }
            case EPG: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int returnCount = 0;

                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(EpgContract.EpgEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                mContentResolver.notifyChange(uri, null);
                return returnCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }
}
