## TODO
- [ ] fix not show copy dialog on first long press
- [ ] fix search for a sentence across pages
- [ ] hide page handle when there is only one page
- [ ] make copy page's text option in action bar more powerful (specify page number in name or choose it in dialog)
- [ ] improve the app logo image
- [ ] Reloading MainActivity restart text extraction, this should be solved someway
- [ ] create a scrollbar for textView in Text Mode
- [ ] changing font size in text mode takes too much and freezes UI thread

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

## IGNORED
- [ ] adding a fullscreen button to restore default zoom
- [ ] elaborate the saving error message and suggest storage permission