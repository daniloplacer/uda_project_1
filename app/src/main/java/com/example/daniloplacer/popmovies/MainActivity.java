package com.example.daniloplacer.popmovies;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity
        implements GridFragment.Callback {

    // Change onActivityResult for a callback, and update Favorite movie when in tablet mode

    private final String DETAILFRAGMENT_TAG = "DETAIL_FRAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if detail_container was created - if so, it has two panes
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.movie_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG);
                ft.commit();
            }

        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // When item is selected, either opens a new activity via Intent,
    // or opens a new fragment on the 2nd container if it's a tablet
    @Override
    public void onItemSelected(Movie movie) {

        // If tablet mode, replaces existing DetailFragment
        if (mTwoPane) {

            DetailFragment detailFragment = DetailFragment.newInstance(movie);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.movie_detail_container, detailFragment, DETAILFRAGMENT_TAG);
            ft.commit();

        } else {

            // If not, simply starts the new Activity
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(Movie.class.getSimpleName(), movie);
            startActivity(detailIntent);

        }

    }

}
