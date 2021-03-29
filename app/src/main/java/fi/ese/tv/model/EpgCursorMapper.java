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

package fi.ese.tv.model;

import android.database.Cursor;

import androidx.leanback.database.CursorMapper;

import fi.ese.tv.data.EpgContract;

/**
 * EpgCursorMapper maps a database Cursor to an Epg object.
 */
public final class EpgCursorMapper extends CursorMapper {

    private static int idIndex;
    private static int snameIndex;
    private static int titleIndex;
    private static int beginTimestampIndex;
    private static int nowTimestampIndex;
    private static int srefIndex;
    private static int genreIndex;
    private static int durationIndex;
    private static int shortdescIndex;
    private static int genreidIndex;
    private static int longdescIndex;

    @Override
    protected void bindColumns(Cursor cursor) {
        idIndex = cursor.getColumnIndex(EpgContract.EpgEntry._ID);
        snameIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_SNAME);
        titleIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_TITLE);
        beginTimestampIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_BEGIN_TIMESTAMP);
        nowTimestampIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_NOW_TIMESTAMP);
        srefIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_SREF);
        genreIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_DURATION_SEC);
        durationIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_DURATION_SEC);
        shortdescIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_SHORTDESC);
        genreidIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_GENREID);
        longdescIndex = cursor.getColumnIndex(EpgContract.EpgEntry.COLUMN_LONGDESC);
    }

    @Override
    protected Object bind(Cursor cursor) {

        // Get the values of the video.
        long id = cursor.getLong(idIndex);
        String sname = cursor.getString(snameIndex);
        String title = cursor.getString(titleIndex);
        long begin_timestamp = cursor.getLong(beginTimestampIndex);
        long now_timestamp = cursor.getLong(nowTimestampIndex);
        String sref = cursor.getString(srefIndex);
        String genre = cursor.getString(genreidIndex);
        long duration_sec = cursor.getLong(durationIndex);
        String shortdesc = cursor.getString(shortdescIndex);
        long genreid = cursor.getLong(genreidIndex);
        String longdesc = cursor.getString(longdescIndex);

        // Build an Epg object to be processed.
        return new Epg.EpgBuilder()
                .id(id)
                .title(title)
                .sname(sname)
                .begin_timestamp(begin_timestamp)
                .now_timestamp(now_timestamp)
                .sref(sref)
                .genre(genre)
                .duration_sec(duration_sec)
                .shortdesc(shortdesc)
                .genreid(genreid)
                .longdesc(longdesc)
                .build();
    }
}
