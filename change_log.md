* 2.0.2
  * Prevented crashing when a user clicks on show more for text results when another one is expanding.
  * DEV: Updated PDFium lib to 112.0.5579.0
  * DEV: Updated Libpng lib to 1.6.39
  * DEV: Updated FreeType lib to 2.13.0
  * DEV: added ~500 lines of scripts to fetch, build and copy all of the dependencies and native code with a single command.
  * Added the missing "page fling" setting in the settings.

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