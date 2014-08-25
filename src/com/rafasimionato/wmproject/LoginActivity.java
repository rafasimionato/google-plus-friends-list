package com.rafasimionato.wmproject;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.rafasimionato.wmproject.image.BitmapHandler;

/**
 * This class implements the Google+ sign-in work flow using the Google+
 * platform SDK for Android. It provides an interface to access a Google account
 * and its Google Play services. Once we get an account authenticated in the
 * device, new instances of GoogleApiClient will succeed accessing the same
 * state without asking user's consent.
 * 
 * GoogleApiClient was designed to be a lightweight way access to the central
 * state managed by Google Play services. As recommended by Google a connection
 * client object shall be instantiated the activity's onCreate method and then
 * call connect() in onStart() and disconnect() in onStop(), regardless of the
 * state.
 * 
 * This class also sets the login and profile elements defined by activity_login
 * layout. After getting connected user may click hardware back key and return
 * to this activity to check some profile information stored during login.
 * 
 * @link https://developer.android.com/google/play-services/index.html
 * @link https://developers.google.com/+/mobile/android/getting-started
 * @link https://developers.google.com/+/mobile/android/sign-in
 * @link 
 *       https://developer.android.com/reference/com/google/android/gms/common/api
 *       /GoogleApiClient.html
 * 
 * @author Rafael Simionato
 */
public class LoginActivity extends Activity implements View.OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";

    /**
     * Google client object used to interact with Google+ APIs
     */
    private GoogleApiClient mGoogleApiClient = null;

    /**
     * This is the request code used to identify sign-in user's interactions
     * upon onActivityResult
     */
    private static final int REQCODE_SIGN_IN = 0x484C5045;

    /**
     * This attribute works as a flag that indicates a pending intent is in
     * progress and prevents onConnectionFailed method from starting further
     * intents
     */
    private boolean mIntentInProgress;

    /**
     * These attributes track whether the sign-in button has been clicked just
     * before starting current connection process. This is required, for
     * instance, when user click hardware back key to check his/her profile's
     * information retrieved from login process
     */
    private boolean mSignInClicked = false;
    private boolean mShowFriendsList = true;

    /**
     * It stores the connection result retrieved in the onConnectionFailed
     * callback. Later in the connection flow it's used by resolveSignInErrors
     * method to start any needed intent. For instance, << Choose account >>
     * pop-up or << Google+ consent >> pop-up
     */
    private ConnectionResult mConnectionResult = null;

    /**
     * These attributes store data from current connected user
     */
    private String personName = null;
    private Person.Image personImage = null;
    private String personProfileUrl = null;

    /**
     * These attributes hold references to activity_login layout elements
     */
    private SignInButton profileSingInButton;
    private Button profileSingOutButton;
    private Button profileRevokeAccessButton;
    private TextView profileUserTextView;
    private ImageView profileImageView;
    private Button profileShareStatus;
    private Button profileFriendsList;

    /**
     * It's used to set a bitmap efficiently. For further information @see
     * BitmapHandler
     */
    private final BitmapHandler mBitmapHandler = new BitmapHandler();;

    private void resetConnectionObject() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN).addScope(Plus.SCOPE_PLUS_PROFILE).build();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate called");

        setContentView(R.layout.activity_login);
        resetConnectionObject();

        profileSingInButton = (SignInButton) findViewById(R.id.sign_in_button);
        profileSingInButton.setOnClickListener(this);

        profileSingOutButton = (Button) findViewById(R.id.sign_out_button);
        profileSingOutButton.setOnClickListener(this);

        profileRevokeAccessButton = (Button) findViewById(R.id.revoke_access_button);
        profileRevokeAccessButton.setOnClickListener(this);

        profileUserTextView = (TextView) findViewById(R.id.profile_userTextView);

        profileImageView = (ImageView) findViewById(R.id.profile_imageView);
        profileImageView.setOnClickListener(this);

        profileShareStatus = (Button) findViewById(R.id.share_button);
        profileShareStatus.setOnClickListener(this);

        profileFriendsList = (Button) findViewById(R.id.friendsList_button);
        profileFriendsList.setOnClickListener(this);

        setProfileElementsState(true, false, false, getApplicationContext().getResources()
                .getString(R.string.no_user_text), View.GONE, View.GONE);

    }

    @Override
    protected void onStart() {

        super.onStart();

        Log.d(TAG, "onStart called");

        mIntentInProgress = false;
        mGoogleApiClient.connect();

    }

    @Override
    protected void onRestart() {

        super.onRestart();

        Log.d(TAG, "onRestart called");

        // This is required to avoid automatically getting back to Friends list
        // class when clicking hardware back key to check profile's data
        // retrieved from login process
        mSignInClicked = false;
        mShowFriendsList = false;

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

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.sign_in_button:

                Log.d(TAG, "sign_in_button clicked");

                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    mShowFriendsList = true;
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }

                break;

            case R.id.sign_out_button:

                Log.d(TAG, "sign_out_button clicked");

                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }

                setProfileElementsState(true, false, false, getApplicationContext().getResources()
                        .getString(R.string.no_user_text), View.GONE, View.GONE);

                break;

            case R.id.revoke_access_button:

                Log.d(TAG, "revoke_access_button clicked");

                if (mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "sign_out_button - calling revokeAccessAndDisconnect");
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                }
                            });

                    // Force disconnecting Google client connection object
                    if (mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.disconnect();
                    }

                    setProfileElementsState(true, false, false, getApplicationContext()
                            .getResources().getString(R.string.no_user_text), View.GONE, View.GONE);

                }

                break;

            case R.id.profile_imageView:

                Log.d(TAG, "profile_imageView clicked");

                if (personProfileUrl != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(personProfileUrl));
                    startActivity(i);
                }

                break;

            case R.id.share_button:

                Log.d(TAG, "share_button clicked");

                Intent shareIntent = new PlusShare.Builder(this).setType("text/plain").getIntent();
                startActivityForResult(shareIntent, 0);

                break;

            case R.id.friendsList_button:

                Log.d(TAG, "friendsList_button clicked");

                startActivity(new Intent(this, FriendsList.class));

                break;

        }
    }

    /**
     * Consolidated method to set all elements from activity_login layout. They
     * assumes different states based on current user's state.
     * 
     * @param singInBState
     *            new sing-in button state
     * @param singOutBState
     *            new sing-out button state
     * @param revokeAccessBState
     *            new revoke access button state
     * @param userTextViewText
     *            new user name or default text
     * @param shareStatusBState
     *            new share status button state
     * 
     * @see profileImageView profile image view is always set here as default
     *      and when needed, it's set outside this method after calling it
     */
    private void setProfileElementsState(boolean singInBState, boolean singOutBState,
            boolean revokeAccessBState, String userTextViewText, int shareStatusBState,
            int friendListBState) {

        profileSingInButton.setEnabled(singInBState);
        profileSingOutButton.setEnabled(singOutBState);
        profileRevokeAccessButton.setEnabled(revokeAccessBState);
        profileImageView.setImageDrawable(getApplicationContext().getResources().getDrawable(
                R.drawable.list_image_0));
        profileUserTextView.setText(userTextViewText);
        profileShareStatus.setVisibility(shareStatusBState);
        profileFriendsList.setVisibility(friendListBState);

        // Resetting logged user data whenever sign-in button is set enabled
        if (singInBState) {
            personName = null;
            personImage = null;
            personProfileUrl = null;
        }

    }

    /**
     * This is a helper method to resolve possible sign-in errors stored in the
     * Connection Result object retrieved by onConnectionFailed callback
     */
    private void resolveSignInErrors() {
        Log.d(TAG, "resolveSignInError called");
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        REQCODE_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was cancelled before it was sent. Return to the
                // default state and attempt to connect to get an updated
                // ConnectionResult
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed called");
        if (!mIntentInProgress) {
            mConnectionResult = result;
            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all errors until the user is signed in, or they
                // cancel.
                resolveSignInErrors();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        Log.d(TAG, "onConnected called");

        // Get current user info and store in the class attributes
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            personImage = currentPerson.getImage();
            personProfileUrl = currentPerson.getUrl();
        }

        setProfileElementsState(false, true, true, personName, View.VISIBLE, View.VISIBLE);

        int lastIndexForRedimensioning = personImage.getUrl().lastIndexOf("?sz=");
        mBitmapHandler.setBitmap(lastIndexForRedimensioning < 0 ? personImage.getUrl()
                : personImage.getUrl().substring(0, lastIndexForRedimensioning), profileImageView);

        if (mShowFriendsList) {
            // Every time application is launched and an user is already
            // signed-in, shows a Toast message to the user and goes
            // straightforward to Friends list activity
            if (!mSignInClicked) {
                Toast.makeText(this, "Already connected as " + personName, Toast.LENGTH_LONG)
                        .show();
            }
            startActivity(new Intent(this, FriendsList.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.d(TAG, "onActivityResult called");
        if (requestCode == REQCODE_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "onConnectionSuspended called");
        mGoogleApiClient.connect();
    }

}