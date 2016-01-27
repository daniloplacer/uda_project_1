package com.example.daniloplacer.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Created by daniloplacer on 1/23/16.
 */
public class Movie implements Parcelable {

    private String mTitle;
    private String mPoster;
    private String mSynopsis;
    private String mRating;
    private String mReleaseDate;

    private final String BASE = "http://image.tmdb.org/t/p/";
    private final String SIZE = "w185";

    private final String DATE_FORMAT = "MMMM dd, yyyy";

    public Movie(String title, String poster, String synopsis, String rating, String releaseDate){
        this.mTitle = title;
        this.mPoster = BASE + SIZE + poster;
        this.mSynopsis = synopsis;
        this.mRating = rating;
        this.mReleaseDate = dateParser(releaseDate);
    }

    // Receives a date like "YYYY-MM-DD" and returns a date like defined by DATE_FORMAT
    private String dateParser(String date){
        StringTokenizer tok = new StringTokenizer(date,"-");
        int year = Integer.parseInt(tok.nextToken());
        int month = Integer.parseInt(tok.nextToken());
        int day = Integer.parseInt(tok.nextToken());

        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setCalendar(calendar);
        return format.format(calendar.getTime());
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPoster() {
        return mPoster;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String getRating() {
        return mRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    // METHODS RELATED TO PARCELABLE IMPLEMENTATION

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mPoster);
        out.writeString(mSynopsis);
        out.writeString(mRating);
        out.writeString(mReleaseDate);
    }

    private Movie(Parcel in) {
        mTitle = in.readString();
        mPoster = in.readString();
        mSynopsis = in.readString();
        mRating = in.readString();
        mReleaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

}
