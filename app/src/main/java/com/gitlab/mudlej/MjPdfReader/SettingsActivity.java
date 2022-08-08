package com.gitlab.mudlej.MjPdfReader;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.view.MenuItem;

import com.gitlab.mudlej.MjPdfReader.data.Preferences;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setupActionBar(); // TODO FIX
        addPreferencesFromResource(R.xml.settings);
        setUpSwitches();
    }

    private void setUpSwitches() {
        // ----------------- First Section -------------------

        // Configure and add Anti Aliasing Switch
        SwitchPreference qualitySwitch = new SwitchPreference(this);
        qualitySwitch.setTitle(getString(R.string.quality));
        qualitySwitch.setDefaultValue(Preferences.highQualityDefault);
        qualitySwitch.setKey(Preferences.highQualityKey);

        // Configure and add Anti Aliasing Switch
        SwitchPreference aliasSwitch = new SwitchPreference(this);
        aliasSwitch.setTitle(getString(R.string.alias));
        aliasSwitch.setDefaultValue(Preferences.antiAliasingDefault);
        aliasSwitch.setKey(Preferences.antiAliasingKey);

        // Configure and add Keep Screen On Switch
        SwitchPreference screenOnSwitch = new SwitchPreference(this);
        screenOnSwitch.setTitle(getString(R.string.keep_screen_on));
        screenOnSwitch.setDefaultValue(Preferences.screenOnDefault);
        screenOnSwitch.setKey(Preferences.screenOnKey);

        // add the switches to the first section
        PreferenceGroup firstSection = (PreferenceGroup) findPreference("firstSection");
        firstSection.addPreference(qualitySwitch);
        firstSection.addPreference(aliasSwitch);
        firstSection.addPreference(screenOnSwitch);


        // ----------------- Second Section ------------------

        // Configure and add Keep Screen On Switch
        SwitchPreference horizontalScrollSwitch = new SwitchPreference(this);
        horizontalScrollSwitch.setTitle(getString(R.string.scroll));
        horizontalScrollSwitch.setDefaultValue(Preferences.horizontalScrollDefault);
        horizontalScrollSwitch.setKey(Preferences.horizontalScrollKey);

        // Configure and add Page Snap Switch
        SwitchPreference pageSnapSwitch = new SwitchPreference(this);
        pageSnapSwitch.setTitle(getString(R.string.snap));
        pageSnapSwitch.setDefaultValue(Preferences.pageSnapDefault);
        pageSnapSwitch.setKey(Preferences.pageSnapKey);

        // Configure and add Page Snap Switch
        SwitchPreference pageFlingSwitch = new SwitchPreference(this);
        pageFlingSwitch.setTitle(getString(R.string.fling));
        pageFlingSwitch.setDefaultValue(Preferences.pageFlingDefault);
        pageFlingSwitch.setKey(Preferences.pageFlingKey);

        // add the switches to the second section
        PreferenceGroup secondSection = (PreferenceGroup) findPreference("secondSection");
        secondSection.addPreference(horizontalScrollSwitch);
        secondSection.addPreference(pageSnapSwitch);
        secondSection.addPreference(pageFlingSwitch);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                onBackPressed();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    // TODO: FIX
//    private void setupActionBar() {
//        ActionBar actionBar =  getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            setActionBar(actionBar);
//        }
//    }
    // TODO FIX
//    @Override
//    public void setSupportActionBar(@Nullable Toolbar toolbar) {
//        getDelegate().setSupportActionBar(toolbar);
//    }
}
