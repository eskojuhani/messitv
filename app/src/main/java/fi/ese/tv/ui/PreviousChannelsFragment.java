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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.lifecycle.ViewModelProviders;

import fi.ese.tv.R;
import fi.ese.tv.model.PreviousChannelsViewModel;
import fi.ese.tv.model.SingletonNameViewModelFactory;
import fi.ese.tv.model.Video;
import fi.ese.tv.presenter.CardPresenter;

/*
 * VerticalGridFragment shows a grid of videos that can be scrolled vertically.
 */
public class PreviousChannelsFragment extends VerticalGridSupportFragment {

    private static final int NUM_COLUMNS = 5;
    private final ArrayObjectAdapter mVideoArrayAdapter =
            new ArrayObjectAdapter(new CardPresenter());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdapter(mVideoArrayAdapter);

        setTitle(getString(R.string.previous_channels));

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }
        setupFragment();

        SingletonNameViewModelFactory singletonNameViewModelFactory =
                new SingletonNameViewModelFactory(PreviousChannelsViewModel.getInstance());
        PreviousChannelsViewModel viewModel =
                ViewModelProviders.of(this, singletonNameViewModelFactory).get(PreviousChannelsViewModel.class);

        viewModel.getChannels().observe(this, channels -> {
            Log.d("ESETV", "PreviousChannelsFragment getChannels() count: " + channels.size());

            mVideoArrayAdapter.addAll(0, channels);
        });
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startEntranceTransition();
            }
        }, 500);

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;

                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
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
