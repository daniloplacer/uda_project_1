package com.example.daniloplacer.popmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by daniloplacer on 3/28/16.
 */
public class FetchTrailersTask extends AsyncTask<String, Void, String> {

    // Create this constant just so if the class is renamed, do not need to change constant
    private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

    // member variable with context for retrieving String resources
    private final Context mContext;

    private LinearLayout mTrailerList;

    private ArrayList<String> mTrailersArray;

    public FetchTrailersTask(Context context, LinearLayout list, ArrayList<String> trailersArray) {
        mContext = context;
        mTrailerList = list;
        mTrailersArray = trailersArray;
    }

    @Override
    protected String doInBackground(String... params) {

        // Problem with receiving movie ID
        if (params.length == 0)
            return null;

        String movieId = params[0];

        // Builds the URL and Uri based on that movie ID
        String url_path = mContext.getString(R.string.api_base)
                + "/" + movieId + "/"
                + mContext.getString(R.string.api_trailer);

        Uri builder = Uri.parse(url_path).buildUpon()
                .appendQueryParameter(mContext.getString(R.string.api_param_key_name), mContext.getString(R.string.api_param_key_value))
                .build();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string
        String trailersJsonStr = null;

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

            trailersJsonStr = buffer.toString();

            return trailersJsonStr;
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

    // Given a Movie DB API movie/id/videos JSON, returns an array of trailer URLs on YouTube
    private String[] getTrailersFromJSON(String json) {

        // if no trailers, returns null
        if (json == null)
            return null;

        JSONArray array = null;
        try {
            array = new JSONObject(json).getJSONArray(mContext.getString(R.string.json_trailer_array));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error creating JSON array from String");
        }

        ArrayList<String> result = new ArrayList<>();

        JSONObject singleTrailer;

        String trailerValue = mContext.getString(R.string.json_trailer_type_value_trailer);
        String youTubeValue = mContext.getString(R.string.json_trailer_site_value_youtube);
        String youTubeBaseURL = mContext.getString(R.string.youtube_base_url);

        for (int i = 0; i < array.length(); i++)
            try {
                singleTrailer = array.getJSONObject(i);

                // Checks if it's indeed a trailer
                if (singleTrailer
                        .getString(mContext.getString(R.string.json_trailer_type))
                        .compareTo(trailerValue) == 0) {

                    // Checks if it' a YouTube video
                    if (singleTrailer
                            .getString(mContext.getString(R.string.json_trailer_site))
                            .compareTo(youTubeValue) == 0) {

                        // if it's, builds the URL and then adds to the result array list
                        result.add(youTubeBaseURL +
                                singleTrailer.getString(
                                        mContext.getString(R.string.json_trailer_key)));
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error fetching trailer info from JSON");
            }

        return result.toArray(new String[result.size()]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        String[] array = null;

        // If was able to return results from the internet...
        if (result != null) {
            // Extracts the Movie[] from the JSON
            array = getTrailersFromJSON(result);

            // updates trailers fetched
            Log.v(LOG_TAG,
                    "Number of trailers fetched from the internet: "
                            + array.length);

            mTrailersArray.clear();
            mTrailersArray.addAll(Arrays.asList(array));

            addTrailersToContainer();

        } else {
            Log.e(LOG_TAG,"Was NOT able to read trailer data from the internet");
        }
    }

    public void addTrailersToContainer(){

        mTrailerList.removeAllViews();

        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i=0; i<mTrailersArray.size(); i++){
            // Inflates the list item
            View itemView = inflater.inflate(R.layout.list_item_trailer, null);

            TextView textView = (TextView)itemView.findViewById(R.id.list_item_trailer_textview);
            int textPosition = i+1;
            textView.setText("Trailer " + textPosition);

            // Uses setTag as the "memory" for this view when calling click listener
            itemView.setTag(mTrailersArray.get(i));

            // Sets a new click listener to open a trailer via Intent
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String trailerURL = (String) v.getTag();

                    Uri playVideo = Uri.parse(trailerURL)
                            .buildUpon()
                            .build();

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(playVideo);

                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Couldn't call open trailer - no receiving apps installed!");
                    }
                }

            });

            // Adds the new item to the list
            mTrailerList.addView(itemView);
        }
    }
}