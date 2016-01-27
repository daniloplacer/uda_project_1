package com.example.daniloplacer.popmovies;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by daniloplacer on 1/21/16.
 */
public class GridFragment extends Fragment{

    private final String MOVIE_ARRAY = "MOVIE_ARRAY";
    private final String SORTING = "SORTING";

    private Movie[] moviesArray;
    MovieAdapter movieAdapter;
    private boolean update_needed = false;

    public GridFragment() {
    }

    // This method is called before onCreateView
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            update_needed = true;
        } else {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sorting = pref.getString(getString(R.string.pref_sort_key),
                    getString(R.string.pref_sort_default));

            String previous_sorting = savedInstanceState.getString(SORTING);

            // if previous sorting is different than current sorting, need to update movies
            if (previous_sorting != sorting)
                update_needed = true;
            else {
                update_needed = false;
                moviesArray = (Movie[])savedInstanceState.getParcelableArray(MOVIE_ARRAY);
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (update_needed)
            updateMovies();
    }

    // Saves the Movies array so it can be used on a saved instance
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current array and what sorting was used
        savedInstanceState.putParcelableArray(MOVIE_ARRAY, moviesArray);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sorting = pref.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        savedInstanceState.putString(SORTING, sorting);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // creating a new adapter with mock data above
        movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        // binding adapter to grid view
        GridView grid = (GridView) rootView.findViewById(R.id.movies_grid);
        grid.setAdapter(movieAdapter);

        // Sets a new click listener to create through Intent
        // a new DetailActivity once a movie poster is clicked on
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Movie.class.getSimpleName(), moviesArray[position]);
                startActivity(detailIntent);

            }
        });

        // no need to call updateMovies here because its going to be called at onStart
        return rootView;
    }

    // Method that will call the background task to fetch movies data and update
    // the UI accordingly given a sorting preference from Shared Preferences
    public void updateMovies(){
        FetchMoviesTask task = new FetchMoviesTask();

        // Retrieves value from Shared Preferences
        // If not value was set by the user, pref.getString returns the default value
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sorting = pref.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        task.execute(sorting);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String> {

        // Create this constant just so if the class is renamed, do not need to change constant
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            // Problem with sorting selection
            if (params.length ==0)
                return null;

            Uri builder = Uri.parse(getString(R.string.api_base)).buildUpon()
                    .appendQueryParameter(getString(R.string.api_param_sort_name), params[0])
                    .appendQueryParameter(getString(R.string.api_param_key_name),getString(R.string.api_param_key_value)).build();

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the Movie DB API query
                URL url = new URL(builder.toString());

                // Create the request to Movie DB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Adds new line for debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty, no point in parsing
                    return null;
                }

                forecastJsonStr = buffer.toString();

                return forecastJsonStr;
            } catch (IOException e) {
                // Catches IO error from HTTP connection
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        // Given a Movie DB API Discover JSON, returns an array of Movie objects
        private Movie[] getMoviesFromJSON(String json) {

            // if no movies, returns null
            if (json == null)
                return null;

            JSONArray array = null;
            try {
                array = new JSONObject(json).getJSONArray(getString(R.string.json_array));
            } catch (JSONException e) {
                Log.e(LOG_TAG,"Error creating JSON array from String");
            }
            Movie[] result = new Movie[array.length()];

            JSONObject singleMovie;

            for (int i=0; i<array.length(); i++)
                try {
                    singleMovie = array.getJSONObject(i);

                    result[i] = new Movie(
                            singleMovie.getString(getString(R.string.json_item_title)),
                            singleMovie.getString(getString(R.string.json_item_poster)),
                            singleMovie.getString(getString(R.string.json_item_synopsis)),
                            singleMovie.getString(getString(R.string.json_item_rating)),
                            singleMovie.getString(getString(R.string.json_item_release_date))
                    );

                } catch (JSONException e) {
                    Log.e(LOG_TAG,"Error fetching movie info and creating Movie object");
                }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null){

                // Extracts the Movie[] from the JSON
                moviesArray = getMoviesFromJSON(result);

                // Adds new movies to the adapter
                movieAdapter.clear();
                movieAdapter.addAll(moviesArray);
            }
        }
    }




}
