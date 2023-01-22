<div align="center">
  <img src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" />
</div>

# Download
- [x] [Play Store](https://play.google.com/store/apps/details?id=com.gitlab.mudlej.MjPdfReader)
- [x] [IzzyOnDroid Repo](https://apt.izzysoft.de/fdroid/index/apk/com.gitlab.mudlej.MjPdfReader)
- [x] [Direct Download (V1.4.3+)](https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/V1.4.3+-stable/app/release/app-release.apk)
- [ ] F-droid (working on it...)

# TABLE OF CONTENTS
* [MJ PDF Reader](https://gitlab.com/mudlej_android/mj_pdf_reader#mj-pdf-reader)
* [Download](https://gitlab.com/mudlej_android/mj_pdf_reader#download)
* [Screenshots](https://gitlab.com/mudlej_android/mj_pdf_reader#screenshots)
* [MJ PDF Reader Features](https://gitlab.com/mudlej_android/mj_pdf_reader#mj-pdf-reader-features)
* [Permissions and privacy](https://gitlab.com/mudlej_android/mj_pdf_reader#permissions-and-privacy)
* [What is new in this release V1.4.3+](https://gitlab.com/mudlej_android/mj_pdf_reader#what-is-new-in-mj-pdf-reader-v143)
* [What's coming in the next release](https://gitlab.com/mudlej_android/mj_pdf_reader#coming-in-the-next-release-mj-pdf-reader-v150)
* [Things I would like to do for MJ PDF Reader](https://gitlab.com/mudlej_android/mj_pdf_reader#things-i-would-like-to-do-for-mj-pdf-reader)
* [What is different from PDF Viewer Plus](https://gitlab.com/mudlej_android/mj_pdf_reader#what-is-different-from-pdf-viewer-plus)
* [Forking PdfiumAndroid](https://gitlab.com/mudlej_android/mj_pdf_reader#forking-pdfiumandroid)
* [Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text-mode-and-search)
* [Authors and acknowledgment](https://gitlab.com/mudlej_android/mj_pdf_reader#authors-and-acknowledgment)
* [License](https://gitlab.com/mudlej_android/mj_pdf_reader#authors-and-acknowledgment)

# MJ PDF Reader
MJ PDF Reader is a lightweight and minimalist PDF viewer made by Mudlej (@mudlej).
It's [the successor](https://github.com/JavaCafe01/PdfViewer#anouncement) of PDF Viewer Plus made by Gokul Swaminathan (@JavaCafe01).

For more ([see changelog](https://gitlab.com/mudlej_android/mj_pdf_reader/-/blob/main/change_log.md))

## Screenshots
| Light Mode | Dark Mode |
|:-:|:-:|
| <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/light_framed.png" width="190" height="400"/> | <img src ="https://gitlab.com/mudlej_android/mj_pdf_reader/-/raw/main/screenshots/dark_framed.png" width="190" height="400"/> |

## MJ PDF Reader Features
* Fast & smooth experience.
* Minimalist & simple user interface.
* Remembers the last opened page.
* Dark mode for the app and the PDF.
* True Full Screen Mode.
* Control brightness and enable auto-scrolling within the Full Screen Mode. 
* Search for text in the PDF.
* An option to keep the screen on.
* Open online PDFs through links.
* Share & print PDFs.
* Open multiple PDFs.
* FOSS and totally private

## Permissions and privacy
This app does not collect any data.
The following permissions are req   uired to provide specific features in the app:
* *Internet*: For opening PDFs through links
* *Storage*: For saving downloading PDFs and opening them from storage

## What is new in MJ PDF Reader V1.4.3+
* Big increase in performance, especially for big files.
* Decreased ram usage significantly and eliminated stuttering for big files. 
* Removed the most common causes of crashing.
* More consistent theme across the app. (now, all dialogs use black/white theme)
* Added an option (seekbar) to adjust brightness in the Full Screen Mode 
* Added 'Go To Page' option.
* Search is now available for files of any size.
* Changed App Bar style. (font, color, icons, title max lines)
* Clicking on the title will show a message containing the full name of the pdf.
* Changed scroll handler style.
* Moved 'Print File' to the main menu, and put 'About' to the additional options.
* Relabeled 'Additional Options' as 'More' 
* Disabled Text Mode since it's not usable yet and crashes a lot. 
* Hid page scroll handle if the pdf consists of only one page.
* Improved Copy Page's Text functionality and UI.

## Coming in the next release MJ PDF Reader V1.5.0
* Re-branded the app as 'MJ Reader'
* New and better icon for the app
* [Done] Added support for Hyperlinks.
* [Done] Added a Table of Content page to show the full list of bookmarks (including nested ones).
* [Done] Added an auto scroll feature in the Full Screen Mode (adjustable speed).
* Added an option to see a list of all the links embedded in the file.
* Improved Search functionality
* [Done] Fixed not being able to reset the zoom to a page-width level by double tapping
* [Done] Added a Full Screen button to lock horizontal scrolling
* [Done] Added a Full Screen button to take screenshots
* [Done?] Fixed not saving the last visited page when navigating for some time in the Full Screen Mode.
* [Done] Prevent hiding the Full Screen Buttons and Scroll Handle when the user is interacting with them.

## Things I would like to do for MJ PDF Reader
[See todo](https://gitlab.com/mudlej_android/mj_pdf_reader/-/blob/main/todo.md)

## What is different from PDF Viewer Plus
[See changelog](https://gitlab.com/mudlej_android/mj_pdf_reader/-/blob/main/change_log.md)

## Forking PdfiumAndroid
I Forked [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid) to update its core libraries that were years behind and had too many security vulnerabilities.

* Updated PDFium to 106.0.5241.0 ([source code](https://pdfium.googlesource.com/pdfium/+/refs/heads/main), [building script](https://github.com/bblanchon/pdfium-binaries))
* Updated libpng to 1.6.37 ([source code](https://sourceforge.net/projects/libpng/files/libpng16/1.6.37/), [building script](https://github.com/kota-kota/libpng-build))
* Updated Freetype to 2.12.1 ([source code](https://github.com/freetype/freetype), [building script](https://github.com/kota-kota/freetype-build))

## Text Mode and Search
EDIT: This has changed in v1.4.3, this section talks about the previous versions

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
- The original app (PDF View Plus) was made by Gokul Swaminathan ([@JavaCafe01](https://github.com/JavaCafe01)).
- [@barteksc](https://github.com/barteksc), made the libraries that MJ PDF Reader uses to render PDFs. 
- Credits to (@Derekelkins)'s pull request for adding the ability to remember last opened page.

## License
MJ PDF Reader uses the GPLv3 license, the original app (PDF View Plus) was under MIT license
