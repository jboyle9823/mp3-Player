# MP3 Player

An Android Application that plays .MP3 files.

DISCLAIMER: This project was developed for academic purposes, as a course project.


## General Information
- Added .MP3 are placed in a list on the scrollable home screen of the application, with cover art, name and artists displayed.
    - On this screen, users can "long press" a track in order to prompt a pop-up that will delete the track from the list.
- when a track is pressed, the application will change activities to a detailed screen.
    - This screen contains a fuller view of the cover art, name and artist, as well as seek bars for the volume and progress of the track, and buttons to pause/play the track and buttons to play the next/previous track.
- Tracks are played using a Service, which is launched when the details screen for a track is entered.
    - The service uses background playback with MediaSessionService to create a notification tab in order to play the track from outside the application.


## Screenshots
