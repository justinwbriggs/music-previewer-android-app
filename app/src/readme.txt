Managing content within the app:

- The top 10 tracks for an individual artist are retained in the db so that the user can return
to any screen with data loaded. They are recored onPostExecute in the AsyncTask.
- We check for a cursor that returns more than 0 records in onViewCreated, and load it into the
adapter if so. Otherwise, we run the AsyncTask. This seems to work for rotational shifts, but what
happens when the user presses back and selects a different artist? The results are still in the db,
but we don't want to load those. We clear the db onDestroy of TrackListFragment, and also, for
whatever reason onDestroy is not called, we'll need to also call it at app creation.


-



- Test cases:
    Track List Screen:
        - Once the list results have are displaying, turn on airplane mode and rotate. Make sure that all
        data is reloaded.