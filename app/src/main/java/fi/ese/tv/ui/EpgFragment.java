package fi.ese.tv.ui;

import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import fi.ese.tv.R;
import fi.ese.tv.data.DatabaseProvider;
import fi.ese.tv.data.EpgContract;
import fi.ese.tv.data.FetchEpgService;
import fi.ese.tv.data.ProfileContract;
import fi.ese.tv.model.Epg;
import fi.ese.tv.model.EpgCursorMapper;
import fi.ese.tv.model.Video;
import fi.ese.tv.presenter.EpgItemPresenter;

/**
 * A placeholder fragment containing a simple view.
 */
public class EpgFragment extends VerticalGridSupportFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int NUM_COLUMNS = 1;
    private final CursorObjectAdapter mEpgCursorAdapter =
            new CursorObjectAdapter(new EpgItemPresenter());

    Video mVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideo = getActivity().getIntent().getParcelableExtra(TVChannelDetailsActivity.TVCHANNEL);
        Log.d("ESETV", "Channel:" + mVideo.title);

        mEpgCursorAdapter.setMapper(new EpgCursorMapper());
        setAdapter(mEpgCursorAdapter);

        setTitle(getString(R.string.epg_title));

        if (savedInstanceState == null) {
            prepareEntranceTransition();
        }
        setupFragment();

        loadProfiles();
    }

    private  void loadProfiles() {
        Cursor cursor = getContext().getContentResolver().query(
                ProfileContract.ProfileEntry.CONTENT_URI,  // The content URI of the words table
                DatabaseProvider.sProfilesContainingQueryColumns,                       // The columns to return for each row
                null,
                null,
                null);

        if (null == cursor) {
            Log.d("ESETV", "Null cursor");
        } else if (cursor.getCount() < 1) {
            Log.d("ESETV", "Empty cursor");
        } else {
            Intent epgIntent = new Intent(getActivity(), FetchEpgService.class);

            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_NAME));
                String auth = cursor.getString(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_AUTH));
                String web_address = cursor.getString(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_WEB_ADDRESS));
                int web_port = cursor.getInt(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_WEB_PORT));
                int stream_port = cursor.getInt(cursor.getColumnIndex(ProfileContract.ProfileEntry.COLUMN_STREAM_PORT));

                Log.d("ESETV", "Profile: " + name);

                epgIntent.putExtra("auth", auth);
                epgIntent.putExtra("download_url", web_address + ":" + web_port + "/");
                epgIntent.putExtra("base", web_address + ":" + stream_port + "/");
                epgIntent.putExtra("bref", mVideo.videoUrl);
                getActivity().startService(epgIntent);
            }
        }
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        Bundle args = new Bundle();
        args.putParcelable("video", mVideo);

        LoaderManager.getInstance(this).initLoader(1, args, this);

        // After 500ms, start the animation to transition the cards into view.
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
            else if (item instanceof Epg) {
                Log.d("ESETV", "EpgFragment clicked: " + ((Epg) item).sname);
                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                intent.putExtra("epg", (Epg) item);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Video video = args.getParcelable("video");
        String category = video.videoUrl;
        return new CursorLoader(
                getActivity(),
                EpgContract.EpgEntry.CONTENT_URI, // Table to query
                null, // Projection to return - null means return all fields
                EpgContract.EpgEntry.COLUMN_BREF + " = ?", // Selection clause
                new String[]{category},  // Select based on the category id.
                null // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == 1 && cursor != null && cursor.moveToFirst()) {
            mEpgCursorAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEpgCursorAdapter.changeCursor(null);
    }
}
