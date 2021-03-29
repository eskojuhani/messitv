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
 * ChannelContract represents the contract for storing videos in the SQLite database.
 */
public final class ChannelContract {

    // The name for the entire content provider.
    public static final String CONTENT_AUTHORITY = "fi.ese.tv";

    // Base of all URIs that will be used to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The content paths.
    public static final String PATH_VIDEO = "channel";
    public static final String PATH_RECORDING = "recording";

    public static final class VideoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();
        public static final Uri CONTENT_URI_REC =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECORDING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_VIDEO;

        public static final String CONTENT_TYPE_REC =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_RECORDING;

        // Name of the video table.
        public static final String TABLE_NAME = "channel";

        // Column with the foreign key into the category table.
        public static final String COLUMN_CATEGORY = "category";

        // Name of the video.
        public static final String COLUMN_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;

        // Description of the video.
        public static final String COLUMN_DESC = SearchManager.SUGGEST_COLUMN_TEXT_2;

        // The url to the video content.
        public static final String COLUMN_STREAM_URL = "stream_url";

        // The auth to the server.
        public static final String COLUMN_AUTH = "auth";

        // The url to the background image.
        public static final String COLUMN_BG_IMAGE_URL = "bg_image_url";

        // The card image for the video.
        public static final String COLUMN_CARD_IMG = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;

        // Whether the video is live or not.
        public static final String COLUMN_IS_LIVE = SearchManager.SUGGEST_COLUMN_IS_LIVE;

        public static final String COLUMN_IS_RECORDING = "is_recording";
        public static final String COLUMN_FILENAME = "filename";
        public static final String COLUMN_LENGTH = "length";

        // Returns the Uri referencing a video with the specified id.
        public static Uri buildVideoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
