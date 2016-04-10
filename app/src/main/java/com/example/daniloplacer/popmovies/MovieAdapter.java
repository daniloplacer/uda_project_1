package com.example.daniloplacer.popmovies;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by daniloplacer on 1/23/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, List<Movie> movies) {
        // Calls super constructor, but with no text_view as its not going to be used
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Return a view to be added to an AdapterView (e.g. GridView),
        // in this case, will return the image view to be put on the grid

        Movie item = getItem(position);

        // Tries to recycle the view, but if not existent, creates a new one
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView posterView = (ImageView) convertView.findViewById(R.id.grid_item_movie_imageview);

        posterView.setImageResource(R.drawable.no_poster);
        Picasso.with(getContext()).load(item.getPoster()).into(posterView);

        return convertView;
    }
}