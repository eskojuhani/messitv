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
import java.util.ArrayList;
import java.util.List;

/**
 * The ChannelDbBuilder is used to grab a JSON file from a server and parse the data
 * to be placed into a local database
 */
public class ChannelDbBuilder {
    public static final String TAG_MEDIA = "subservices";
    public static final String TAG_GOOGLE_VIDEOS = "services";
    public static final String TAG_CATEGORY = "servicereference";
    public static final String TAG_STUDIO = "pos";
    public static final String TAG_SOURCES = "servicereference";
    public static final String TAG_TITLE = "servicename";

    private static final String TAG = "ChannelDbBuilder";

    private Context mContext;

    /**
     * Default constructor that can be used for tests
     */
    public ChannelDbBuilder() {

    }

    public ChannelDbBuilder(Context mContext) {
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

        JSONArray categoryArray = jsonObj.getJSONArray(TAG_GOOGLE_VIDEOS);
        List<ContentValues> contentValues = new ArrayList<>();

        for (int i = 0; i < categoryArray.length(); i++) {
            JSONArray videoArray;

            JSONObject category = categoryArray.getJSONObject(i);
            String categoryName = category.getString(TAG_TITLE);
            String servicereferenceName = category.getString(TAG_CATEGORY);
            videoArray = category.getJSONArray(TAG_MEDIA);

            ContentValues contentValue = new ContentValues();
            contentValue.put(ChannelContract.VideoEntry.COLUMN_CATEGORY, categoryName);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_NAME, "EPG");
            contentValue.put(ChannelContract.VideoEntry.COLUMN_DESC, "Program info for " + categoryName);
            contentValue.put(ChannelContract.VideoEntry.COLUMN_STREAM_URL, servicereferenceName);
            contentValues.add(contentValue);

            for (int j = 0; j < videoArray.length(); j++) {
                JSONObject video = videoArray.getJSONObject(j);

                String temp = video.optString(TAG_SOURCES).replaceAll(".$", "");
                String imageRef = temp.replaceAll(":", "_");
                String imageUrl = url + "picon/" + imageRef + ".png";
                String title = video.optString(TAG_TITLE);
                String description = title;
                String videoUrl = base + video.optString(TAG_SOURCES);
                String bgImageUrl = imageUrl;
                String cardImageUrl = imageUrl;

                /*ContentValues*/ contentValue = new ContentValues();
                contentValue.put(ChannelContract.VideoEntry.COLUMN_CATEGORY, categoryName);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_NAME, title);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_DESC, description);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_STREAM_URL, videoUrl);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_CARD_IMG, cardImageUrl);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_BG_IMAGE_URL, bgImageUrl);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_AUTH, auth);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_IS_LIVE, true);
                contentValue.put(ChannelContract.VideoEntry.COLUMN_IS_RECORDING, false);

                // Fixed defaults.
                contentValue.put(ChannelContract.VideoEntry.COLUMN_IS_LIVE, true);

                contentValues.add(contentValue);
            }
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
        java.net.URL url = new java.net.URL(urlString + "api/getallservices");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Log.d("ESETV", "fetch services: " + urlString);
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
