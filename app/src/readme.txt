Managing content within the app:

- The top 10 tracks for an individual artist are retained in the db so that the user can return
to any screen with data loaded. They are recored onPostExecute in the AsyncTask.
- We check for a cursor that returns more than 0 records in onViewCreated, and load it into the
adapter if so. Otherwise, we run the AsyncTask. This seems to work for rotational shifts, but what
happens when the user presses back and selects a different artist? The results are still in the db,
but we don't want to load those.

/* Doesn't work, breaks rotational */
We clear the db onResume in the ArtistListFragment, since there is  and also, for
whatever reason onDestroy is not called, we'll need to also call it at app creation.


- We populate the db in onPostExecute. When we rotate, we want to retain the results.
    - When we navigate back to the artist list fragment, we want to delete the results.


- The question is, when do we delete the cached tracks? The service will basically always need them
 as long as the app is running


- Test cases:
    Track List Screen:
        - Once the list results have are displaying, turn on airplane mode and rotate. Make sure that all
        data is reloaded.

    Artist List Screen:
        - Go to the track list screen, rotate to landscape, rotate to portrait, back to ArtistList.

    - When navigating to the the currently playing track via the Now Playing button, each rotation adds
    a new fragment