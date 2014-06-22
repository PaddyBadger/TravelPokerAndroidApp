package com.travelpoker.app.objects;

/**
 * Created by patriciaestridge on 4/14/14.
 */
public class DeckGalleryItem {
    private String mTitle;
    private String mId;
    private String mUrl;
    private String mDescription;
    private String mLongitude;
    private String mLatitude;

    public String toString() {
        return mTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getId() {
        return mId;
    }

    public String getDeckPageUrl() {
        return "http://www.travelpoker.co/decks/" + mId + ".xml";
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        this.mLongitude = longitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        this.mLatitude = latitude;
    }
}
