* 2.2.1
  * Avoid crashing because of "lateinit property actionBarMenu has not been initialized".
* 2.2.0
  * Multilingual Support: Added initial support for Arabic, Chinese, Turkish, German, Spanish, Brazilian Portuguese, Hindi, and Russian.
  * Orientation and Theme Options: Added an option to always use the app in horizontal mode.
  * Search and Highlighting Enhancements:
  * Added a button to return back to Search Results at the same position.
  * Improved search result highlighting for better visibility and accuracy.
  * Hide search icon when there are no results to maintain a cleaner interface.
  * Enabled the use of password-protected PDF files for Link, Bookmarks, TextMode, and Search functionalities.
  * Added support for using Text Mode, Search, Table of Contents, and Links with non-local PDF files.
  * Corrected the display of PDF names from an SMB server.
  * Implemented an experimental setting to show a 'reload PDF file' button in the action bar.
  * Ensured Action Bar Buttons and options that require a loaded PDF file are hidden when there is no file loaded.
  * Fix back button not working in some cases.
  * Ensured icon colors are consistent throughout the app.
  * Made background and text colors on Full Screen (FS) Buttons, Scroll handle, and seekbar consistent with the system theme.
  * Adjusted the scroll handle text color for better visibility in dark mode.
  * Enhanced the Full Screen Buttons layout to minimize padding and margins, improving usability beside PDF views.
  * Fixed brightness seekbar overlap with FS buttons list.
  * Improved the display of the reading progress text view to better match the app's current style.
  * Fix clicking on the page ScrollHandle not displaying the GoTo popup. (Android < 12 only)
  * Orientation and Theme Options: Resolved issues with the Dracula theme not persisting in Text Mode.
  * Computed file hash in the background to avoid crashes related to android.os.NetworkOnMainThreadException.
  * Addressed many crash scenarios related to null pointers and file format issues.
  * Attempted to resolve the issue with the back button not exiting the app properly.
  * Initialized pdfExtractor in the background, improving app performance.
  * Upgraded to Android SDK 34 and Gradle v8.3.2, including some slight refactoring.
  * Updated libraries and several major gradle versions.
  * Upgraded dependencies for the core libs (PdfiumAndroid & AndroidPdfViewer) to the latest versions.
  * Fixed settings to ensure proper functionality of the app post-compilation.
  * Removed all unused resources to streamline the project.
  * Refactored the creation of PdfExtractor for easier debugging and maintenance.
  * Note: Major updates to core libraries mark significant progress, resolving long-standing issues.
* 2.1.0
  * Material 3 design. 
  * Redesigned many UI parts and pages. 
  * App follows system theme by default. 
  * Simpler AutoScroll & Brightness buttons. 
  * Add labels to FullScreen Buttons. 
  * Add Zoom Lock button in FullScreen Mode. 
  * Added an option to save PDF password for protected files. 
  * Added an option to let PDF pages follow in settings. (Opt-in) 
  * Added an option to disable double tap in settings.
  * Added an option to switch to FullScreen mode automatically in settings.
  * Can zoom out less than 1x. 
  * Changed Max Zoom In to 10 instead of 5. 
  * Set Max Zoom In to 100 in Adv Config.
  * Improved Double Tap to Zoom in all scenarios.
  * Fixed: crashing when a user clicks on show more in search results when another one is expanding.
  * Fixed: the missing page fling setting in the settings.
  * Fixed: the second top bar hiding part of the page.
  * DEV: Switch to OpenJDK 11
  * DEV: Updated PDFium lib to 117.0.5921.0
  * DEV: Updated Libpng lib to 1.6.39
  * DEV: Updated FreeType lib to 2.13.0
  * DEV: added ~500 lines of scripts to fetch, build and copy all of the dependencies and native code with a single command.

* 2.0.1
  * Fixed back button not working in Bookmarks Activity.
  * Fixed displaying search results incorrectly.
  * Added the option to expand the text of a search result.
  * Added an option to switch to a dark theme (dracula theme) for the text and color in Text Mode.

* V2.0.0
  * Rebranded the app as MJ PDF with a new original icon.
  * Search has become blazingly fast.
  * You can search the the results of a search.
  * Added support for Hyperlinks.
  * Added a Table of Content page.
  * Added a page to see a list of all the links embedded in the file.
  * Added Text Mode to view the PDF as text. (configurable text size and color)
  * Added auto scrolling. (adjustable speed, both direction).
  * Added a button to lock horizontal scrolling.
  * Added a button to take a screenshot.
  * Added a second top bar with seven shortcuts. (hidden by default)
  * Added icons to all menu items in all pages.
  * Clicking on the scroll handle shows the 'Go To Page' dialog.
  * Prevent accidental back pressing by required double press to exit.
  * Decreased app's size by 27.5%. It became 5.1 Megabytes.
  * Fixed not remembering the last visited page sometimes.
  * Fixed hiding the Buttons and Scroll Handle while the user is still interacting with them.
  * Fixed not being able to reset the zoom to a page-width level by double tapping
  * Fixed few common crashes.
  * Fixed no stopping auto scrolling when the user exit the Full Screen Mode.

* V1.4.3+
  * Big increase in performance, especially for big files.
  * Removed the most common causes of crashing.
  * Decreased ram usage significantly.
  * Added 'Go To Page' option.
  * Added an option (seekbar) to adjust brightness in the Full Screen Mode
  * Search is now available for files of any
  * Better and more consistent theme across the app.
  * Changed App Bar style. (font, color, icons, title max lines)
  * Clicking on the title will show a message containing the full name of the pdf.
  * Changed scroll handler style.
  * Moved 'Print File' to the main menu, and put 'About' to the additional options.
  * Relabeled 'Additional Options' as 'More'
  * Disabled Text Mode since it's not usable yet and crashes a lot.
  * Hid page scroll handle if the pdf consists of only one page.
  * Improved Copy Page's Text functionality and UI.

* V1.4.2
    * Add an option to turn the page using volume buttons.
    * Add a button to disable copy page text pop up on long press.
    * Fix NumberFormatException when local use comma for decimal point.
  
* V1.4.1
    * A workaround to prevent app from crashing when opening huge files.
  
* V1.4.0
    * Updated the core libraries and fixed the security issue.
    * Added Search functionality. (experimental) ([see Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text-mode-and-search))
    * Added Text mode to view PDFs like E-readers. (experimental) ([see Text Mode and Search](https://gitlab.com/mudlej_android/mj_pdf_reader#text-mode-and-search))
    * Added the ability to copy text from the PDF via a dialog.
    * Reorganized action bar's options and added Additional Options.