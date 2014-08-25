package com.rafasimionato.wmproject.data;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rafasimionato.wmproject.R;

/**
 * This is a simple base adapter implementation class used to load data into the
 * list view object { @see listView_contactsList } defined by
 * activity_friends_list layout.
 * 
 * @author Rafael Simionato
 */
public class ContactAdapter extends BaseAdapter {

    private static final String TAG = "ContactAdapter";

    // Array list of Contact that hold all the references read from each contact
    // after sign-in the Google+ user's account.
    private ArrayList<Contact> arrayListContacts;

    // Layout inflater reference used to inflate a layout row in the contacts
    // list view object. Please @see R.layout.row
    private final LayoutInflater layoutInflater;

    /**
     * Contact adapter constructor. It stores the initial set of data to be used
     * and gets a reference to a layout inflater that will be used later to
     * inflates each layout row of the contacts list view.
     * 
     * @param context
     *            reference for activity where lies the contacts list view
     * @param contacts
     *            initial data to be loaded in this adapter
     */
    public ContactAdapter(Context context, ArrayList<Contact> contacts) {
        layoutInflater = LayoutInflater.from(context);
        arrayListContacts = contacts;
    }

    /**
     * It returns the number of Contact objects in the contacts array list.
     */
    @Override
    public int getCount() {
        return arrayListContacts.size();
    }

    /**
     * It returns a Contact object reference from a specific position in the
     * contacts array list.
     * 
     * @param position
     *            index in the contacts array list for the required Contact
     *            object
     * 
     * @return Contact object reference
     */
    @Override
    public Object getItem(int position) {
        return arrayListContacts.get(position);
    }

    /**
     * It returns an ID for a specific position in the contacts array list.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * It's used to set a specific Contact to a row layout in the contacts list
     * view object.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView called for position : " + position);

        // Using the View Holder pattern to avoid inflate a new layout object
        // each time this method is called
        ContactViewHolder vHolder;

        if (convertView == null) {
            // It only inflates a new layout object when current row
            // (convertView) was not recycled
            convertView = layoutInflater.inflate(R.layout.row, parent, false);
            vHolder = new ContactViewHolder(convertView);
            // and stores a reference in the row object tag
            convertView.setTag(vHolder);
        } else {
            // Otherwise it only retrieves the stored reference from the row's
            // object's tag
            vHolder = (ContactViewHolder) convertView.getTag();
        }

        // Sets the contact data to the row's layout elements before returning
        // the row's reference
        vHolder.setData(position, (Contact) getItem(position));

        return convertView;
    }

}