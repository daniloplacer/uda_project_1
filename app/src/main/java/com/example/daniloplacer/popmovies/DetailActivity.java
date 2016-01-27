package com.example.daniloplacer.popmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by daniloplacer on 1/23/16.
 */
public class DetailActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailFragment())
                    .commit();
        }
    }

    // Fragment that will Display the additional information about the movie
    public static class DetailFragment extends Fragment {

        public DetailFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // When the Fragment is created, gets movie info from Intent,
            // and shows its info through the Fragments components
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Movie.class.getSimpleName())){

                // Retrieves the Movie object sent via intent, which is a Parcelable
                Movie movie = (Movie) intent.getParcelableExtra(Movie.class.getSimpleName());

                TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_title_textview);
                detailTextView.setText(movie.getTitle());

                detailTextView = (TextView) rootView.findViewById(R.id.detail_release_date_textview);
                detailTextView.setText(movie.getReleaseDate());

                detailTextView = (TextView) rootView.findViewById(R.id.detail_rating_textview);
                detailTextView.setText(movie.getRating());

                detailTextView = (TextView) rootView.findViewById(R.id.detail_synopsis_textview);
                detailTextView.setText(movie.getSynopsis());

                ImageView detailImageView = (ImageView) rootView.findViewById(R.id.detail_poster_imageview);
                Picasso.with(getContext()).load(movie.getPoster()).into(detailImageView);
            }

            return rootView;
        }
    }
}
