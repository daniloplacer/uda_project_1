package com.example.daniloplacer.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Created by daniloplacer on 1/23/16.
 */
public class Movie implements Parcelable {

    private String mId;
    private String mTitle;
    private String mPoster;
    private String mSynopsis;
    private String mRating;
    private String mReleaseDate;
    private double mPopularity;
    private boolean mFavorite;

    private final String BASE = "http://image.tmdb.org/t/p/";
    private final String SIZE = "w185";

    public Movie(String id, String title, String poster, String synopsis,
                 String rating, String releaseDate, double popularity, boolean favorite){
        this.mId = id;
        this.mTitle = title;

        this.mSynopsis = synopsis;
        this.mRating = rating;

        if ((poster == "") || (poster == null))
            this.mPoster = null;
        else
            this.mPoster = BASE + SIZE + poster;

        this.mReleaseDate = releaseDate;
        this.mPopularity = popularity;
        this.mFavorite = favorite;
    }

    public String getId() {
        return mId;
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

    public double getPopularity() {
        return mPopularity;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.mFavorite = favorite;
    }

    // METHODS RELATED TO PARCELABLE IMPLEMENTATION

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mId);
        out.writeString(mTitle);
        out.writeString(mPoster);
        out.writeString(mSynopsis);
        out.writeString(mRating);
        out.writeString(mReleaseDate);
        out.writeDouble(mPopularity);
        out.writeByte((byte) (mFavorite? 1:0));
    }

    private Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mPoster = in.readString();
        mSynopsis = in.readString();
        mRating = in.readString();
        mReleaseDate = in.readString();
        mPopularity = in.readDouble();
        mFavorite = in.readByte() != 0;
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
