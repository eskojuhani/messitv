/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import fi.ese.tv.R;
import fi.ese.tv.data.ChannelContract;
import fi.ese.tv.model.Epg;
import fi.ese.tv.model.Playlist;
import fi.ese.tv.model.Video;
import fi.ese.tv.model.VideoCursorMapper;
import fi.ese.tv.player.VideoPlayerGlue;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Plays selected video, loads playlist and related videos, and delegates playback to {@link
 * VideoPlayerGlue}.
 */
public class PlaybackFragment extends VideoSupportFragment {

    private static final int UPDATE_DELAY = 16;
    public static final String ABR_ALGORITHM_EXTRA = "abr_algorithm";
    public static final String ABR_ALGORITHM_DEFAULT = "default";
    public static final String ABR_ALGORITHM_RANDOM = "random";

    private VideoPlayerGlue mPlayerGlue;
    private LeanbackPlayerAdapter mPlayerAdapter;
    private SimpleExoPlayer mPlayer;
    private TrackSelector mTrackSelector;
    private MyPlaylistActionListener mPlaylistActionListener;

    private PlayerControlView playerControlView;
    private PlayerView playerView;
    private Video mVideo;
    private Epg mEpg;
    private Playlist mPlaylist;
    private VideoLoaderCallbacks mVideoLoaderCallbacks;
    private CursorObjectAdapter mVideoCursorAdapter;

    private DefaultTrackSelector trackSelector;
    private Button selectTracksButton;
    private boolean isShowingTrackSelectionDialog;

    private SubtitleView subtitleView;
    //private LinearLayout debugRootView;
    //private TextView debugTextView;
    //private DebugTextViewHelper debugViewHelper;

    private TrackGroupArray previousTrackGroups;
    int textTrackIndex;
    TrackGroupArray trackGroups;

    ArrayList<Pair<Integer, Integer>> pairTrackList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getContext()).build();

        mVideo = getActivity().getIntent().getParcelableExtra(TVChannelDetailsActivity.TVCHANNEL);
        mEpg = getActivity().getIntent().getParcelableExtra("epg");
        mPlaylist = new Playlist();

        mVideoLoaderCallbacks = new VideoLoaderCallbacks(mPlaylist);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    /** Pauses the player. */
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onPause() {
        super.onPause();

        if (mPlayerGlue != null && mPlayerGlue.isPlaying()) {
            mPlayerGlue.pause();
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private RenderersFactory buildRenderersFactory(boolean preferExtensionRenderer) {
        @DefaultRenderersFactory.ExtensionRendererMode
        int extensionRendererMode =
                         preferExtensionRenderer
                        ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;

        return new DefaultRenderersFactory(getContext())
                .setExtensionRendererMode(extensionRendererMode);
    }

    private void initializePlayer() {
        Log.d("ESETV", "preparePlayer()");

        //debugRootView = getActivity().findViewById(R.id.controls_root);
        //debugRootView.setVisibility(View.GONE);
        //debugTextView = getActivity().findViewById(R.id.debug_text_view);

        subtitleView = getActivity().findViewById(R.id.subtitle);

        Boolean isVideo = false;
        String url = "";
        String authstr = "Basic cm9vdDowc2thcmkhMQ==";
        if (mVideo != null) {
            url = mVideo.videoUrl;
            authstr = mVideo.authorization;
            isVideo = mVideo.isRecording;
        }
        else {
            url = mEpg.sref;
        }
        String auth = authstr;

        Uri uriOfContentUrl = Uri.parse(url);
        DefaultDataSourceFactory defdataSourceFactory =
            new DefaultDataSourceFactory(getActivity(), null,
                new DefaultHttpDataSourceFactory("eseTV", null));

        ProgressiveMediaSource progressiveMediaSource =
            new ProgressiveMediaSource.Factory(
                () -> {
                    HttpDataSource dataSource =
                            new DefaultHttpDataSource("eseTV");
                    dataSource.setRequestProperty ("Authorization", auth);
                    return dataSource;
                })
                .createMediaSource(uriOfContentUrl);

        TrackSelection.Factory trackSelectionFactory;
        trackSelectionFactory = new AdaptiveTrackSelection.Factory();

        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        trackSelector.setParameters(
                trackSelector
                        .buildUponParameters()
                        .setPreferredTextLanguage("fin")
                        .setPreferredAudioLanguage("fin")
                        .build()
        );

        mPlayer = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector)
                .build();

        mPlayer.prepare(progressiveMediaSource);
        mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        mPlayer.addVideoListener(new MyVideoListener());

        MyPlayerEventListener playerEventListener = new MyPlayerEventListener();
        mPlayer.addListener(playerEventListener);

        mPlayer.addAnalyticsListener(new EventLogger(trackSelector));

        mPlayer.addTextOutput((List<Cue> cues) -> {
            if(subtitleView != null){
                subtitleView.onCues(cues);
            }
        });


        //debugViewHelper = new DebugTextViewHelper(mPlayer, debugTextView);
        //debugViewHelper.start();

        mPlayerAdapter = new LeanbackPlayerAdapter(getActivity(), mPlayer, UPDATE_DELAY);
        mPlaylistActionListener = new MyPlaylistActionListener(mPlaylist);
        mPlayerGlue = new VideoPlayerGlue(getActivity(), mPlayerAdapter, mPlaylistActionListener, isVideo);
        mPlayerGlue.setHost(new VideoSupportFragmentGlueHost(this));
        mPlayerGlue.playWhenPrepared();

        play(mVideo);
    }

    void showTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo =
                trackSelector != null ? trackSelector.getCurrentMappedTrackInfo() : null;
        if (mappedTrackInfo == null) {
            Log.d("ESETV", "tracksChanged []");
            return;
        }
        // Log tracks associated to renderers.
        int rendererCount = mappedTrackInfo.getRendererCount();
        for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
            TrackSelection trackSelection = trackSelections.get(rendererIndex);
            if (rendererTrackGroups.length > 0) {
                Log.d("ESETV", "  Renderer:" + rendererIndex + " [");
                for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                    TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                    String adaptiveSupport =
                            getAdaptiveSupportString(
                                    trackGroup.length,
                                    mappedTrackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false));
                    Log.d("ESETV", "    Group:" + groupIndex + ", adaptive_supported=" + adaptiveSupport + " [");
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        String status = getTrackStatusString(trackSelection, trackGroup, trackIndex);
                        String formatSupport =
                                getFormatSupportString(
                                        mappedTrackInfo.getTrackSupport(rendererIndex, groupIndex, trackIndex));
                        Log.d("ESETV",
                                "      "
                                        + status
                                        + " Track:"
                                        + trackIndex
                                        + ", "
                                        + Format.toLogString(trackGroup.getFormat(trackIndex))
                                        + ", supported="
                                        + formatSupport);
                    }
                    Log.d("ESETV", "    ]");
                }
                // Log metadata for at most one of the tracks selected for the renderer.
                if (trackSelection != null) {
                    for (int selectionIndex = 0; selectionIndex < trackSelection.length(); selectionIndex++) {
                        Metadata metadata = trackSelection.getFormat(selectionIndex).metadata;
                        if (metadata != null) {
                            Log.d("ESETV", "    Metadata [");
                            printMetadata(metadata, "      ");
                            Log.d("ESETV", "    ]");
                            break;
                        }
                    }
                }
                Log.d("ESETV", "  ]");
            }
        }
        // Log tracks not associated with a renderer.
        TrackGroupArray unassociatedTrackGroups = mappedTrackInfo.getUnmappedTrackGroups();
        if (unassociatedTrackGroups.length > 0) {
            Log.d("ESETV", "  Renderer:None [");
            for (int groupIndex = 0; groupIndex < unassociatedTrackGroups.length; groupIndex++) {
                Log.d("ESETV", "    Group:" + groupIndex + " [");
                TrackGroup trackGroup = unassociatedTrackGroups.get(groupIndex);
                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    String status = getTrackStatusString(false);
                    String formatSupport =
                            getFormatSupportString(RendererCapabilities.FORMAT_UNSUPPORTED_TYPE);
                    Log.d("ESETV",
                            "      "
                                    + status
                                    + " Track:"
                                    + trackIndex
                                    + ", "
                                    + Format.toLogString(trackGroup.getFormat(trackIndex))
                                    + ", supported="
                                    + formatSupport);
                }
                Log.d("ESETV", "    ]");
            }
            Log.d("ESETV", "  ]");
        }
        Log.d("ESETV", "]");
    }
    private void printMetadata(Metadata metadata, String prefix) {
        for (int i = 0; i < metadata.length(); i++) {
            Log.d("ESETV", prefix + metadata.get(i));
        }
    }

    private static String getFormatSupportString(int formatSupport) {
        switch (formatSupport) {
            case RendererCapabilities.FORMAT_HANDLED:
                return "YES";
            case RendererCapabilities.FORMAT_EXCEEDS_CAPABILITIES:
                return "NO_EXCEEDS_CAPABILITIES";
            case RendererCapabilities.FORMAT_UNSUPPORTED_DRM:
                return "NO_UNSUPPORTED_DRM";
            case RendererCapabilities.FORMAT_UNSUPPORTED_SUBTYPE:
                return "NO_UNSUPPORTED_TYPE";
            case RendererCapabilities.FORMAT_UNSUPPORTED_TYPE:
                return "NO";
            default:
                return "?";
        }
    }
    private static String getAdaptiveSupportString(int trackCount, int adaptiveSupport) {
        if (trackCount < 2) {
            return "N/A";
        }
        switch (adaptiveSupport) {
            case RendererCapabilities.ADAPTIVE_SEAMLESS:
                return "YES";
            case RendererCapabilities.ADAPTIVE_NOT_SEAMLESS:
                return "YES_NOT_SEAMLESS";
            case RendererCapabilities.ADAPTIVE_NOT_SUPPORTED:
                return "NO";
            default:
                return "?";
        }
    }
    private static String getTrackStatusString(
            @Nullable TrackSelection selection, TrackGroup group, int trackIndex) {
        return getTrackStatusString(selection != null && selection.getTrackGroup() == group
                && selection.indexOf(trackIndex) != C.INDEX_UNSET);
    }

    private static String getTrackStatusString(boolean enabled) {
        return enabled ? "[X]" : "[ ]";
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mTrackSelector = null;
            mPlayerGlue = null;
            mPlayerAdapter = null;
            mPlaylistActionListener = null;
        }
    }

    private void play(Video video) {
        mPlayerGlue.setTitle(video.title);
        mPlayerGlue.setSubtitle(video.description);
        mPlayerGlue.play();
    }


    public void skipToNext() {
        mPlayerGlue.next();
    }

    public void skipToPrevious() {
        mPlayerGlue.previous();
    }

    public void rewind() {
        mPlayerGlue.rewind();
    }

    public void fastForward() {
        mPlayerGlue.fastForward();
    }

    /** Loads a playlist with videos from a cursor and also updates the related videos cursor. */
    protected class VideoLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        static final int RELATED_VIDEOS_LOADER = 1;
        static final int QUEUE_VIDEOS_LOADER = 2;

        private final VideoCursorMapper mVideoCursorMapper = new VideoCursorMapper();

        private final Playlist playlist;

        private VideoLoaderCallbacks(Playlist playlist) {
            this.playlist = playlist;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // When loading related videos or videos for the playlist, query by category.
            String category = args.getString(ChannelContract.VideoEntry.COLUMN_CATEGORY);
            return new CursorLoader(
                    getActivity(),
                    ChannelContract.VideoEntry.CONTENT_URI,
                    null,
                    ChannelContract.VideoEntry.COLUMN_CATEGORY + " = ?",
                    new String[] {category},
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }
            int id = loader.getId();
            if (id == QUEUE_VIDEOS_LOADER) {
                playlist.clear();
                do {
                    Video video = (Video) mVideoCursorMapper.convert(cursor);

                    // Set the current position to the selected video.
                    long videoId = 0;
                    if (mVideo != null)
                        videoId = mVideo.id;
                    else
                        videoId = mEpg.id;

                    if (video.id == videoId) {
                        playlist.setCurrentPosition(playlist.size());
                    }

                    playlist.add(video);

                } while (cursor.moveToNext());
            } else if (id == RELATED_VIDEOS_LOADER) {
                mVideoCursorAdapter.changeCursor(cursor);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mVideoCursorAdapter.changeCursor(null);
        }
    }

    class MyPlaylistActionListener implements VideoPlayerGlue.OnActionClickedListener {

        private Playlist mPlaylist;

        MyPlaylistActionListener(Playlist playlist) {
            this.mPlaylist = playlist;
        }

        @Override
        public void onPrevious() {
            Log.d("ESETV", "MyPlaylistActionListener onPrevious() called");
            play(mPlaylist.previous());
        }

        @Override
        public void onNext() {
            Log.d("ESETV", "MyPlaylistActionListener onNext() called");
            play(mPlaylist.next());
        }

        @Override
        public void toggleCaption() {
            Log.d("ESETV", "Toggle Caption called.");
        }
    }
    class MyVideoListener implements VideoListener {

        // This is where we will resize view to fit aspect ratio of video
        @Override
        public void onVideoSizeChanged(
                int width,
                int height,
                int unappliedRotationDegrees,
                float pixelWidthHeightRatio
        ) {
            Log.d("ESETV", "onVideoSizeChanged w:" + width + " h:" + height + " pixelWidthHeightRatio:" + pixelWidthHeightRatio);
        }

        @Override
        public void onRenderedFirstFrame() {
            Log.d("ESETV", "onRenderedFirstFrame");
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {
            Log.d("ESETV", "onSurfaceSizeChanged: w:" + width + " h:" + height);
        }
    }

    class MyPlayerEventListener implements Player.EventListener {
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            showTracksChanged(trackGroups, trackSelections);

            TrackSelectionArray trackSelectionArray = mPlayer.getCurrentTrackSelections();
            for (int i = 0; i < trackSelectionArray.length; i++) {
                TrackSelection trackSelection = trackSelectionArray.get(i);
                if (trackSelection != null) {
                    int selectedIndex = trackSelection.getSelectedIndex();
                    int reason = trackSelection.getSelectionReason();
                    int selectedIndexInTrackGroup = trackSelection.getSelectedIndexInTrackGroup();
                    int indexInTrackGroup = trackSelection.getIndexInTrackGroup(selectedIndex);

                    Log.d("ESETV", i + ": selectedIndex:" + selectedIndex + ", reason:" + reason + ", selectedIndexInTrackGroup:" + selectedIndexInTrackGroup + ", indexInTrackGroup:" + indexInTrackGroup);
                }
                else {
                    Log.d("ESETV", "trackSelection is null " + i);
                }
            }
        }
        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.d("ESETV",isLoading + ": is or is it loading?");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d("ESETV", "onPlayerStateChanged: " + playbackState);
            switch(playbackState) {
                case Player.STATE_BUFFERING:
                    break;
                case Player.STATE_ENDED:
                    break;
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_READY:
                    Log.d("ESETV", "state ready");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (isPlaying) {
                Log.d("ESETV", "onIsPlayingChanged to PLAYING");
            }
            else {
                Log.d("ESETV", "onIsPlayingChanged not playing for some reason.");
            }
        }


        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d("ESETV", "onPlayerError error: " + error.getLocalizedMessage());
        }
    }
}
