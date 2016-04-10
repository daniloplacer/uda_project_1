package com.example.daniloplacer.popmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Created by daniloplacer on 3/24/16.
 */
public class Utility {

    private static final String DATE_FORMAT_INPUT = "yyyy-mm-dd";
    private static final String DATE_FORMAT_OUTPUT = "MMMM dd, yyyy";

    // Retrieves the preferred sorting from Shared Preferences - popularity, vote average or favorites
    // If not value was set by the user, pref.getString returns the default value
    public static String getPreferredSorting(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String sorting = pref.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_default));

        return sorting;
    }

    public static boolean isConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    // check if its a valid date
    private static boolean  isValidDate(String input) {
        if (input == null)
            return false;

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_INPUT);

        try {
            format.parse(input);
            return true;
        }
        catch(ParseException e){
            return false;
        }
    }

    // Receives a date like "YYYY-MM-DD" and returns a date like defined by DATE_FORMAT
    public static String dateParser(String date){

        if (!isValidDate(date))
            return null;

        StringTokenizer tok = new StringTokenizer(date,"-");
        int year = Integer.parseInt(tok.nextToken());
        int month = Integer.parseInt(tok.nextToken());
        int day = Integer.parseInt(tok.nextToken());

        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_OUTPUT);
        format.setCalendar(calendar);
        return format.format(calendar.getTime());
    }
}