/*
 *   MJ PDF
 *   Copyright (C) 2023 Mudlej
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  --------------------------
 *  This code was previously licensed under
 *
 *  MIT License
 *
 *  Copyright (c) 2018 Gokul Swaminathan
 *  Copyright (c) 2023 Mudlej
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.gitlab.mudlej.MjPdfReader.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        setUpSwitches()
    }

    private fun setUpSwitches() {
        // ----------------- Visual Section -------------------

        // Configure and add Anti Aliasing Switch
        val qualitySwitch = SwitchPreferenceCompat(requireContext())
        qualitySwitch.title = getString(R.string.quality)
        qualitySwitch.setDefaultValue(Preferences.highQualityDefault)
        qualitySwitch.key = Preferences.highQualityKey
        qualitySwitch.isIconSpaceReserved = false

        // Configure and add Anti Aliasing Switch
        val aliasSwitch = SwitchPreferenceCompat(requireContext())
        aliasSwitch.title = getString(R.string.alias)
        aliasSwitch.setDefaultValue(Preferences.antiAliasingDefault)
        aliasSwitch.key = Preferences.antiAliasingKey
        aliasSwitch.isIconSpaceReserved = false

        // Configure and add Keep Screen On Switch
        val screenOnSwitch = SwitchPreferenceCompat(requireContext())
        screenOnSwitch.title = getString(R.string.keep_screen_on)
        screenOnSwitch.setDefaultValue(Preferences.screenOnDefault)
        screenOnSwitch.key = Preferences.screenOnKey
        screenOnSwitch.isIconSpaceReserved = false

        // add the switches to the first section
        val firstSection: PreferenceCategory? = findPreference("visualSection")
        firstSection?.isIconSpaceReserved = false
        firstSection?.addPreference(qualitySwitch)
        firstSection?.addPreference(aliasSwitch)
        firstSection?.addPreference(screenOnSwitch)


        // ----------------- Behavior Section ------------------

        // Configure and add Double Tap to Exit On Switch
        val doubleTapToExitSwitch = SwitchPreferenceCompat(requireContext())
        doubleTapToExitSwitch.title = getString(R.string.double_tap_to_exit)
        doubleTapToExitSwitch.setDefaultValue(Preferences.doubleTapToExitEnabledDefault)
        doubleTapToExitSwitch.key = Preferences.doubleTapToExitEnabledKey
        doubleTapToExitSwitch.isIconSpaceReserved = false

        // Configure and add Keep Screen On Switch
        val horizontalScrollSwitch = SwitchPreferenceCompat(requireContext())
        horizontalScrollSwitch.title = getString(R.string.scroll)
        horizontalScrollSwitch.setDefaultValue(Preferences.horizontalScrollDefault)
        horizontalScrollSwitch.key = Preferences.horizontalScrollKey
        horizontalScrollSwitch.isIconSpaceReserved = false


        // Configure and add Auto Full Screen On Switch
        val autoFullScreenSwitch = SwitchPreferenceCompat(requireContext())
        autoFullScreenSwitch.title = getString(R.string.auto_full_screen)
        autoFullScreenSwitch.setDefaultValue(Preferences.autoFullScreenDefault)
        autoFullScreenSwitch.key = Preferences.autoFullScreenKey
        autoFullScreenSwitch.summary = getString(R.string.auto_full_screen_summary)
        autoFullScreenSwitch.isIconSpaceReserved = false

        // Configure and add Page Snap Switch
        val pageSnapSwitch = SwitchPreferenceCompat(requireContext())
        pageSnapSwitch.title = getString(R.string.snap)
        pageSnapSwitch.setDefaultValue(Preferences.pageSnapDefault)
        pageSnapSwitch.key = Preferences.pageSnapKey
        pageSnapSwitch.summary = getString(R.string.snap_summary)
        pageSnapSwitch.isIconSpaceReserved = false

        // Configure and add Page Snap Switch
        val pageFlingSwitch = SwitchPreferenceCompat(requireContext())
        pageFlingSwitch.title = getString(R.string.fling)
        pageFlingSwitch.setDefaultValue(Preferences.pageFlingDefault)
        pageFlingSwitch.key = Preferences.pageFlingKey
        pageFlingSwitch.summary = getString(R.string.fling_summary)
        pageFlingSwitch.isIconSpaceReserved = false

        // Configure and add Turn Page By Volume Buttons Switch
        val turnPageByVolumeButtonsSwitch = SwitchPreferenceCompat(requireContext())
        turnPageByVolumeButtonsSwitch.title = getString(R.string.turn_page_by_volume_buttons_title)
        turnPageByVolumeButtonsSwitch.setDefaultValue(Preferences.turnPageByVolumeButtonsDefault)
        turnPageByVolumeButtonsSwitch.key = Preferences.turnPageByVolumeButtonsKey
        turnPageByVolumeButtonsSwitch.summary = getString(R.string.turn_page_by_volume_buttons_summary)
        turnPageByVolumeButtonsSwitch.isIconSpaceReserved = false

        // add the switches to the second section.
        val secondSection: PreferenceCategory? = findPreference("behaviorSection")
        secondSection?.isIconSpaceReserved = false
        secondSection?.addPreference(doubleTapToExitSwitch)
        secondSection?.addPreference(horizontalScrollSwitch)
        secondSection?.addPreference(autoFullScreenSwitch)
        secondSection?.addPreference(pageSnapSwitch)
        secondSection?.addPreference(pageFlingSwitch)
        secondSection?.addPreference(turnPageByVolumeButtonsSwitch)


        // ----------------- Text Section ------------------
        // Configure and add Show Copy Text Dialog Switch
        val showCopyTextDialogSwitch = SwitchPreferenceCompat(requireContext())
        showCopyTextDialogSwitch.title = getString(R.string.show_copy_dialog_title)
        showCopyTextDialogSwitch.setDefaultValue(Preferences.copyTextDialogDefault)
        showCopyTextDialogSwitch.key = Preferences.copyTextDialogKey
        showCopyTextDialogSwitch.summary = getString(R.string.show_copy_dialog_summary)
        showCopyTextDialogSwitch.isIconSpaceReserved = false

        // add the switches to the second section
        val textSection: PreferenceCategory? = findPreference("textSection")
        textSection?.isIconSpaceReserved = false
        textSection?.addPreference(showCopyTextDialogSwitch)


        // ----------------- Experimental Section ------------------

        // Configure and add Keep Screen On Switch
        val appDarkThemeSwitch = SwitchPreferenceCompat(requireContext())
        appDarkThemeSwitch.title = getString(R.string.dark_theme_for_app)
        appDarkThemeSwitch.setDefaultValue(Preferences.appFollowSystemThemeDefault)
        appDarkThemeSwitch.key = Preferences.appFollowSystemTheme
        appDarkThemeSwitch.summary = getString(R.string.app_dark_theme_summary)
        appDarkThemeSwitch.isIconSpaceReserved = false

        // set a caution dialog to show for this option
        appDarkThemeSwitch.setOnPreferenceClickListener {
            // don't show the dialog when turning it off
            if (!appDarkThemeSwitch.isChecked) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                return@setOnPreferenceClickListener true
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.caution))
                .setMessage(getString(R.string.app_dark_dialog_message))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
                .setNegativeButton(getString(R.string.cancel)) {_, _ -> appDarkThemeSwitch.isChecked = false }
                .create().show()
            return@setOnPreferenceClickListener true
        }



        // Configure and add Keep Screen On Switch
        val pdfDarkThemeSwitch = SwitchPreferenceCompat(requireContext())
        pdfDarkThemeSwitch.title = getString(R.string.dark_theme_for_pdf)
        pdfDarkThemeSwitch.setDefaultValue(Preferences.pdfFollowSystemThemeDefault)
        pdfDarkThemeSwitch.key = Preferences.pdfFollowSystemTheme
        pdfDarkThemeSwitch.summary = getString(R.string.pdf_dark_theme_summary)
        pdfDarkThemeSwitch.isIconSpaceReserved = false

        // add the switches to the Experimental section
        val thirdSection: PreferenceCategory? = findPreference("experimentalSection")
        thirdSection?.isIconSpaceReserved = false
        thirdSection?.addPreference(appDarkThemeSwitch)
        thirdSection?.addPreference(pdfDarkThemeSwitch)
    }
}