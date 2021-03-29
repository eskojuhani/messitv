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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.net.Uri.decode;

/**
 * The ChannelDbBuilder is used to grab a JSON file from a server and parse the data
 * to be placed into a local database
 */
public class RecordingDbBuilder {
    public static final String TAG_MOVIES = "movies";
    private static final String TAG = "RecordingDbBuilder";

    private Context mContext;

    /**
     * Default constructor that can be used for tests
     */
    public RecordingDbBuilder() {

    }

    public RecordingDbBuilder(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Fetches JSON data representing videos from a server and populates that in a database
     * @param url The location of the video list
     */
    public @NonNull List<ContentValues> fetch(String auth, String url, String base)
            throws IOException, JSONException {

        JSONObject videoData = fetchJSON(auth, url);
        return buildMedia(videoData, auth, url, base);
    }

    /**
     * Takes the contents of a JSON object and populates the database
     * @param jsonObj The JSON object of videos
     * @throws JSONException if the JSON object is invalid
     */
    public List<ContentValues> buildMedia(JSONObject jsonObj, String auth, String url, String base) throws JSONException {

        JSONArray movieArray = jsonObj.getJSONArray(TAG_MOVIES);
        List<ContentValues> contentValues = new ArrayList<>();

        for (int i = 0; i < movieArray.length(); i++) {

            JSONObject movie = movieArray.getJSONObject(i);
            String eventname = decode(movie.getString("eventname"));
            String description = movie.getString("description");
            String descriptionExtended = movie.getString("descriptionExtended");
            String filename = movie.getString("filename");
            String serviceref = movie.getString("serviceref");
            String length = movie.getString("length");
            String videoUrl = url + "file?file=";
            try {
                videoUrl += URLEncoder.encode(filename, "UTF-8");
            }
            catch(Exception e) {
                Log.d(TAG, "Error encoding: " + e.getLocalizedMessage());
            }

            ContentValues contentValue = new ContentValues();
            contentValue.put(ChannelContract.VideoEntry.COLUMN_CATEGORY, "Recording");
            contentValue.put(ChannelContract.VideoEntry.COLUMN_NAME, eventname);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_DESC, (description.length() == 0) ? descriptionExtended : description);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_FILENAME, filename);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_LENGTH, length);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_STREAM_URL, videoUrl);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_AUTH, auth);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_IS_LIVE, false);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_IS_RECORDING, true);
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
    private JSONObject fetchJSON(String auth, String urlString) throws JSONException, IOException {
        BufferedReader reader = null;
        java.net.URL url = new java.net.URL(urlString + "api/movielist");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            urlConnection.setRequestProperty("Authorization", auth);

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } finally {
            urlConnection.disconnect();
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "JSON feed closed", e);
                }
            }
        }
    }
}
