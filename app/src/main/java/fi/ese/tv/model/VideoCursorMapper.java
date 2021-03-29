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

import fi.ese.tv.data.ChannelContract;

/**
 * VideoCursorMapper maps a database Cursor to a Video object.
 */
public final class VideoCursorMapper extends CursorMapper {

    private static int idIndex;
    private static int nameIndex;
    private static int descIndex;
    private static int authIndex;
    private static int videoUrlIndex;
    private static int bgImageUrlIndex;
    private static int cardImageUrlIndex;
    private static int categoryIndex;
    private static int isLiveIndex;
    private static int isRecordingIndex;
    private static int filenameIndex;
    private static int lengthIndex;

    @Override
    protected void bindColumns(Cursor cursor) {
        idIndex = cursor.getColumnIndex(ChannelContract.VideoEntry._ID);
        nameIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_NAME);
        descIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_DESC);
        authIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_AUTH);
        videoUrlIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_STREAM_URL);
        bgImageUrlIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_BG_IMAGE_URL);
        cardImageUrlIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_CARD_IMG);
        categoryIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_CATEGORY);
        isLiveIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_IS_LIVE);
        isRecordingIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_IS_RECORDING);
        filenameIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_FILENAME);
        lengthIndex = cursor.getColumnIndex(ChannelContract.VideoEntry.COLUMN_LENGTH);
    }

    @Override
    protected Object bind(Cursor cursor) {

        // Get the values of the video.
        long id = cursor.getLong(idIndex);
        String category = cursor.getString(categoryIndex);
        String title = cursor.getString(nameIndex);
        String desc = cursor.getString(descIndex);
        String auth = cursor.getString(authIndex);
        String videoUrl = cursor.getString(videoUrlIndex);
        String bgImageUrl = cursor.getString(bgImageUrlIndex);
        String cardImageUrl = cursor.getString(cardImageUrlIndex);
        Boolean isLive = cursor.getInt(isLiveIndex) == 1 ? true : false;
        Boolean isRecording = cursor.getInt(isRecordingIndex) == 1 ? true : false;
        String filename = cursor.getString(filenameIndex);
        String length = cursor.getString(lengthIndex);

        // Build a Video object to be processed.
        return new Video.VideoBuilder()
                .id(id)
                .title(title)
                .category(category)
                .authorization(auth)
                .description(desc)
                .videoUrl(videoUrl)
                .bgImageUrl(bgImageUrl)
                .cardImageUrl(cardImageUrl)
                .isLive(isLive)
                .isRecording(isRecording)
                .filename(filename)
                .length(length)
                .build();
    }
}
