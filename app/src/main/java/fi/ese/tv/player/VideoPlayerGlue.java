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

package fi.ese.tv.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityOptionsCompat;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;

import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter;

import java.util.concurrent.TimeUnit;

import fi.ese.tv.ui.AuthenticationActivity;
import fi.ese.tv.ui.SettingsActivity;

/**
 * Manages customizing the actions in the {@link PlaybackControlsRow}. Adds and manages the
 * following actions to the primary and secondary controls:
 *
 * <ul>
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.RepeatAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.ThumbsDownAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.ThumbsUpAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.SkipNextAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.FastForwardAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.RewindAction}
 * </ul>
 *
 * Note that the superclass, {@link PlaybackTransportControlGlue}, manages the playback controls
 * row.
 */
public class VideoPlayerGlue extends PlaybackTransportControlGlue<LeanbackPlayerAdapter> {

    private static final long TEN_SECONDS = TimeUnit.SECONDS.toMillis(10);

    /** Listens for when skip to next and previous actions have been dispatched. */
    public interface OnActionClickedListener {

        /** Skip to the previous item in the queue. */
        void onPrevious();

        /** Skip to the next item in the queue. */
        void onNext();

        void toggleCaption();
    }

    private final OnActionClickedListener mActionListener;

    private PlaybackControlsRow.RepeatAction mRepeatAction;
    private PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
    private PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    private PlaybackControlsRow.PictureInPictureAction mPictureInPictureAction;
    private PlaybackControlsRow.MoreActions mMoreActions;

    private Context mContext;
    private boolean mIsVideo = false;

    public VideoPlayerGlue(
            Context context,
            LeanbackPlayerAdapter playerAdapter,
            OnActionClickedListener actionListener,
            Boolean isVideo) {
        super(context, playerAdapter);

        this.mContext = context;
        this.mIsVideo = isVideo;

        mActionListener = actionListener;
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(context);
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(context);
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(context);
        mRewindAction = new PlaybackControlsRow.RewindAction(context);

        mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(context);
        mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsUpAction.INDEX_OUTLINE);
        mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(context);
        mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsDownAction.INDEX_OUTLINE);
        mRepeatAction = new PlaybackControlsRow.RepeatAction(context);

        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(context);
        mPictureInPictureAction = new PlaybackControlsRow.PictureInPictureAction(context);
        mMoreActions = new PlaybackControlsRow.MoreActions(context);

    }

    @Override
    protected void onCreatePrimaryActions(ArrayObjectAdapter adapter) {
        // Order matters, super.onCreatePrimaryActions() will create the play / pause action.
        // Will display as follows:
        // play/pause, previous, rewind, fast forward, next
        //   > /||      |<        <<        >>         >|
        if (mIsVideo) {
            super.onCreatePrimaryActions(adapter);
            adapter.add(mSkipPreviousAction);
            adapter.add(mRewindAction);
            adapter.add(mFastForwardAction);
            adapter.add(mSkipNextAction);
        }
        else {
            adapter.add(mSkipPreviousAction);
            super.onCreatePrimaryActions(adapter);
            adapter.add(mSkipNextAction);
        }
    }

    @Override
    protected void onCreateSecondaryActions(ArrayObjectAdapter adapter) {
        super.onCreateSecondaryActions(adapter);

        if (mIsVideo) {
            adapter.add(mThumbsDownAction);
            adapter.add(mThumbsUpAction);
            adapter.add(mRepeatAction);
        }
        else {
            adapter.add(mThumbsDownAction);
            adapter.add(mThumbsUpAction);
        }
        adapter.add(mClosedCaptioningAction);
        adapter.add(mPictureInPictureAction);
        adapter.add(mMoreActions);
    }

    @Override
    public void onActionClicked(Action action) {
        Log.d("ESETV", "VideoPlayerGlue.onActionClicked " + action.toString() + " : " +  action);
        if (shouldDispatchAction(action)) {
            dispatchAction(action);
            return;
        }
        // Super class handles play/pause and delegates to abstract methods next()/previous().
        super.onActionClicked(action);
    }

    // Should dispatch actions that the super class does not supply callbacks for.
    private boolean shouldDispatchAction(Action action) {
        return action == mRewindAction
                || action == mFastForwardAction
                || action == mThumbsDownAction
                || action == mThumbsUpAction
                || action == mRepeatAction
                || action == mClosedCaptioningAction
                || action == mPictureInPictureAction;
    }

    private void dispatchAction(Action action) {
        Log.d("ESETV", "Primary action. Handled manually..");
        // Primary actions are handled manually.
        if (action == mRewindAction) {
            rewind();
        } else if (action == mFastForwardAction) {
            fastForward();
        } else if (action == mClosedCaptioningAction) {
            mActionListener.toggleCaption();
        } else if (action == mThumbsUpAction) {
            Intent intent = new Intent(getContext(), AuthenticationActivity.class);
            Bundle bundle =
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)mContext)
                            .toBundle();
            getContext().startActivity(intent, bundle);
        } else if (action instanceof PlaybackControlsRow.MultiAction) {
            PlaybackControlsRow.MultiAction multiAction = (PlaybackControlsRow.MultiAction) action;
            multiAction.nextIndex();
            // Notify adapter of action changes to handle secondary actions, such as, thumbs up/down
            // and repeat.
            notifyActionChanged(
                    multiAction,
                    (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter());
        } else {
            Log.d("ESETV", "action dispatched:" + action.toString());
        }
    }

    private void notifyActionChanged(
            PlaybackControlsRow.MultiAction action, ArrayObjectAdapter adapter) {
        if (adapter != null) {
            int index = adapter.indexOf(action);
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1);
            }
        }
    }

    @Override
    public void next() {
        Log.d("ESETV", "VideoPlayerGlue.next called.");
        mActionListener.onNext();
    }

    @Override
    public void previous() {
        Log.d("ESETV", "VideoPlayerGlue.previous called.");
        mActionListener.onPrevious();
    }

    /** Skips backwards 10 seconds. */
    public void rewind() {
        long newPosition = getCurrentPosition() - TEN_SECONDS;
        newPosition = (newPosition < 0) ? 0 : newPosition;
        getPlayerAdapter().seekTo(newPosition);
    }

    /** Skips forward 10 seconds. */
    public void fastForward() {
        if (getDuration() > -1) {
            long newPosition = getCurrentPosition() + TEN_SECONDS;
            newPosition = (newPosition > getDuration()) ? getDuration() : newPosition;
            getPlayerAdapter().seekTo(newPosition);
        }
    }
}
