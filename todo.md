## TODO

### Urgent

### Not Urgent
- [ ] Adding an option to crop margins.
- [ ] Add the ability to expand search results.
- [ ] add a 'go to page' like Librera's
- [ ] Adding an option to disable zooming in Full Screen Mode.
- [ ] Adding an option to disable double-tap or customize it in the settings.
- [ ] Adding highlight functionality.
- [ ] fix search for a sentence across pages
- [ ] Add search in bookmarks activity
- [ ] Add expand / collapse all in bookmarks activity
- [ ] add an option to export saved reading progress
- [ ] make Full Screen Mode utilize 100% of the screen. (even around the notch)
- [ ] clicking on autoscroll speed textView shows a dialog to enter it manually
- [ ] "only thing i'd like to have an option on is tap for next page." could be added as an option in the bottom bar.6
- [ ] Prevent crashing when opening large files for a long time. (400MB+, 30M+)

## DONE
- [x] activate appDarkTheme in MainActivity
- [x] Add a workaround for the dark theme issue (https://github.com/barteksc/AndroidPdfViewer/issues/914)
- [x] Fix Page Length TextView count
- [x] Make fullscreen buttons visible on all backgrounds
- [x] show line number when search result dialog show up
- [x] make showing snackbar when finished extraction an opt-in option
- [x] add an option in settings to disable that a long press show copy page text dialog
- [x] adding table of contents option
- [x] adding text mode
- [x] fix indexesOf regex pattern getting unwanted special meaning 
- [x] disable copy button in copy text dialog when there is no text in page
- [x] change title in search action bar when finished to indicate the status whether extracting or finished
- [x] update app info to talk about v2, text mode, search ... etc
- [x] update about activity
- [x] Add copy page's text option from action bar
- [x] Fix Text Mode textView being shifted upwards.
- [x] loading TextMode Activity blocks UI and can't dismiss the dialog until finished
- [x] don't disable copy page's text option in action bar when long press is disabled in settings
- [x] Reloading MainActivity restart text extraction, this should be solved someway
- [x] scrolling using the volume keys
- [x] add 'don't pop up in copy page dialog if shown by long press
- [x] fix PdfBox-Android can't deal with big files in the background
- [x] fix not show copy dialog on first long press
- [x] change page handler theme to suit the app
- [x] add the ability to view the full file name of the pdf as a toast message by long pressing the title
- [x] Add 'continue in the background'
- [x] hide page handle when there is only one page
- [x] Refactor the code for Brightness and Auto Scroll buttons
- [x] add brightness control in fullscreen mode
- [x] Add auto-scroll option in Full Screen Mode.
- [x] Adding the ability to click on hyperlinks.
- [x] add an option to lock horizontal swipe
- [x] fin only showing the top level bookmarks and not the nested ones
- [x] fix dialogs theme in dark mode
- [x] Add textView nea3r the auto-scroll option to see the speed.
- [x] double tapping should reset zoom to page-width zoom
- [x] change bookmarks to table of contents
- [x] Clicking on Scroll Handle show 'Go To Page' dialog.
- [x] (dev) Moved the unresolved errors out of the MainActivity
- [x] Prevented hiding the Full Screen Buttons when the user is interacting with them.
- [x] Prevented hiding the scroll handle while touching it.
- [x] Fixed Horizontal Swipe lock color in dark theme
- [x] Prevented Auto Scrolling and Horizontal Swipe locking from preserving their effects in the Normal Mode.
- [x] Fixed not saving the last page you visited when navigating for some time in the Full Screen Mode.
- [x] Used PDFium for search the text of the PDF.
- [x] make getting all the bookmarks done in the background
- [x] add toggle to show a second bar
- [x] Fix app's icon ratio
- [x] create a scrollbar for textView in Text Mode
- [x] changing font size in text mode takes too much and freezes UI thread
- [x] Add links list dialog.
- [x] Stop Auto-Scrolling when the user exit the Full Screen Mode.
 
## IGNORED
- [ ] add a pop up after enabling Dark Mode for app to enable it for pdf as well
 
## To DO in MJ Reader
- [ ] Remember the user chose speed for auto scrolling for each book.
- [ ] Add a home activity with multiple pages (PDFs folders, Recent, Favorite, want to read, finished)
- [ ] Items in each page has an info card similar to that in Sebaq.
