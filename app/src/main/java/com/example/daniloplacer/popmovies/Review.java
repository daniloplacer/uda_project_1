package com.example.daniloplacer.popmovies;

/**
 * Created by daniloplacer on 4/1/16.
 */
public class Review {

    private String mAuthor;
    private String mContent;
    private String mUrl;

    public Review(String mAuthor, String mContent, String mUrl) {
        this.mAuthor = mAuthor;
        this.mContent = mContent;
        this.mUrl = mUrl;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
