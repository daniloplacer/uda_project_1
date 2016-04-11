package com.example.daniloplacer.popmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
 * Created by daniloplacer on 3/28/16.
 */
public class FetchReviewsTask extends AsyncTask<String, Void, String> {

    // Create this constant just so if the class is renamed, do not need to change constant
    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    // member variable with context for retrieving String resources
    private final Context mContext;

    private LinearLayout mReviewsList;

    private ArrayList<Review> mReviewsArray;

    public FetchReviewsTask(Context context, LinearLayout list, ArrayList<Review> reviewsArray) {
        mContext = context;
        mReviewsList = list;
        mReviewsArray = reviewsArray;
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
                + mContext.getString(R.string.api_reviews);

        Uri builder = Uri.parse(url_path).buildUpon()
                .appendQueryParameter(mContext.getString(R.string.api_param_key_name), mContext.getString(R.string.api_param_key_value))
                .build();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string
        String reviewsJsonStr = null;

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

            reviewsJsonStr = buffer.toString();

            return reviewsJsonStr;
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

    // Given a Movie DB API movie/id/reviews JSON, returns an array of reviews (author, content, URL)
    private Review[] getReviewsFromJSON(String json) {

        // if no reviews, returns null
        if (json == null)
            return null;

        JSONArray array = null;
        try {
            array = new JSONObject(json).getJSONArray(mContext.getString(R.string.json_review_array));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error creating JSON array from String");
        }

        Review[] result = new Review[array.length()];

        JSONObject singleReview;

        for (int i = 0; i < array.length(); i++)
            try {
                singleReview = array.getJSONObject(i);

                result[i] = new Review(
                        singleReview.getString(mContext.getString(R.string.json_review_author)),
                        singleReview.getString(mContext.getString(R.string.json_review_content)),
                        singleReview.getString(mContext.getString(R.string.json_review_url)));

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error fetching reviews info from JSON");
            }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Review[] array = null;

        // If was able to return results from the internet...
        if (result != null) {
            // Extracts the Review[] from the JSON
            array = getReviewsFromJSON(result);

            // updates reviews fetched
            Log.v(LOG_TAG,
                    "Number of reviews fetched from the internet: "
                            + array.length);

            mReviewsArray.clear();
            mReviewsArray.addAll(Arrays.asList(array));

            addReviewsToContainer();

        } else {
            Log.e(LOG_TAG,"Was NOT able to read reviews data from the internet");
        }
    }

    public void addReviewsToContainer(){

        mReviewsList.removeAllViews();

        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i=0; i<mReviewsArray.size(); i++) {
            // Inflates the list item
            View itemView = inflater.inflate(R.layout.list_item_review, null);

            Review review = mReviewsArray.get(i);

            // Sets the author and content text views
            TextView textView = (TextView)itemView.findViewById(R.id.list_item_review_author_textview);
            textView.setText(review.getAuthor());

            textView = (TextView)itemView.findViewById(R.id.list_item_review_content_textview);
            textView.setText(review.getContent());

            // Adds the new item to the list
            mReviewsList.addView(itemView);
        }
    }
}