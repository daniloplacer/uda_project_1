package com.example.daniloplacer.popmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by daniloplacer on 3/20/16.
 */ // Fragment that will Display the additional information about the movie
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String ARGS_MOVIE = "uri";
    private static final String ARGS_TWO_PANE = "two_pane";

    String mMovieId;

    Switch mFavoriteSwitch;
    boolean mOriginalFavoriteValue;
    boolean mNewFavoriteValue;
    boolean mTwoPane;

    LinearLayout mTrailersList;
    LinearLayout mReviewsList;

    // Listener to be called when Fragment is about to close, so can update favorite if needed
    Callback mListener;

    // todo implement return of result depending on two pane mode or not

    public static DetailFragment newInstance(Movie movie) {

        // Creates a new Fragment by passing movie as argds for onCreateView
        Bundle args = new Bundle();

        args.putParcelable(ARGS_MOVIE, movie);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Movie movie = null;
        View rootView = null;

        Bundle args = getArguments();

        // If args != null, then it's tablet, and received movie via newInstance method
        if (args != null) {
            mTwoPane = true;
            movie = args.getParcelable(ARGS_MOVIE);

        } else {
            // If not, activity/fragment was opened by intent

            mTwoPane = false;
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Movie.class.getSimpleName())) {
                movie = intent.getParcelableExtra(Movie.class.getSimpleName());
            }
        }

        if (movie != null) {
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            mMovieId = movie.getId();
            Log.v(LOG_TAG, "Opened Detail view for movie ID: " + mMovieId);

            // Populates views with Movie details

            TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_title_textview);
            detailTextView.setText(movie.getTitle());

            detailTextView = (TextView) rootView.findViewById(R.id.detail_release_date_textview);
            detailTextView.setText(Utility.dateParser(movie.getReleaseDate()));

            detailTextView = (TextView) rootView.findViewById(R.id.detail_rating_textview);
            detailTextView.setText(movie.getRating());

            detailTextView = (TextView) rootView.findViewById(R.id.detail_synopsis_textview);
            detailTextView.setText(movie.getSynopsis());

            ImageView detailImageView = (ImageView) rootView.findViewById(R.id.detail_poster_imageview);
            Picasso.with(getContext()).load(movie.getPoster()).into(detailImageView);

            // Implements logic for click listener of Favorite switch button

            mFavoriteSwitch = (Switch) rootView.findViewById(R.id.detail_favorite_switch);
            mOriginalFavoriteValue = movie.isFavorite();
            mNewFavoriteValue = mOriginalFavoriteValue;
            mFavoriteSwitch.setChecked(mOriginalFavoriteValue);

            // Once the button is clicked, prepare the activity to return the new value
            // of favorite for that movie ID
            mFavoriteSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mNewFavoriteValue = mFavoriteSwitch.isChecked();

                    // If it's not in tablet, sets the result of the activity
                    if (!mTwoPane) {
                        // If not, returns via intent result
                        Intent data = new Intent();
                        data.putExtra(MainActivity.INTENT_MOVIE_ID, mMovieId);
                        data.putExtra(MainActivity.INTENT_NEW_FAVORITE_VALUE, mNewFavoriteValue);
                        getActivity().setResult(Activity.RESULT_OK, data);

                    }
                }
            });

            mTrailersList = (LinearLayout)rootView.findViewById(R.id.trailers_container);
            mReviewsList = (LinearLayout)rootView.findViewById(R.id.reviews_container);

        } else {

            mMovieId = null;
            rootView = inflater.inflate(R.layout.fragment_detail_null, container, false);

        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Utility.isConnected(getActivity()) && mMovieId != null) {
            FetchTrailersTask trailersTask =
                    new FetchTrailersTask(getActivity(), mTrailersList);
            trailersTask.execute(mMovieId);

            FetchReviewsTask reviewsTask =
                    new FetchReviewsTask(getActivity(), mReviewsList);
            reviewsTask.execute(mMovieId);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // If tablet mode, returns via callback
        // Called here to avoid unnecessary changes to database in case
        // user pushes the favorite button multiple times
        if ((mNewFavoriteValue != mOriginalFavoriteValue) && (mTwoPane)) {
            mListener.changeFavoriteValue(mMovieId, mNewFavoriteValue);
        }


    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. Will be used to notify
     */

    public interface Callback {

        void changeFavoriteValue(String movieId, boolean newFavoriteValue);
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
