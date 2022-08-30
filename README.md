<div align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" />
</div>

# Download
- [x] [Play Store](https://play.google.com/store/apps/details?id=com.gitlab.mudlej.MjPdfReader)
- [x] [IzzyOnDroid Repo](https://apt.izzysoft.de/fdroid/index/apk/com.gitlab.mudlej.MjPdfReader)
- [x] [Direct Download (V1.4.0)](https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/V1.4.0-stable/app/release/app-release.apk)
- [ ] F-droid (coming soon)

# MJ PDF Reader
This is a fork mady by Mudlej (@mudlej) from PDF Viewer Plus by Gokul Swaminathan (@JavaCafe01).
I added, fixed and modified many things in the app and its core libraries.
The new app is significantly different and is called MJ PDF Reader. ([see What is different from PDF Viewer Plus](https://gitlab.com/mudlej_android/mj_pdf_reader#what-is-different-from-pdf-viewer-plus))

## Screenshots
| Light Mode | Dark Mode |
|:-:|:-:|
| <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/light_framed%20(1).png" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/dark_framed.png" width="190" height="400"/> |

| Full Screen (No Buttons) | Full Screen (Buttons) |
|:-:|:-:|
| <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/full_no_buttons%20(1).png" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/full_buttons_framed.png" width="190" height="400"/> |

## MJ PDF Reader Features
* Fast & smooth experience.
* Minimalist & simple user interface.
* Remembers the last opened page.
* Dark mode for the app and the PDF.
* True full screen with hidable buttons.
* Search the PDF file. (experimental)
* Text mode to view PDFs like E-readers. (experimental)
* An option to keep the screen on.
* Open online PDFs through links.
* Share & print PDFs.
* Open multiple PDFs.
* FOSS and totally private

## What is new in MJ PDF Reader V1.4.0
* V1.4.1 adds a workaround to prevent app from crashing when opening huge files
* Updated the core libraries and fixed the security issue.
* Added Search functionality. (experimental) ([see Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text-mode-and-search))
* Added Text mode to view PDFs like E-readers. (experimental) ([see Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text-mode-and-search))
* Added the ability to copy text from the PDF via a dialog.
* Reorganized action bar's options and added Additional Options.

## Permissions and privacy
This app does not collect any data.
The following permissions are required to provide specific features in the app:
* *Internet*: For opening PDFs through links
* *Storage*: For saving downloading PDFs and opening them from storage

## Things I would like to do for MJ PDF Reader
- [ ] Add a home page with three tabs (PDFs folders, Recent, Favorite)
- [ ] Adding the ability to click on hyperlinks.
- [ ] Adding highlight functionality. I don't use it personally, so I don't think I'll work on it any time soon. But feel free to create a pull request.
- [ ] Adding auto-scroll mode.

## What is different from PDF Viewer Plus
- [x] Great refactoring of the code. (still needs more)
- [x] Converted the code to Kotlin. (except for two files)
- [x] Removed many security vulnerabilities. ([see Forking PdfiumAndroid](https://gitlab.com/mudlej_android/mj_pdf_reader#forking-pdfiumandroid))
- [x] Extracted almost all the resources from the logic files.
- [x] Support for the new versions of Android.
- [x] Forked and changed android-pdf-viewer library to achieve some of the below modifications.
- [x] Forked [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid) to update its core libraries. ([see Forking PdfiumAndroid](https://gitlab.com/mudlej_android/mj_pdf_reader#forking-pdfiumandroid))
- [x] Removed [WhatsNew](https://github.com/TonnyL/WhatsNew) dependency.
- [x] Removed [Cyanea](https://github.com/jaredrummler/Cyanea) dependency.
- [x] Removed deprecated PreferenceActivity and replaced it with AndroidX Preference library and rewrote all the files.
- [x] Changed the license to GPLv3
- [x] Remembers the last opened page of each PDF. (credits to @Derekelkins)
- [x] True full screen mode
  - While in the full screen mode, there are no buttons nor bars that will pop up very annoyingly when scrolling, instead tapping the screen will show / hide three elements to control the viewer:
    - A simple button to get out of the full screen. (top-left corner)
    - A simple button to rotate the screen and lock the rotation while in the full screen mode. (top-left corner)
    - A page scroll handle.
- [x] Adding a new library that will work in the background to extract PDFs text.
- [x] Adding a Text Mode that tries to view the PDF like epub files. ([see Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text_mode_and_search))
- [x] Adding a search functionality. ([see Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text_mode_and_search))
- [x] Adding the ability to copy text from the PDF via a dialog that will show up by a long press on the page.
- [x] Fixed the (very annoying) issue with full screen mode being lost when the screen gets rotated.
- [x] Added TextView that will be visible only when scrolling, so you can see the page number while scrolling.
- [x] Added the functionality to restore the zoom level when rotating the device via a Snackbar prompt.
- [x] Removed the Bottom Bar & placed its options in the action menu.
- [x] Placed the toggle full screen button in the top bar.
- [x] Changed the title format in the top bar to show the pages count first, and removed the '.pdf' form it.
- [x] Changed the Default Theme to the Material theme.
- [x] App's light / dark theme follows phone's theme. (You've to enable this option, disabled by default due to https://github.com/barteksc/AndroidPdfViewer/issues/914)
- [x] A shortcut to switch the theme for the PDF in the action menu.
- [x] Decreased the size of the app from 20 MB to 14.9 MB. (MJ PDF Reader v1.3)

## Forking PdfiumAndroid
I Forked [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid) to update its core libraries that were years behind and had too many security vulnerabilities.

* Updated PDFium to 106.0.5241.0 ([source code](https://pdfium.googlesource.com/pdfium/+/refs/heads/main), [building script](https://github.com/bblanchon/pdfium-binaries))
* Updated libpng to 1.6.37 ([source code](https://sourceforge.net/projects/libpng/files/libpng16/1.6.37/), [building script](https://github.com/kota-kota/libpng-build))
* Updated Freetype to 2.12.1 ([source code](https://github.com/freetype/freetype), [building script](https://github.com/kota-kota/freetype-build))

## Text Mode and Search
These were challenging to add, and they are still experimental. (especially Text Mode)
The problem was that PdfiumAndroid doesn't provide an option to extract text from the PDF. (even though PDFium does AFAIK)
And to make things more problematic, android-pdf-viewer renders the PDF file as bitmap images.
So, there is no way to search text. And if you did it somehow, you can't mark the results.

To overcome these obstacles, I added a new library (PdfBox-Android) that will work in the background 
as soon as the pdf is loaded to extract its text, then save it in a pageNumber:pageText map.

Then, I use it to search for the text. It works so far, but it doesn't find sentences spread over two pages.
I will try to fix that later. But the bigger problem was how to show the user the result? I can navigate
to the page of the result via pageNumber, but there is no way - that I'm aware of - to mark the result on the page.

To help the user find the result, I created a simple list that contains the line numbers of each result 
after calculating them by counting previous newlines (\n) characters.

Then, I create a snackbar that will be shown when the app navigates to the result page and won't go away
until the user dismiss it. That snackbar has enough information for the user to find the result in the page.

It tells him the line that has the result and its number in the page. 
e.g. (Line 25 - Page 12) -> this is {query} text...
The helping will start from the begging of the line to make it easier to find the line.

Text mode works in a similar fashion.
The pageNumber:pageText map of the PDF gets passed to TextModeActivity, then it displays 
all of the pages continuously in a textView.

There is two options to increase / decrease the font of the textView in the action bar.
There is a search option to look up any string in the textView. But here, results will be marked 
by a red color. (It's still under heavy development and may crash or work improperly)

## Authors and acknowledgment
- MJ PDF Reader is made by @mudlej.
- The original app (PDF View Plus) was made by Gokul Swaminathan (@JavaCafe01).
- Credits to (@Derekelkins)'s pull request for adding the ability to remember last opened page.

## License
MJ PDF Reader uses the GPLv3 license, the original app (PDF View Plus) was under MIT license
