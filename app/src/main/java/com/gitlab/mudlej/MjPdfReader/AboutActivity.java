/*
 * MIT License
 *
 * Copyright (c) 2018 Gokul Swaminathan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gitlab.mudlej.MjPdfReader;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityAboutBinding;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AboutActivity extends CyaneaAppCompatActivity {

    private ActivityAboutBinding viewBinding;
    private final String APP_VERSION_RELEASE = "Version " + Utils.getAppVersion();   //contains Version + the version number
    private final String APP_VERSION_DEBUG = "Version " + Utils.getAppVersion() + "-debug";   //contains Version + the version number + debug


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        setVersionText();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // just for fun
        viewBinding.aboutAppLogo.setOnClickListener(view -> fixColors());
    }

    private void setVersionText() {
        // check if app is debug
        if (BuildConfig.DEBUG) {
            viewBinding.versionTextView.setText(APP_VERSION_DEBUG);
        } else {   //if app is release
            viewBinding.versionTextView.setText(APP_VERSION_RELEASE);
        }
    }

    public void replayIntro(View v) {
        //navigate to intro class (replay the intro)
        startActivity(Utils.navIntent(getApplicationContext(), MainIntroActivity.class));
    }

    public void showLog(View v) {
        Utils.showAppFeaturesDialog(this);
    }

    public void showPrivacy(View v) {
        new PrivacyInfoDialog().show(getSupportFragmentManager(), "privacy_dialog");
    }

    public void showLicense(View v) {
        startActivity(Utils.linkIntent("https://gitlab.com/mudlej_android/mj_pdf_reader/-/blob/main/LICENSE"));
    }

    public void showLibraries(View v) {
        AttributionPresenter attributionPresenter = new AttributionPresenter.Builder(this)
                .addAttributions(
                        new Attribution.Builder("AttributionPresenter")
                                .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("MJ PDF's fork of Android PdfViewer")
                                .addCopyrightNotice("Copyright 2017 Bartosz Schiller")
                                .addLicense(License.APACHE)
                                .setWebsite("https://gitlab.com/mudlej_android/mj_pdf_reader/-/tree/main/AndroidPdfViewer")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("AppIntro")
                                .addCopyrightNotice("Copyright 2018 Paolo Rotolo")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/paolorotolo/AppIntro")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Android Open Source Project")
                                .addCopyrightNotice("Copyright 2016 The Android Open Source Project")
                                .addLicense(License.APACHE)
                                .setWebsite("http://developer.android.com/tools/support-library/index.html")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Android Support Libraries")
                                .addCopyrightNotice("Copyright 2016 The Android Open Source Project")
                                .addLicense(License.APACHE)
                                .setWebsite("http://developer.android.com/tools/support-library/index.html")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Material Design Icons")
                                .addCopyrightNotice("Copyright 2014, Austin Andrews")
                                .addLicense("SIL Open Font", "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE")
                                .setWebsite("https://materialdesignicons.com/")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Cyanea")
                                .addCopyrightNotice("Copyright 2018 Jared Rummler")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/jaredrummler/Cyanea")
                                .build()
                )
                .build();

        //show license dialogue
        attributionPresenter.showDialog("Open Source Libraries");
    }

    public void emailDev(View v) {
        String email = "mudlej@proton.me";
        try {
            startActivity(Utils.emailIntent(email, "MJ PDF Reader", APP_VERSION_RELEASE));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, email, Toast.LENGTH_SHORT).show();
        }
    }

    public void navToGit(View v) {
        startActivity(Utils.linkIntent("https://gitlab.com/mudlej"));
    }

    public void navToSourceCode(View v) {
        startActivity(Utils.linkIntent("https://gitlab.com/mudlej_android/mj_pdf_reader"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public static class PrivacyInfoDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            return builder.setTitle(R.string.privacy)
                    .setMessage(R.string.privacy_info)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {})
                    .setIcon(R.drawable.privacy_icon)
                    .create();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // To fix icons colors right after the activity is fully visible
        fixColors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // To fix AppName TextView color (doesn't work in onWindowFocusChanged)
        fixColors();
    }

    private void fixColors() {
        List<TextView> views = Arrays.asList(
                viewBinding.introView, viewBinding.changeView, viewBinding.codeView,
                viewBinding.emailView, viewBinding.libView, viewBinding.gitView,
                viewBinding.privacyView, viewBinding.versionTextView, viewBinding.licenseView
        );

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDark = prefManager.getBoolean("isDarkTheme", false);

        int color = isDark ? R.color.bright : R.color.dark;

        for (TextView view : views) {
            if (view.getCompoundDrawables()[0] != null)
                view.getCompoundDrawables()[0].setTint(getResources().getColor(color));
        }

        viewBinding.aboutAppName.setTextColor(Color.parseColor(getResources().getString(color)));
    }
}