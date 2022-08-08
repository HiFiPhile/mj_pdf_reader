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

import android.content.ClipData;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    static public void showAppFeaturesDialog(Context context) {
        String end = "\n\n";
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name) + " Features")
                .setMessage(
                    "* Fast & smooth experience." + end +
                    "* Minimalist & simple user interface." + end +
                    "* Remembers the last opened page." + end +
                    "* Dark mode for the app and the PDF." + end +
                    "* True full screen with hidable buttons." + end +
                    "* An option to keep the screen on." + end +
                    "* Open online PDFs through links." + end +
                    "* Share & print PDFs." + end +
                    "* Open multiple PDFs." + end +
                    "* FOSS and totally private. (see About)."
                )
                .setPositiveButton(context.getResources().getString(R.string.ok), (dialogInterface, i)
                        -> dialogInterface.dismiss() )
                .create()
                .show();
    }


    static Intent emailIntent(String emailAddress, String subject, String text) {
        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(Uri.parse("mailto:" + emailAddress));
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, text);
        return email;
    }

    static Intent plainTextShareIntent(String chooserTitle, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return Intent.createChooser(intent, chooserTitle);
    }

    static Intent fileShareIntent(String chooserTitle, String fileName, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setClipData(new ClipData(fileName, new String[] { "application/pdf" }, new ClipData.Item(fileUri)));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(intent, chooserTitle);
    }

    static Intent linkIntent(String url) {
        Intent link = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return link;
    }

    static Intent navIntent(Context context, Class activity) {
        Intent navigate = new Intent(context, activity);
        return navigate;
    }

    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    static boolean canWriteToDownloadFolder(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return true;

        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    static byte[] readBytesToEnd(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    static void writeBytesToFile(File directory, String fileName, byte[] fileContent) throws IOException {
        File file = new File(directory, fileName);
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(fileContent);
        }
    }
}
