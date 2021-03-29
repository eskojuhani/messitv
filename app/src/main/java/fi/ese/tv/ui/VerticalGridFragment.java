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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.view.View;

import fi.ese.tv.R;
import fi.ese.tv.data.ChannelContract;
import fi.ese.tv.model.PreviousChannelsViewModel;
import fi.ese.tv.model.SingletonNameViewModelFactory;
import fi.ese.tv.model.Video;
import fi.ese.tv.model.VideoCursorMapper;
import fi.ese.tv.presenter.CardPresenter;

/*
 * VerticalGridFragment shows a grid of videos that can be scrolled vertically.
 */
public class VerticalGridFragment extends VerticalGridSupportFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int NUM_COLUMNS = 5;
    private final CursorObjectAdapter mVideoCursorAdapter =
            new CursorObjectAdapter(new CardPresenter());
    private static final int ALL_VIDEOS_LOADER = 1;
    private SingletonNameViewModelFactory singletonNameViewModelFactory;
    PreviousChannelsViewModel viewModel;
    private boolean isRecordings = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getActivity().getIntent().getExtras();

        if (b != null) {
            isRecordings = b.getBoolean("recordings");
        }

        mVideoCursorAdapter.setMapper(new VideoCursorMapper());
        setAdapter(mVideoCursorAdapter);

        setTitle(getString(R.string.vertical_grid_title));

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }
        setupFragment();
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        LoaderManager.getInstance(this).initLoader(ALL_VIDEOS_LOADER, null, this);

        singletonNameViewModelFactory =
                new SingletonNameViewModelFactory(PreviousChannelsViewModel.getInstance());
        viewModel =
                ViewModelProviders.of(this, singletonNameViewModelFactory).get(PreviousChannelsViewModel.class);

        // After 500ms, start the animation to transition the cards into view.
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startEntranceTransition();
            }
        }, 500);

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (isRecordings) {
            return new CursorLoader(
                    getActivity(),
                    ChannelContract.VideoEntry.CONTENT_URI_REC,
                    null, // projection
                    ChannelContract.VideoEntry.COLUMN_IS_RECORDING + " = ?",
                    new String[]{"1"},
                    null  // sort order
            );
        }
        else {
            return new CursorLoader(
                    getActivity(),
                    ChannelContract.VideoEntry.CONTENT_URI,
                    null, // projection
                    ChannelContract.VideoEntry.COLUMN_IS_RECORDING + " = ?",
                    new String[]{"0"},
                    null  // sort order
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == ALL_VIDEOS_LOADER && cursor != null && cursor.moveToFirst()) {
            mVideoCursorAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mVideoCursorAdapter.changeCursor(null);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;
                //if (!isRecordings)
                viewModel.addChannel(video);
                Intent intent = new Intent(getActivity(), TVChannelDetailsActivity.class);
                intent.putExtra(TVChannelDetailsActivity.TVCHANNEL, video);
                startActivity(intent);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
        }
    }
}
