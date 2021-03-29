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
 * FetchProfileService is responsible for fetching the profiles from the raw json file and inserting the
 * results into a local SQLite database.
 */
//public class FetchChannelService extends JobIntentService {
//    private static final String TAG = "FetchEpgService";
public class FetchProfileService extends IntentService {
    private static final String TAG = "FetchProfileService";

    /**
     * Creates an IntentService with a default name for the worker thread.
     */
    public FetchProfileService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
      ProfileDbBuilder builder = new ProfileDbBuilder(getApplicationContext());

        try {
            getApplicationContext().getContentResolver().delete(ProfileContract.ProfileEntry.CONTENT_URI,
                    ProfileContract.ProfileEntry.COLUMN_AUTH+ " = ?", new String[] { "Basic cm9vdDowc2thcmkhMQ=="});

            List<ContentValues> contentValuesList =
                    builder.fetch();
            ContentValues[] downloadedValues =
                    contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
            getApplicationContext().getContentResolver().bulkInsert(ProfileContract.ProfileEntry.CONTENT_URI,
                    downloadedValues);

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error occurred in reading profiles");
            e.printStackTrace();
        }
    }
}
