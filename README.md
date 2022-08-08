<div align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="120" />
</div>

# MJ PDF Reader

This is a fork made by Mudlej from PDF Viewer Plus originally made by Gokul Swaminathan (@JavaCafe01).

I made several modifications and fixes to the UI, UX and functionality, and called the new version MJ PDF Reader.

## Screenshots

| Main Page (Light) | Main Page (Dark) | Full Screen (Light) |
|:-:|:-:|:-:|
| <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/normal_light.jpg" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/normal_dark.jpg" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/fullscreen_light.jpg" width="190" height="400"/> |

## MJ PDF Reader Features
* Fast & smooth experience.
* Minimalist & simple user interface.
* Remembers the last opened page.
* Dark mode for the app and the PDF.
* True full screen with hidable buttons.
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
- [ ] Refactor the unholy code. (I am not the culprit for the most part, though.)
- [ ] Rewrite it in Kotlin would be even better.
- [ ] Adding search functionality. Though, it looks like this is not an easy task, since the android-pdf-viewer library doesn't support that. (and of course OCR is not an option)
- [ ] Adding highlight functionality. I don't use it personally, so I don't think I'll work on it any time soon. But feel free to create a pull request.
- [ ] Take a look at this (https://github.com/JavaCafe01/PdfViewer/issues/175)
- [ ] Adding an option to disable anything related to network connections in the settings.
- [ ] Extract all the resources from java files.

## What is different from PDF Viewer Plus

- [x] Supports new versions of Android
- [x] Removed the Bottom Bar & placed its options in the action menu.
- [x] Changed the Default Theme to the Material theme.
- [ ] (Disabled this temporarily) App's light / dark theme follows phone's theme.
- [x] A Shortcut to switch the theme for the app and PDF in the action menu.
- [x] Placed the toggle full screen button in the top bar.
- [x] True full screen mode
  - While in the full screen mode, there are no buttons nor bars that will show by default or when scrolling, instead tapping the screen will show / hide three elements to control the viewer:
    - A simple button to get out of the full screen. (top-left corner)
    - A simple button to rotate the screen and lock the rotation while in the full screen mode. (top-left corner)
    - A page scroll handle.
- [x] Changed the behavior of the page scroll handler (scroll bar). 
  - Right now it won't pop up very annoyingly when you're just reading the file. It's hidden until you tap the screen, another tap will hide it.
- [x] Changed the title format in the top bar to show the pages count first, and removed the '.pdf' form it.
- [x] Fixed the (very annoying) issue with full screen mode being lost when the screen gets rotated.
- [x] Added the functionality to restore the zoom level when rotating the device via a Snackbar prompt.
- [x] Removed WhatsNew dependency. (https://github.com/TonnyL/WhatsNew) 
- [x] Removed Cyanea dependency. (https://github.com/jaredrummler/Cyanea)
- [x] Fixed light / dark theming consistency problem by removing the ability to choose many different themes for the app. (in theory it could be added, but I don't want to spend time on it)
- [x] Made the already unholy code further from cleanliness. (I went along with style, it really needs to be refactored) 
- [x] Forked and changed android-pdf-viewer library to achieve some of the above modifications.
- [x] Added TextView that will be visible only when scrolling, so you can see the page number while scrolling.

## Project status
I made this mainly for myself, and I am happy with its current state. I don't think I will give it a lot of attention.
The code could be written much more cleanly.

## Authors and acknowledgment
- MJ PDF Reader is made by @mudlej.
- The original app was made by Gokul Swaminathan (@JavaCafe01).
- Credits to (@Derekelkins)'s pull request for adding the ability to remember last opened page.

## License
MJ PDF Reader has the same MIT License as PDF Viewer Plus.

