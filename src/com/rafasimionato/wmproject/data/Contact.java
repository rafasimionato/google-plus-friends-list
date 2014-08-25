package com.rafasimionato.wmproject.data;

import android.util.Log;

/**
 * This is simple class to keep each contact information in the same data
 * structure.
 * 
 * @author Rafael Simionato
 */
public class Contact {

    private static final String TAG = "Contact";

    private final String _ID;
    private final String name;
    private final String profileUrl;
    private final String pictureUrl;

    public Contact(String _ID, String name, String profileUrl, String pictureUrl) {

        this._ID = _ID;
        this.name = name;
        this.profileUrl = profileUrl;

        if (pictureUrl != null) {
            int lastIndexForRedimensioning = pictureUrl.lastIndexOf("?sz=");
            this.pictureUrl = lastIndexForRedimensioning < 0 ? pictureUrl : pictureUrl.substring(0,
                    lastIndexForRedimensioning);
        } else {
            this.pictureUrl = null;
        }

    }

    /**
     * It returns the Google+ profile picture URL for the contact.
     */
    public String getPictureUrl() {
        return pictureUrl;
    }

    /**
     * It returns the display name for the contact.
     */
    public String getName() {
        return name;
    }

    /**
     * It returns the Google+ profile name for the contact.
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append("ID : " + _ID);
        sb.append("\nName : " + name);
        sb.append("\nprofileUrl : " + profileUrl);
        sb.append("\npictureUrl : " + pictureUrl);

        Log.d(TAG, sb.toString());

        return sb.toString();
    }

}
