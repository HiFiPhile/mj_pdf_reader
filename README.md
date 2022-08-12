<div align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="120" />
</div>

# MJ PDF Reader

This is a fork made by Mudlej from PDF Viewer Plus originally made by Gokul Swaminathan (@JavaCafe01).

I made significant modifications to the app, see the section below (What is different from PDF Viewer Plus).

## Screenshots

| Main Page (Light) | Main Page (Dark) | Full Screen (Light) |
|:-:|:-:|:-:|
| <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/normal_light.jpg" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/normal_dark.jpg" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/fullscreen_light.jpg" width="190" height="400"/> |

## MJ PDF Reader Features
* Fast & smooth experience.
* Minimalist & simple user interface.
* Remembers the last opened page.
* Dark mode for the app and the PDF.
* True full screen with hidable buttons and page handler.
* An option to keep the screen on.
* Open online PDFs through links.
* Share & print PDFs.
* Open multiple PDFs.
* FOSS and totally private

## Permissions and privacy
This app does not collect any data.  
The following permissions are required to provide specific features in the app:
* *Internet*: For opening PDFs through links
* *Storage*: For saving downloading PDFs and opening them from storage

## Things I would like to do for MJ PDF Reader
- [ ] Add a home page with three tabs (PDFs folders, Recent, Favorite)
- [ ] Adding search functionality. Though, it looks like this is not an easy task, since the android-pdf-viewer library doesn't support that. (and of course OCR is not an option)
- [ ] Adding highlight functionality. I don't use it personally, so I don't think I'll work on it any time soon. But feel free to create a pull request.
- [ ] Adding auto-scroll mode.
- [ ] Take a look at this (https://github.com/JavaCafe01/PdfViewer/issues/175)

## What is different from PDF Viewer Plus
- [x] Great refactoring of the code. (still needs more)
- [x] Converted the code to Kotlin. (except for two files)
- [x] Extracted almost all the resources from the logic files.
- [x] Support for the new versions of Android.
- [x] Forked and changed android-pdf-viewer library to achieve some of the below modifications.
- [x] Removed WhatsNew dependency. (https://github.com/TonnyL/WhatsNew)
- [x] Removed Cyanea dependency. (https://github.com/jaredrummler/Cyanea)
- [x] Removed deprecated PreferenceActivity and replaced it with AndroidX Preference library and rewrote all the files.
- [x] Changed the license to GPLv3
- [x] True full screen mode
  - While in the full screen mode, there are no buttons nor bars that will show by default or when scrolling, instead tapping the screen will show / hide three elements to control the viewer:
    - A simple button to get out of the full screen. (top-left corner)
    - A simple button to rotate the screen and lock the rotation while in the full screen mode. (top-left corner)
    - A page scroll handle.
- [x] Fixed the (very annoying) issue with full screen mode being lost when the screen gets rotated.
- [x] Changed the behavior of the page scroll handler (scroll bar).
  - Right now it won't pop up very annoyingly when you're just reading the file. It's hidden until you tap the screen, another tap will hide it.
- [x] Added TextView that will be visible only when scrolling, so you can see the page number while scrolling.
- [x] Added the functionality to restore the zoom level when rotating the device via a Snackbar prompt.
- [x] Removed the Bottom Bar & placed its options in the action menu.
- [x] Placed the toggle full screen button in the top bar.
- [x] Changed the title format in the top bar to show the pages count first, and removed the '.pdf' form it.
- [x] Changed the Default Theme to the Material theme.
- [x] App's light / dark theme follows phone's theme. (You've to enable this option, disabled by default due to https://github.com/barteksc/AndroidPdfViewer/issues/914)
- [x] A Shortcut to switch the theme for the PDF in the action menu.


## Authors and acknowledgment
- MJ PDF Reader is made by @mudlej.
- The original app (PDF View Plus) was made by Gokul Swaminathan (@JavaCafe01).
- Credits to (@Derekelkins)'s pull request for adding the ability to remember last opened page.

## License
MJ PDF Reader uses the GPLv3 license, the original app (PDF View Plus) was under MIT licnse