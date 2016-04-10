package com.example.daniloplacer.popmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.Arrays;

/**
 * Created by daniloplacer on 3/19/16.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, String> {

    // Create this constant just so if the class is renamed, do not need to change constant
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    // member variable with context for retrieving String resources
    private final Context mContext;

    // member variables for the Movie Array and Movie Adapter so they can be
    // updated once the data download is completed
    private ArrayList<Movie> mMoviesArray;
    private Movie[] mMoviesFavoritesArray;
    private MovieAdapter mMovieAdapter;

    public FetchMoviesTask(
            Context context,
            ArrayList<Movie> array,
            Movie[] favoritesArray,
            MovieAdapter adapter) {

        mContext = context;
        mMoviesArray = array;
        mMoviesFavoritesArray = favoritesArray;
        mMovieAdapter = adapter;
    }

    @Override
    protected String doInBackground(String... params) {

        // Problem with sorting selection
        if (params.length == 0)
            return null;

        // Builds the URL for API call based on sorting selection param[0]
        String url_path = mContext.getString(R.string.api_base) + "/" + params[0];

        Uri builder = Uri.parse(url_path).buildUpon()
                .appendQueryParameter(mContext.getString(R.string.api_param_key_name), mContext.getString(R.string.api_param_key_value))
                .build();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string
        String moviesJsonStr = null;

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

            moviesJsonStr = buffer.toString();

            return moviesJsonStr;
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
            array = new JSONObject(json).getJSONArray(mContext.getString(R.string.json_movie_array));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error creating JSON array from String");
        }
        Movie[] result = new Movie[array.length()];

        JSONObject singleMovie;

        for (int i = 0; i < array.length(); i++)
            try {
                singleMovie = array.getJSONObject(i);

                result[i] = new Movie(
                        singleMovie.getString(mContext.getString(R.string.json_movie_item_id)),
                        singleMovie.getString(mContext.getString(R.string.json_movie_item_title)),
                        singleMovie.getString(mContext.getString(R.string.json_movie_item_poster)),
                        singleMovie.getString(mContext.getString(R.string.json_movie_item_synopsis)),
                        singleMovie.getString(mContext.getString(R.string.json_movie_item_rating)),
                        singleMovie.getString(mContext.getString(R.string.json_movie_item_release_date)),
                        singleMovie.getDouble(mContext.getString(R.string.json_movie_item_popularity)),
                        false
                );

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error fetching movie info and creating Movie object");
            }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Movie[] array = null;

        // If was able to return results from the internet...
        if (result != null) {
            // Extracts the Movie[] from the JSON
            array = getMoviesFromJSON(result);

            Log.v(LOG_TAG,
                    "Number of movies read from the internet: "
                            + array.length);

            // updates favorite movies data
            int numFavoritesUpdate = updateFavorites(array);
            Log.v(LOG_TAG,
                    "Number of fetched movies with favorite data set: "
                            + numFavoritesUpdate);

            // Adds new movies to the adapter (only works with Array type)
            mMovieAdapter.clear();
            mMovieAdapter.addAll(array);

            mMoviesArray.clear();
            mMoviesArray.addAll(Arrays.asList(array));
        } else {
            Log.e(LOG_TAG,"Was NOT able to read movie data from the internet");
        }
    }

    // updates the favorite movies from the array of movies received by parameter
    // based on list of movies from mMoviesFavoriteArray
    private int updateFavorites(Movie[] array) {
        int count = 0;

        for (int i=0; i<mMoviesFavoritesArray.length; i++) {
            Movie favorite = mMoviesFavoritesArray[i];

            for (int j=0; j<array.length; j++) {
                Movie movie = array[j];

                if (movie.getId().compareTo(favorite.getId()) == 0) {
                    movie.setFavorite(true);
                    count++;
                    break;
                }
            }
        }

        return count;
    }
}