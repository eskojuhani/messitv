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

import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fi.ese.tv.R;
import fi.ese.tv.model.GridItem;
import fi.ese.tv.model.MainHeaderItem;
import fi.ese.tv.ui.MainFragment;

public class GridItemPresenter extends Presenter {
    private final MainFragment mainFragment;

    public GridItemPresenter(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        //TextView view = new TextView(parent.getContext());
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.icon_grid_item, null);
        TextView label = (TextView) view.findViewById(R.id.header_label);

        Resources res = parent.getResources();
        int width = res.getDimensionPixelSize(R.dimen.grid_item_width);
        int height = res.getDimensionPixelSize(R.dimen.grid_item_height);

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
        GridItem gridItem = (GridItem) item;
        View rootView = viewHolder.view;

        TextView label = (TextView) rootView.findViewById(R.id.header_label);
        label.setText(gridItem.getTitle());

        rootView.setFocusable(true);
        int iconId = gridItem.getIconResId();

        if (iconId != GridItem.ICON_NONE) {
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
