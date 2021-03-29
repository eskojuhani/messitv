/*
 * Copyright (c) 2014 The Android Open Source Project
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

package fi.ese.tv.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import fi.ese.tv.R;
import fi.ese.tv.data.ChannelContract;
import fi.ese.tv.data.DatabaseProvider;
import fi.ese.tv.data.FetchChannelService;
import fi.ese.tv.data.FetchProfileService;
import fi.ese.tv.data.FetchRecordService;
import fi.ese.tv.data.ProfileContract;
import fi.ese.tv.model.GridItem;
import fi.ese.tv.model.MainHeaderItem;
import fi.ese.tv.model.PreviousChannelsViewModel;
import fi.ese.tv.model.SingletonNameViewModelFactory;
import fi.ese.tv.model.Video;
import fi.ese.tv.model.VideoCursorMapper;
import fi.ese.tv.presenter.CardPresenter;
import fi.ese.tv.presenter.GridItemPresenter;
import fi.ese.tv.presenter.IconHeaderItemPresenter;
import fi.ese.tv.recommendation.UpdateRecommendationsService;

import java.util.HashMap;
import java.util.Map;

/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class MainFragment extends BrowseSupportFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RSS_JOB_ID = 1000;

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mCategoryRowAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Runnable mBackgroundTask;
    private Uri mBackgroundURI;
    private String mBackgroundAuth;
    private BackgroundManager mBackgroundManager;
    private LoaderManager mLoaderManager;
    private static final int CATEGORY_LOADER = 123; // Unique ID for Category Loader.
    private String mSelectedHeaderName = null;
    private long mSelectedRowId = -1;

    // Maps a Loader Id to its CursorObjectAdapter.
    private Map<Integer, CursorObjectAdapter> mVideoCursorAdapters;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("ESETV", "MainFragment.onAttach called");
        // Create a list to contain all the CursorObjectAdapters.
        // Each adapter is used to render a specific row of videos in the MainFragment.
        mVideoCursorAdapters = new HashMap<>();

        // Start loading the categories from the database.
        mLoaderManager = LoaderManager.getInstance(this);
        mLoaderManager.initLoader(CATEGORY_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ESETV", "onResume called.");
        loadProfiles();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Final initialization, modifying UI elements.
        super.onActivityCreated(savedInstanceState);

        // Prepare the manager that maintains the same background image between activities.
        prepareBackgroundManager();

        setupUIElements();
        setupEventListeners();
        prepareEntranceTransition();

        // Map category results from the database to ListRow objects.
        // This Adapter is used to render the MainFragment sidebar labels.
        mCategoryRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mCategoryRowAdapter);

        //loadProfiles();
        //updateRecommendations();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mBackgroundTask);
        mBackgroundManager = null;
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mBackgroundManager.release();
        super.onStop();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mBackgroundTask = new UpdateBackgroundTask();
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.filmi_banner, null));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // Set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.fastlane_background));

        // Set search icon color.
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private void updateBackground(String uri, String auth) {

        //Log.d("ESETV", "updateBackgroud called.");

        /*
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(mDefaultBackground);

        GlideUrl glideUrl = new GlideUrl(uri,
            new LazyHeaders.Builder()
                .addHeader("Authorization", auth)
                .build());

        Glide.with(this)
            .asBitmap()
            .load(glideUrl)
            .apply(options)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    // Intentionally empty, this can be optionally implemented by subclasses.
                    Log
                    .d("ESETV", "Picon load failed:" + uri.substring(uri.lastIndexOf("/")));
                }
                @Override
                public void onResourceReady(
                        Bitmap resource,
                        Transition<? super Bitmap> transition) {
                    mBackgroundManager.setColor(Color.BLACK);
                    mBackgroundManager.setBitmap(resource);
                }
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) { }
            });

         */
    }

    private void startBackgroundTimer() {
        mHandler.removeCallbacks(mBackgroundTask);
        mHandler.postDelayed(mBackgroundTask, BACKGROUND_UPDATE_DELAY);
    }

    private void updateRecommendations() {
        Intent recommendationIntent = new Intent(getActivity(), UpdateRecommendationsService.class);
        getActivity().startService(recommendationIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CATEGORY_LOADER) {
            Log.d("ESETV", "Loader onCreateLoader get ALL Categories");
            return new CursorLoader(
                    getContext(),
                    ChannelContract.VideoEntry.CONTENT_URI, // Table to query
                    new String[]{"DISTINCT " + ChannelContract.VideoEntry.COLUMN_CATEGORY},
                    // Only categories
                    null, // No selection clause
                    null, // No selection arguments
                    null  // Default sort order
            );
        } else {
            // Assume it is for a video.
            String category = args.getString(ChannelContract.VideoEntry.COLUMN_CATEGORY);
            Log.d("ESETV", "Loader onCreateLoader get category:" + category);

            // This just creates a CursorLoader that gets all videos.
            return new CursorLoader(
                    getContext(),
                    ChannelContract.VideoEntry.CONTENT_URI, // Table to query
                    null, // Projection to return - null means return all fields
                    ChannelContract.VideoEntry.COLUMN_CATEGORY + " = ?", // Selection clause
                    new String[]{category},  // Select based on the category id.
                    null // Default sort order
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.d("ESETV", "onLoadFinished called.");
        if (data != null && data.moveToFirst()) {
            final int loaderId = loader.getId();

            if (loaderId == CATEGORY_LOADER) {
                Log.d("ESETV", "onLoadFinished CATEGORY_LOADER. id:" + loaderId);

                // Every time we have to re-get the category loader, we must re-create the sidebar.
                //mCategoryRowAdapter.clear();
                // Create a row for this special case with more samples.
                HeaderItem gridHeader1 = new HeaderItem(getString(R.string.channel_lists));
                GridItemPresenter gridPresenter1 = new GridItemPresenter(this);
                ArrayObjectAdapter gridRowAdapter1 = new ArrayObjectAdapter(gridPresenter1);
                gridRowAdapter1.add(new GridItem(getString(R.string.all_channels), R.drawable.android_header));
                gridRowAdapter1.add(new GridItem(getString(R.string.previous_channels)));
                gridRowAdapter1.add(new GridItem(getString(R.string.recordings)));
                ListRow row1 = new ListRow(gridHeader1, gridRowAdapter1);
                mCategoryRowAdapter.add(row1);

                // Iterate through each category entry and add it to the ArrayAdapter.

                while (!data.isAfterLast()) {

                    int categoryIndex =
                            data.getColumnIndex(ChannelContract.VideoEntry.COLUMN_CATEGORY);
                    String category = data.getString(categoryIndex);

                    // Create header for this category.
                    Log.d("ESETV", "onLoadFinished Create header for category:" + category);
                    HeaderItem header = new HeaderItem(category);

                    int videoLoaderId = category.hashCode(); // Create unique int from category.
                    CursorObjectAdapter existingAdapter = mVideoCursorAdapters.get(videoLoaderId);
                    if (existingAdapter == null) {

                        // Map video results from the database to Video objects.
                        CursorObjectAdapter videoCursorAdapter =
                                new CursorObjectAdapter(new CardPresenter());
                        videoCursorAdapter.setMapper(new VideoCursorMapper());
                        mVideoCursorAdapters.put(videoLoaderId, videoCursorAdapter);

                        ListRow row = new ListRow(header, videoCursorAdapter);
                        mCategoryRowAdapter.add(row);

                        // Start loading the videos from the database for a particular category.
                        Log.d("ESETV", "onLoadFinished. Start loading the videos from the database for a particular category.");
                        Bundle args = new Bundle();
                        args.putString(ChannelContract.VideoEntry.COLUMN_CATEGORY, category);
                        mLoaderManager.initLoader(videoLoaderId, args, this);
                    } else {
                        Log.d("ESETV", "onLoadFinished. existing adapter exists, add row to adapter.");
                        ListRow row = new ListRow(header, existingAdapter);
                        mCategoryRowAdapter.add(row);
                    }

                    data.moveToNext();
                }

                // Create a row for this special case with more samples.
                MainHeaderItem gridHeader = new MainHeaderItem(getString(R.string.settings), R.drawable.ic_settings_settings);
                GridItemPresenter gridPresenter = new GridItemPresenter(this);
                ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
                gridRowAdapter.add(new GridItem(getString(R.string.personal_settings), R.drawable.ic_settings_settings));
                ListRow row = new ListRow(gridHeader, gridRowAdapter);
                mCategoryRowAdapter.add(row);
                //this.setSelectedPosition(2);
                startEntranceTransition(); // TODO: Move startEntranceTransition to after all
                // cursors have loaded.
                mLoaderManager.destroyLoader(CATEGORY_LOADER);
            } else {
                // The CursorAdapter contains a Cursor pointing to all videos.
                //Log.d("ESETV", "onLoadFinished CursorAdapter contains a Cursor pointing to all videos");

                mVideoCursorAdapters.get(loaderId).changeCursor(data);
            }
        } else {
            // Every time we have to re-get the category loader, we must re-create the sidebar.
            Log.d("ESETV", "onLoadFinished clear rowAdapter.");

            mCategoryRowAdapter.clear();

            // Create a row for this special case with more samples.
            HeaderItem gridHeader = new HeaderItem(getString(R.string.settings));
            GridItemPresenter gridPresenter = new GridItemPresenter(this);
            ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
            gridRowAdapter.add(new GridItem(getString(R.string.personal_settings), R.drawable.ic_settings_settings));
            ListRow row = new ListRow(gridHeader, gridRowAdapter);
            mCategoryRowAdapter.add(row);

            Intent profileIntent = new Intent(getActivity(), FetchProfileService.class);
            getActivity().startService(profileIntent);
        }
    }

    private  void loadProfiles() {
        Cursor cursor = getContext().getContentResolver().query(
                ProfileContract.ProfileEntry.CONTENT_URI,  // The content URI of the words table
                DatabaseProvider.sProfilesContainingQueryColumns,                       // The columns to return for each row
                null,
                null,
                null);

        if (null == cursor) {
            Log.d("ESETV", "loadProfiles. Null cursor");
        } else if (cursor.getCount() < 1) {
            Log.d("ESETV", "loadProfiles. Empty cursor");
        } else {
            Intent channelIntent = new Intent(getActivity(), FetchChannelService.class);
            Intent recordingIntent = new Intent(getActivity(), FetchRecordService.class);
            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_NAME));
                String auth = cursor.getString(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_AUTH));
                String web_address = cursor.getString(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS));
                int web_port = cursor.getInt(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_WEB_PORT));
                int stream_port = cursor.getInt(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_STREAM_PORT));

                Log.d("ESETV", "Profile: " + name);

                channelIntent.putExtra("auth", auth);
                channelIntent.putExtra("download_url", web_address + ":" + web_port + "/");
                channelIntent.putExtra("base", web_address + ":" + stream_port + "/");
                getActivity().startService(channelIntent);

                recordingIntent.putExtra("auth", auth);
                recordingIntent.putExtra("download_url", web_address + ":" + web_port + "/");
                recordingIntent.putExtra("base", web_address + ":" + stream_port + "/");
                getActivity().startService(recordingIntent);

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();
        //Log.d("ESETV", "onLoaderReset called id: " + loaderId);
        if (loaderId != CATEGORY_LOADER) {
            mVideoCursorAdapters.get(loaderId).changeCursor(null);
        } else {
            mCategoryRowAdapter.clear();
        }
    }

    private class UpdateBackgroundTask implements Runnable {

        @Override
        public void run() {
            if (mBackgroundURI != null) {
                updateBackground(mBackgroundURI.toString(), mBackgroundAuth);
            }
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;

                mSelectedHeaderName = row.getHeaderItem().getName();
                mSelectedRowId = video.id;

                if (video.title.endsWith("EPG")) {
                    Intent intent = new Intent(getActivity(), EpgActivity.class);
                    intent.putExtra(TVChannelDetailsActivity.TVCHANNEL, video);
                    startActivity(intent);
                    return;
                }

                SingletonNameViewModelFactory singletonNameViewModelFactory =
                        new SingletonNameViewModelFactory(PreviousChannelsViewModel.getInstance());
                PreviousChannelsViewModel viewModel =
                        ViewModelProviders.of(getActivity(), singletonNameViewModelFactory).get(PreviousChannelsViewModel.class);

                viewModel.addChannel(video);
                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                intent.putExtra(TVChannelDetailsActivity.TVCHANNEL, video);
                startActivity(intent);

            } else if (item instanceof GridItem) {
                String title = ((GridItem) item).getTitle();
                if (title.contains(getString(R.string.all_channels))) {
                    Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
                    intent.putExtra("recordings", false);
                    Bundle bundle =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                    .toBundle();
                    startActivity(intent, bundle);
                } else if (title.contains(getString(R.string.previous_channels))) {
                    Intent intent = new Intent(getActivity(), PreviousChannelsActivity.class);
                    Bundle bundle =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                    .toBundle();
                    startActivity(intent, bundle);
                } else if (title.contains(getString(R.string.recordings))) {
                    Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
                    intent.putExtra("recordings", true);
                    Bundle bundle =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                    .toBundle();
                    bundle.putBoolean("recordings", true);
                    startActivity(intent, bundle);
                } else if(title.contains(getString(R.string.personal_settings))) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    Bundle bundle =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity())
                                    .toBundle();
                    startActivity(intent, bundle);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Video) {
                try {
                    String title = ((Video) item).title;
                    if (title.endsWith("/epg")) {

                    }
                    else {
                        mBackgroundURI = Uri.parse(((Video) item).bgImageUrl);
                        mBackgroundAuth = ((Video) item).authorization;
                    }
                    startBackgroundTimer();
                }
                catch (Exception e) {
                    Log.d("ESETV", "Exception " + e.getLocalizedMessage());
                }
            }

        }
    }
}
