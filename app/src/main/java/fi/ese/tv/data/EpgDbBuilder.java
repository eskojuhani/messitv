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
import android.media.Rating;
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

import javax.net.ssl.HttpsURLConnection;

import fi.ese.tv.R;

/**
 * The EpgDbBuilder is used to grab a JSON file from a server and parse the data
 * to be placed into a local database
 */
public class EpgDbBuilder {
    public static final String TAG_EVENTS = "events";
    public static final String TAG_ID = "id";
    public static final String TAG_SNAME = "sname";
    public static final String TAG_TITLE = "title";
    public static final String TAG_BEGIN_TIMESTAMP = "begin_timestamp";
    public static final String TAG_NOW_TIMESTAMP = "now_timestamp";
    public static final String TAG_SREF = "sref";
    public static final String TAG_GENRE = "genre";
    public static final String TAG_DURATION_SEC = "duration_sec";
    public static final String TAG_SHORTDESC = "shortdesc";
    public static final String TAG_GENREID = "genreid";
    public static final String TAG_LONGDESC = "longdesc";

    private static final String TAG = "EpgDbBuilder";

    private Context mContext;

    /**
     * Default constructor that can be used for tests
     */
    public EpgDbBuilder() {

    }

    public EpgDbBuilder(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Fetches JSON data representing epgs from a server and populates that in a database
     * @param url The location of the epg list
     */
    public @NonNull List<ContentValues> fetch(String auth, String url, String base, String bRef)
            throws IOException, JSONException {

        JSONObject videoData = fetchJSON(auth, url, bRef);
        return buildMedia(videoData, base, bRef);
    }

    /**
     * Takes the contents of a JSON object and populates the database
     * @param jsonObj The JSON object of videos
     * @throws JSONException if the JSON object is invalid
     */
    public List<ContentValues> buildMedia(JSONObject jsonObj, String base, String bRef) throws JSONException {

        JSONArray eventArray = jsonObj.getJSONArray(TAG_EVENTS);
        List<ContentValues> contentValues = new ArrayList<>();

        for (int i = 0; i < eventArray.length(); i++) {
            JSONObject epg = eventArray.getJSONObject(i);

            //Long id = epg.optLong(TAG_ID);
            String sname = epg.optString(TAG_SNAME);
            String title = epg.optString(TAG_TITLE);
            Long begin_timestamp = epg.optLong(TAG_BEGIN_TIMESTAMP);
            Long now_timestamp = epg.optLong(TAG_NOW_TIMESTAMP);
            String sref = epg.optString(TAG_SREF);
            String genre = epg.optString(TAG_GENRE);
            Long duration_Sec = epg.optLong(TAG_DURATION_SEC);
            String shortdesc = epg.optString(TAG_SHORTDESC);
            Long genreid = epg.optLong(TAG_GENREID);
            String longdesc = epg.optString(TAG_LONGDESC);

            ContentValues epgValues = new ContentValues();
            //epgValues.put(EpgContract.EpgEntry.COLUMN_ID, id);
            epgValues.put(EpgContract.EpgEntry.COLUMN_SNAME, sname);
            epgValues.put(EpgContract.EpgEntry.COLUMN_TITLE, title);
            epgValues.put(EpgContract.EpgEntry.COLUMN_BEGIN_TIMESTAMP, begin_timestamp);
            epgValues.put(EpgContract.EpgEntry.COLUMN_NOW_TIMESTAMP, now_timestamp);
            epgValues.put(EpgContract.EpgEntry.COLUMN_SREF, base + sref);
            epgValues.put(EpgContract.EpgEntry.COLUMN_BREF, bRef);
            epgValues.put(EpgContract.EpgEntry.COLUMN_GENRE, genre);
            epgValues.put(EpgContract.EpgEntry.COLUMN_DURATION_SEC, duration_Sec);
            epgValues.put(EpgContract.EpgEntry.COLUMN_SHORTDESC, shortdesc);
            epgValues.put(EpgContract.EpgEntry.COLUMN_GENREID, genreid);
            epgValues.put(EpgContract.EpgEntry.COLUMN_LONGDESC, longdesc);

            contentValues.add(epgValues);
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
    private JSONObject fetchJSON(String auth, String urlString, String bRef) throws JSONException, IOException {
        BufferedReader reader = null;
        String queryString = "";

        if (bRef != null)
            queryString = URLEncoder.encode(bRef, "UTF-8");

        java.net.URL url = new java.net.URL(urlString + "api/epgnow?bRef=" + queryString);
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
