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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import fi.ese.tv.R;
import fi.ese.tv.model.Epg;

public class EpgItemPresenter extends Presenter {

    public EpgItemPresenter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        //TextView view = new TextView(parent.getContext());
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.epg_item, null);

        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, final boolean isFocused) {
                final TextView titleField = (TextView)view.findViewById(R.id.header_label);
                if (isFocused) {
                    Log.d("ESETV", "EpgItemPresenter isFocused TRUE: " + titleField.getText());
                }
                else {
                    Log.d("ESETV", "EpgItemPresenter isFocused false");
                }
            }
        });

        TextView label = (TextView) view.findViewById(R.id.header_label);

        Resources res = parent.getResources();
        int width = res.getDimensionPixelSize(R.dimen.epg_item_width);
        int height = res.getDimensionPixelSize(R.dimen.epg_item_height);

        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackgroundColor(ContextCompat.getColor(parent.getContext(),
                R.color.default_background));

        label.setTextColor(Color.WHITE);
        label.setGravity(Gravity.CENTER);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Epg epgItem = (Epg) item;
        View rootView = viewHolder.view;

        TextView label = (TextView) rootView.findViewById(R.id.header_label);
        label.setText(epgItem.sname);

        TextView description = (TextView) rootView.findViewById(R.id.description_label);
        description.setText(epgItem.shortdesc + " " + epgItem.longdesc);

        rootView.setFocusable(true);
        int iconId = R.drawable.ic_play_circle_filled;

        if (iconId != -1) {
            try {
                ImageView iconView = (ImageView) rootView.findViewById(R.id.header_icon);
                Drawable icon = rootView.getResources().getDrawable(iconId, null);
                iconView.setImageDrawable(icon);
            }
            catch (Exception e) {
                Log.d("ESETV", "onBindViewHolder error: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
