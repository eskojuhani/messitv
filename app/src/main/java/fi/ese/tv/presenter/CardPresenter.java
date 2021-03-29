/*
 * Copyright (c) 2015 The Android Open Source Project
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

package fi.ese.tv.presenter;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import fi.ese.tv.R;
import fi.ese.tv.model.Video;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;
    private Drawable mDefaultCardImage;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mDefaultBackgroundColor =
            ContextCompat.getColor(parent.getContext(), R.color.default_background);
        mSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie, null);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };
        cardView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, final boolean isFocused) {
                final View infoField = view.findViewById(R.id.info_field);
                final TextView contentField = (TextView)view.findViewById(R.id.content_text);
                final TextView titleField = (TextView)view.findViewById(R.id.title_text);
                final Drawable mainImage = ((ImageView)view.findViewById(R.id.main_image)).getDrawable();
                contentField.setMaxWidth(400);
                if (isFocused) {
                    ((TextView)cardView.findViewById(R.id.title_text)).setMaxLines(8);
                    BaseCardView.LayoutParams infoLayout = new BaseCardView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    infoField.setLayoutParams(infoLayout);
                    RelativeLayout.LayoutParams contentLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    contentLayout.addRule(RelativeLayout.BELOW, R.id.title_text);
                    contentField.setLayoutParams(contentLayout);
                }
                else {
                    ((TextView)cardView.findViewById(R.id.title_text)).setMaxLines(4);
                }
            }
        });
        cardView.setCardType(ImageCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA);
        //cardView.setInfoVisibility(ImageCardView.CARD_REGION_VISIBLE_ALWAYS);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;

        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof String) {
            String title = (String) item;
            ImageCardView cardView = (ImageCardView) viewHolder.view;
            cardView.setTitleText(title);

            return;
        }
        Video video = (Video) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setTitleText(video.title);
        cardView.setContentText(video.description);

        Resources res = cardView.getResources();
        int width = res.getDimensionPixelSize(R.dimen.card_width);
        int height = res.getDimensionPixelSize(R.dimen.card_height);
        cardView.setMainImageDimensions(width, height);

        if (video.title.endsWith("EPG")) {
            View rootView = viewHolder.view;
            Drawable icon = rootView.getResources().getDrawable(R.drawable.row_app_icon, null);
            cardView.setMainImage(icon);
        }
        else if (video.cardImageUrl != null) {
            // Set card size from dimension resources.

            GlideUrl glideUrl = new GlideUrl(video.cardImageUrl,
                    new LazyHeaders.Builder()
                            .addHeader("Authorization", video.authorization)
                            .build());

            Glide.with(cardView.getContext())
                    .load(glideUrl)
                    .apply(RequestOptions.errorOf(mDefaultCardImage))
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
