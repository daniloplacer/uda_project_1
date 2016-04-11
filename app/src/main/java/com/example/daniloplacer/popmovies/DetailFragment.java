package com.example.daniloplacer.popmovies;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniloplacer.popmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by daniloplacer on 3/20/16.
 */ // Fragment that will Display the additional information about the movie
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String ARGS_MOVIE = "uri";

    private Movie mMovie;
    private boolean mTwoPane;
    private boolean mIsFavorite;
    private boolean mNeedsUpdate;

    private ArrayList<String> mTrailersArray;
    private ArrayList<Review> mReviewsArray;

    private LinearLayout mTrailersList;
    private LinearLayout mReviewsList;

    static final String STATE_MOVIE = "movie";
    static final String STATE_TWO_PANE = "two_pane";
    static final String STATE_FAVORITE = "favorite";
    static final String STATE_NEEDS_UPDATE = "needs_update";

    static final String STATE_TRAILERS_ARRAY = "trailers_array";
    static final String STATE_REVIEWS_ARRAY = "reviews_array";

    public static DetailFragment newInstance(Movie movie) {

        // Creates a new Fragment by passing movie as args for onCreateView
        Bundle args = new Bundle();

        args.putParcelable(ARGS_MOVIE, movie);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public DetailFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(STATE_MOVIE, mMovie);
        savedInstanceState.putBoolean(STATE_TWO_PANE, mTwoPane);
        savedInstanceState.putBoolean(STATE_FAVORITE, mIsFavorite);
        savedInstanceState.putBoolean(STATE_NEEDS_UPDATE, mNeedsUpdate);
        savedInstanceState.putStringArrayList(STATE_TRAILERS_ARRAY, mTrailersArray);
        savedInstanceState.putParcelableArrayList(STATE_REVIEWS_ARRAY, mReviewsArray);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = null;
        mMovie = null;

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {

            // Gets some values from the bundle
            mMovie = savedInstanceState.getParcelable(STATE_MOVIE);
            mTwoPane = savedInstanceState.getBoolean(STATE_TWO_PANE);
            mIsFavorite = savedInstanceState.getBoolean(STATE_FAVORITE);
            mNeedsUpdate = savedInstanceState.getBoolean(STATE_NEEDS_UPDATE);
            mTrailersArray = savedInstanceState.getStringArrayList(STATE_TRAILERS_ARRAY);
            mReviewsArray = savedInstanceState.getParcelableArrayList(STATE_REVIEWS_ARRAY);

            rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        } else {

            Bundle args = getArguments();

            // If args != null, then it's tablet, and received movie via newInstance method
            if (args != null) {
                mTwoPane = true;
                mMovie = args.getParcelable(ARGS_MOVIE);
            } else {
                // If not, activity/fragment was opened by intent

                mTwoPane = false;
                Intent intent = getActivity().getIntent();
                if (intent != null && intent.hasExtra(Movie.class.getSimpleName())) {
                    mMovie = intent.getParcelableExtra(Movie.class.getSimpleName());
                }
            }

            if (mMovie == null) {

                // if there's no movie to show, shows the appropriate layout and returns
                rootView = inflater.inflate(R.layout.fragment_detail_null, container, false);
                return rootView;

            } else {

                rootView = inflater.inflate(R.layout.fragment_detail, container, false);

                Log.v(LOG_TAG, "Opened Detail view for movie ID: " + mMovie.getId());
                mIsFavorite = mMovie.isFavorite();

                // Populates views with Movie details
                TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_title_textview);
                detailTextView.setText(mMovie.getTitle());

                detailTextView = (TextView) rootView.findViewById(R.id.detail_release_date_textview);
                detailTextView.setText(Utility.dateParser(mMovie.getReleaseDate()));

                detailTextView = (TextView) rootView.findViewById(R.id.detail_rating_textview);
                detailTextView.setText(mMovie.getRating());

                detailTextView = (TextView) rootView.findViewById(R.id.detail_synopsis_textview);
                detailTextView.setText(mMovie.getSynopsis());

                mTrailersArray = null;
                mReviewsArray = null;
                mNeedsUpdate = true;

            }
        }

        ImageView detailImageView = (ImageView) rootView.findViewById(R.id.detail_poster_imageview);
        Picasso.with(getContext()).load(mMovie.getPoster()).into(detailImageView);

        // Implements logic for click listener of Favorite switch button
        Switch favoriteSwitch = (Switch) rootView.findViewById(R.id.detail_favorite_switch);
        favoriteSwitch.setChecked(mIsFavorite);

        // Once the button is clicked, prepare the activity to return the new value
        // of favorite for that movie ID
        favoriteSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    insertFavoriteMovie(mMovie);
                } else {
                    // Otherwise, it changed to false, and needs to remove it from the database
                    deleteFavoriteMovie(mMovie.getId());
                }
            }
        });

        mTrailersList = (LinearLayout) rootView.findViewById(R.id.trailers_container);
        mReviewsList = (LinearLayout) rootView.findViewById(R.id.reviews_container);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mMovie == null)
            return;

        if (mNeedsUpdate) {

            mTrailersArray = new ArrayList<String>();
            mReviewsArray = new ArrayList<Review>();

            if (Utility.isConnected(getActivity())) {
                FetchTrailersTask trailersTask =
                        new FetchTrailersTask(getActivity(), mTrailersList, mTrailersArray);
                trailersTask.execute(mMovie.getId());

                FetchReviewsTask reviewsTask =
                        new FetchReviewsTask(getActivity(), mReviewsList, mReviewsArray);
                reviewsTask.execute(mMovie.getId());
            }

            mNeedsUpdate = false;
        } else {
            addTrailersToContainer();
            addReviewsToContainer();
        }
    }

    public void addReviewsToContainer(){

        mReviewsList.removeAllViews();

        LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

    public void addTrailersToContainer(){

        mTrailersList.removeAllViews();

        LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        getActivity().startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Couldn't call open trailer - no receiving apps installed!");
                    }
                }

            });

            // Adds the new item to the list
            mTrailersList.addView(itemView);
        }
    }

    // =============================================================
    //    INSERT/DELETE MOVIES FROM DATABASE VIA CONTENT PROVIDER
    // =============================================================

    // Adds a movie to the database
    public long insertFavoriteMovie(Movie movie) {
        long movieEntryId;

        // First, check if the movie with this city name exists in the db
        Cursor movieCursor = getActivity().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movie.getId()},
                null);

        // If the movie already exists, returns its DB ID
        if (movieCursor.moveToFirst()) {
            int locationIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            movieEntryId = movieCursor.getLong(locationIdIndex);
        } else {
            // If movie doesn't exist, then insert the values into de DB
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getSynopsis());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPoster());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getRating());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getPopularity());


            // Finally, insert location data into the database.
            Uri insertedUri = getActivity().getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            // The resulting URI contains the ID for the row.  Extract the row ID from the Uri.
            movieEntryId = ContentUris.parseId(insertedUri);
        }

        movieCursor.close();
        return movieEntryId;
    }

    // Removes a movie from the database based on a movie ID
    public int deleteFavoriteMovie(String movieId) {

        int removedRows = getActivity().getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId}
        );

        return removedRows;
    }

}