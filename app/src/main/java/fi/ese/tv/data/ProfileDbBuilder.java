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

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import fi.ese.tv.R;

/**
 * The ChannelDbBuilder is used to grab a JSON file from a server and parse the data
 * to be placed into a local database
 */
public class ProfileDbBuilder {
    public static final String TAG_PROFILES = "profiles";

    private static final String TAG = "ProfileDbBuilder";

    private Context mContext;

    /**
     * Default constructor that can be used for tests
     */
    public ProfileDbBuilder() {

    }

    public ProfileDbBuilder(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Fetches JSON data representing videos from a server and populates that in a database
     * @param url The location of the video list
     */
    public @NonNull List<ContentValues> fetch()
            throws IOException, JSONException {

        JSONObject data = fetchJSON();
        return buildMedia(data);
    }

    /**
     * Takes the contents of a JSON object and populates the database
     * @param jsonObj The JSON object of videos
     * @throws JSONException if the JSON object is invalid
     */
    public List<ContentValues> buildMedia(JSONObject jsonObj) throws JSONException {

        JSONArray profileArray = jsonObj.getJSONArray(TAG_PROFILES);
        List<ContentValues> contentValues = new ArrayList<>();

        for (int i = 0; i < profileArray.length(); i++) {
            JSONObject profile = profileArray.getJSONObject(i);
            String name = profile.getString("name");
            String username = profile.getString("username");
            String password = profile.getString("password");
            String auth = profile.getString("auth");
            String web_address = profile.getString("web_address");
            String web_port = profile.getString("web_port");
            String stream_port = profile.getString("stream_port");

            ContentValues contentValue = new ContentValues();
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_NAME, name);
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_USERNAME, username);
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_PASSWORD, password);
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_AUTH, auth);
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS, web_address);
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_WEB_PORT, web_port);
            contentValue.put(ProfileContract.ProfileEntry.COLUMN_STREAM_PORT, stream_port);

            contentValues.add(contentValue);

        }
        return contentValues;
    }

    /**
     * Fetch JSON object from a given URL.
     *
     * @return the JSONObject representation of the response
     * @throws JSONException
     * @throws IOException
     */
    private JSONObject fetchJSON() throws JSONException, IOException {

        InputStream is = mContext.getResources().openRawResource(R.raw.enigma2boxes);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        String json = writer.toString();
        return new JSONObject(json);

    }
}
