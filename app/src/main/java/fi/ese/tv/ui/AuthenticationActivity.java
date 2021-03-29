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

package fi.ese.tv.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.preference.PreferenceManager;

import android.text.InputType;
import android.widget.Toast;

import fi.ese.tv.R;

import java.util.List;

public class AuthenticationActivity extends FragmentActivity {
    private static final int CONTINUE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepSupportFragment.addAsRoot(this, new FirstStepFragment(), android.R.id.content);
        }
    }

    public static class FirstStepFragment extends GuidedStepSupportFragment {
        @Override
        public int onProvideTheme() {
            return R.style.Theme_Example_Leanback_GuidedStep_First;
        }

        @Override
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() {
                @Override
                public int onProvideLayoutId() {
                    return R.layout.guidedstep_second_guidance;
                }
            };
        }
        @Override
        @NonNull
        public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
            String title = getString(R.string.pref_title_screen_signin);
            String description = getString(R.string.pref_title_login_description);
            Drawable icon = getActivity().getDrawable(R.drawable.ic_main_icon);
            return new GuidanceStylist.Guidance(title, description, "", icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction enterUsername = new GuidedAction.Builder(getContext())
                    .title(getString(R.string.pref_title_username))
                    .descriptionEditable(true)
                    .build();
            GuidedAction enterPassword = new GuidedAction.Builder(getContext())
                    .title(getString(R.string.pref_title_password))
                    .descriptionEditable(true)
                    .descriptionInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT)
                    .build();
            GuidedAction login = new GuidedAction.Builder(getContext())
                    .id(CONTINUE)
                    .title(getString(R.string.guidedstep_continue))
                    .build();
            actions.add(enterUsername);
            actions.add(enterPassword);
            actions.add(login);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == CONTINUE) {
                // TODO Authenticate your account
                // Assume the user was logged in
                Toast.makeText(getActivity(), "Welcome!", Toast.LENGTH_SHORT).show();
                //getActivity().finishAfterTransition();
            }
            else if (action.getTitle().equals(getString(R.string.pref_title_username))) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username",action.getLabel2().toString());
                editor.apply();
                editor.commit();
            }
            else if (action.getTitle().equals(getString(R.string.pref_title_password))) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("password",action.getLabel2().toString());
                editor.apply();
                editor.commit();
            }
        }
    }
}
