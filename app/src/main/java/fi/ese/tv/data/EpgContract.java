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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * EpgContract represents the contract for storing videos in the SQLite database.
 */
public final class EpgContract {

    // The name for the entire content provider.
    public static final String CONTENT_AUTHORITY = "fi.ese.tv";

    // Base of all URIs that will be used to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The content paths.
    public static final String PATH_EPG = "epg";

    public static final class EpgEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EPG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_EPG;

        // Name of the video table.
        public static final String TABLE_NAME = "epg";

        // Column with the foreign key into the category table.
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_SNAME = "sname";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BEGIN_TIMESTAMP = "begin_timestamp";
        public static final String COLUMN_NOW_TIMESTAMP = "now_timestamp";
        public static final String COLUMN_SREF = "sref";
        public static final String COLUMN_BREF = "bref";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_DURATION_SEC = "duration_sec";
        public static final String COLUMN_SHORTDESC = "shortdesc";
        public static final String COLUMN_GENREID = "genreid";
        public static final String COLUMN_LONGDESC = "longdesc";

        // Returns the Uri referencing a video with the specified id.
        public static Uri buildEpgUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
