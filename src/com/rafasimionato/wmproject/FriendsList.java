package com.rafasimionato.wmproject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.rafasimionato.wmproject.data.Contact;
import com.rafasimionato.wmproject.data.ContactAdapter;

/**
 * This class loads all contacts from the connected account into a list view
 * object { @see listView_contactsList } defined by activity_friends_list
 * layout.
 * 
 * Clicking each line in the list view will raise an intent to open the
 * contact's profile in the Google+.
 * 
 * This class also implements part of the Google+ sign-in work flow as it needs
 * to query the contact list of the connected user.
 * 
 * @author Rafael Simionato
 */
public class FriendsList extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    private static final String TAG = "FriendsList";

    // Google client object used to interact with Google+ APIs
    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).addScope(Plus.SCOPE_PLUS_PROFILE).build();
    }

    @Override
    protected void onStart() {

        super.onStart();

        Log.d(TAG, "onStart called");
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {

        super.onStop();

        Log.d(TAG, "onStop called");

        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "onStop called - calling disconnect");
            mGoogleApiClient.disconnect();
        }

    }

    /**
     * As soon as user is given connected, it starts a request to query a list
     * of visible people in the user's circles.
     * 
     * The result for this query will be available in the onResult callback
     * method and there, all contacts will be loaded into an adapter to be set
     * to the list view object { @see listView_contactsList }.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected called");
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed called");
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "onConnectionSuspended called");
    }

    /**
     * It a callback method that receives the result of querying all visible
     * people in the user's circles. It loads into an adapter all this data and
     * set the list view object { @see listView_contactsList }.
     */
    @Override
    public void onResult(LoadPeopleResult peopleData) {

        Log.d(TAG, "onResult called - setting adapter");

        Contact contact;
        ArrayList<Contact> arrayListContacts = new ArrayList<Contact>();

        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {

            PersonBuffer personBuffer = peopleData.getPersonBuffer();

            try {

                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {

                    contact = new Contact(personBuffer.get(i).hasId() ? personBuffer.get(i).getId()
                            : null, personBuffer.get(i).hasDisplayName() ? personBuffer.get(i)
                            .getDisplayName() : null, personBuffer.get(i).hasUrl() ? personBuffer
                            .get(i).getUrl() : null, personBuffer.get(i).hasImage() ? personBuffer
                            .get(i).getImage().getUrl() : null);

                    arrayListContacts.add(contact);

                }

            } finally {
                personBuffer.close();
            }
        } else {
            Log.e(TAG, "Error requesting visible circles : " + peopleData.getStatus());
        }

        // Setting the adapter already loaded with all contacts retrieved from
        // the connected user account
        final ListView mListViewContacts = (ListView) findViewById(R.id.listView_contactsList);
        mListViewContacts.setAdapter(new ContactAdapter(this, arrayListContacts));

        // Setting a listener to monitor each line in the list view. Clicking it
        // will raise an intent to open the contact's profile in the Google+
        mListViewContacts.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Contact contact = (Contact) mListViewContacts.getItemAtPosition(position);
                String url = contact.getProfileUrl();

                if (url != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else {
                    Toast.makeText(FriendsList.this, "No URL available for this contact.",
                            Toast.LENGTH_LONG).show();
                }

            }

        });

    }

}