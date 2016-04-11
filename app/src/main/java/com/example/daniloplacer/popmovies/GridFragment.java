package com.example.daniloplacer.popmovies;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.daniloplacer.popmovies.data.MovieContract;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by daniloplacer on 1/21/16.
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = GridFragment.class.getSimpleName();

    private ArrayList<Movie> moviesArray;
    MovieAdapter movieAdapter;
    // In case detail page is opened, favorite value may change, so refreshes the data
    private boolean mNeedsRefresh;
    private String mPreviousSorting;

    static final String STATE_MOVIES_ARRAY = "movies_array";
    static final String STATE_NEEDS_REFRESH = "needs_refresh";
    static final String STATE_PREVIOUS_SORTING = "previous_sorting";

    private static final int MOVIE_LOADER_ID = 0;

    // Column order that will be returned from the DB to the loader
    private static final String[] FORECAST_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };

    // These indices are tied to MOVIE_COLUMNS (must change if array order changes)
    static final int COL_MOVIE_ID_INDEX = 0;
    static final int COL_TITLE_INDEX = 1;
    static final int COL_POSTER_PATH_INDEX = 2;
    static final int COL_OVERVIEW_INDEX = 3;
    static final int COL_VOTE_AVERAGE_INDEX = 4;
    static final int COL_POPULARITY_INDEX = 5;
    static final int COL_RELEASE_DATE_INDEX = 6;

    // Listener to be called when an item is selected, and inform the parent Activity
    Callback mListener;

    public GridFragment() {}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(STATE_MOVIES_ARRAY, moviesArray);
        savedInstanceState.putBoolean(STATE_NEEDS_REFRESH, mNeedsRefresh);
        savedInstanceState.putString(STATE_PREVIOUS_SORTING, mPreviousSorting);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateMovies(){
        Log.v(LOG_TAG, "Will start updating movies");
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNeedsRefresh ||
                (Utility.getPreferredSorting(getActivity()).compareTo(mPreviousSorting) !=0)) {

            mNeedsRefresh = false;
            mPreviousSorting = Utility.getPreferredSorting(getActivity());
            updateMovies();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {

            mNeedsRefresh = savedInstanceState.getBoolean(STATE_NEEDS_REFRESH);
            mPreviousSorting = savedInstanceState.getString(STATE_PREVIOUS_SORTING);

            // In case changed sorting, needs to refresh data
            if (Utility.getPreferredSorting(getActivity()).compareTo(mPreviousSorting) !=0) {
                mNeedsRefresh = true;
            }

            if (mNeedsRefresh) {
                movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
                updateMovies();
            } else {
                moviesArray = savedInstanceState.getParcelableArrayList(STATE_MOVIES_ARRAY);
                movieAdapter = new MovieAdapter(getActivity(), moviesArray);
            }

        } else {
            Log.v(LOG_TAG, "Creating a new view");
            movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
            updateMovies();
        }

        mNeedsRefresh = false;
        mPreviousSorting = Utility.getPreferredSorting(getActivity());
        // binding adapter to grid view
        GridView grid = (GridView) rootView.findViewById(R.id.movies_grid);
        grid.setAdapter(movieAdapter);

        // Sets a new click listener to create through Intent
        // a new DetailActivity once a movie poster is clicked on
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mNeedsRefresh = true;
                mListener.onItemSelected(moviesArray.get(position));
            }

        });

        return rootView;
    }

    // ======================================================
    //                LOADER RELATED METHODS
    // ======================================================

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "Creating loader");

        String prefSorting = Utility.getPreferredSorting(getActivity());
        String sortOrderDB;

        // Gets the right column to sort on depending on preferred settings
        if (prefSorting.compareTo(getString(R.string.pref_sort_popular_value)) ==0)
            sortOrderDB = MovieContract.MovieEntry.COLUMN_POPULARITY;
        else
            sortOrderDB = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;

        sortOrderDB = sortOrderDB + " DESC";

        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrderDB);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG,"Loader finished to load");

        moviesArray = new ArrayList<Movie>();

        // Once loading from DB finishes, creates an Array with info from offline favorite movies
        Movie[] moviesFavoritesArray = new Movie[data.getCount()];

        Movie movie;

        // Iterates through the data returned from DB, and update the array with favorites
        // REMINDER: we only store favorite movies on the database!
        int i=0;
        while (data.moveToNext()) {
            movie = new Movie(
                    data.getString(COL_MOVIE_ID_INDEX),
                    data.getString(COL_TITLE_INDEX),
                    data.getString(COL_POSTER_PATH_INDEX),
                    data.getString(COL_OVERVIEW_INDEX),
                    data.getString(COL_VOTE_AVERAGE_INDEX),
                    data.getString(COL_RELEASE_DATE_INDEX),
                    data.getDouble(COL_POPULARITY_INDEX),
                    true
            );

            moviesFavoritesArray[i] = movie;
            i++;
        }

        Log.v(LOG_TAG, "Favorite movies read from the database: " + data.getCount());

        boolean favoriteMode = false;

        if (!Utility.isConnected(getActivity())){
            favoriteMode = true;
            Log.v(LOG_TAG, "Connected to the internet: NO");

            String message = "No internet connection.";
            if (moviesFavoritesArray.length > 0) {
                message = message + " Loading favorites.";
            } else {
                message = message + " No favorites selected yet.";
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        } else {
            Log.v(LOG_TAG, "Connected to the internet: YES");

            if (Utility.getPreferredSorting(getActivity())
                    .compareTo(getString(R.string.pref_sort_favorites_value)) == 0) {
                favoriteMode = true;
            }
        }

        // If is online, loads movies from internet
        if (favoriteMode){
            Log.v(LOG_TAG, "Favorite mode. Will show movies from the database");

            // otherwise, show favorites only
            moviesArray.clear();
            moviesArray.addAll(Arrays.asList(moviesFavoritesArray));

            // Adds new movies to the adapter (only works with Array type)
            movieAdapter.clear();
            movieAdapter.addAll(moviesFavoritesArray);

        } else {
            Log.v(LOG_TAG, "Not in Favorite mode. Will get movies from the internet.");
            // After movies are loaded, starts fetching movies from internet
            FetchMoviesTask task =
                    new FetchMoviesTask(getActivity(), moviesArray, moviesFavoritesArray, movieAdapter);

            task.execute(Utility.getPreferredSorting(getActivity()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // No action for now
        Log.v(LOG_TAG,"Loader reset");
    }

    // ========== METHODS FOR SUPPORT FOR TABLES ===========

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item selections.
     */

    public interface Callback {

        void onItemSelected(Movie movie);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Tries to cast Activity to Callback, to confirm that it implemented the
        // interface and to be able to execute the callback later
        try {
            mListener = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement Callback");
        }
    }

}
