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

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * FetchEpgService is responsible for fetching the epg from the Internet and inserting the
 * results into a local SQLite database.
 */
//public class FetchChannelService extends JobIntentService {
//    private static final String TAG = "FetchEpgService";
public class FetchEpgService extends IntentService {
    private static final String TAG = "FetchEpgService";

    /**
     * Creates an IntentService with a default name for the worker thread.
     */
    public FetchEpgService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
      EpgDbBuilder builder = new EpgDbBuilder(getApplicationContext());

        try {
            String auth = workIntent.getStringExtra("auth");
            String url = workIntent.getStringExtra("download_url");
            String base = workIntent.getStringExtra("base");
            String bref = workIntent.getStringExtra("bref");

            getApplicationContext().getContentResolver().delete(EpgContract.EpgEntry.CONTENT_URI,
                    EpgContract.EpgEntry.COLUMN_BREF + " = ?", new String[] {bref});

            List<ContentValues> contentValuesList =
                    builder.fetch(auth, url, base, bref);
            ContentValues[] downloadedVideoContentValues =
                    contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
            getApplicationContext().getContentResolver().bulkInsert(EpgContract.EpgEntry.CONTENT_URI,
                    downloadedVideoContentValues);

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error occurred in downloading videos");
            e.printStackTrace();
        }
    }
}
