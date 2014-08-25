package com.rafasimionato.wmproject.data;

import java.lang.ref.WeakReference;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rafasimionato.wmproject.R;
import com.rafasimionato.wmproject.image.BitmapHandler;
import com.rafasimionato.wmproject.image.BitmapHandlerDownloadTask;

/**
 * This class implements a view holder for the elements defined by row layout.
 * 
 * This is used by the contact adapter class { @see ContactAdapter } to avoid
 * inflating a new layout and calling 3 times findViewById for each line in the
 * list view object { @see listView_contactsList } defined by
 * activity_friends_list layout.
 * 
 * For further information, please visit Android developers web site using the
 * following link.
 * 
 * @link 
 *       http://developer.android.com/training/improving-layouts/smooth-scrolling
 *       .html
 * 
 * @author Rafael Simionato
 */
public class ContactViewHolder {

    // These are the view holder references for the row layout elements. Their
    // data may change high frequently according the scrolling of the list view
    // object every time getView method of ContactAdapter is called
    private final ImageView picture;
    private final TextView name;
    private final TextView profileUrl;

    // It handles how to set bitmaps for the ImageView object referred by the
    // view holder
    private final BitmapHandler mBitmapHandler = new BitmapHandler();

    // It holds a weak reference to the last task thrown to download a bitmap
    // for the ImageView object referred by the view holder
    private WeakReference<BitmapHandlerDownloadTask> mBitmapHandlerDownloadTaskReference = new WeakReference<BitmapHandlerDownloadTask>(
            null);

    /**
     * It finds the references for each element in the entry row layout.
     * 
     * @param layoutRow
     *            row layout object that refers one line of the list view object
     *            { @see listView_contactsList } defined by
     *            activity_friends_list layout.
     */
    public ContactViewHolder(View layoutRow) {
        picture = (ImageView) layoutRow.findViewById(R.id.imageView_contactPicture);
        name = (TextView) layoutRow.findViewById(R.id.textView_contactName);
        profileUrl = (TextView) layoutRow.findViewById(R.id.textView_contactProfileUrl);
    }

    /**
     * It maps the data in the entry contact to the row layout elements referred
     * by the view holder. It also starts an asynchronous request to set the
     * ImageView object with a bitmap specified by the image URL in the entry
     * contact.
     * 
     * As soon as the bitmap is given available, the bitmap handler object sets
     * it to the ImageView object if the view holder still refers the row layout
     * elements in the list view @param position.
     * 
     * @param position
     *            refers the contact index in the list view object defined by
     *            activity_friends_list layout
     * @param contact
     *            entry data to be mapped to the row layout elements
     */
    public void setData(int position, Contact contact) {
        name.setText(contact.getName());
        profileUrl.setText(contact.getProfileUrl());
        mBitmapHandler.setBitmap(position, contact.getPictureUrl(), this);
    }

    /**
     * It returns the ImageView object referred by the view holder
     */
    public ImageView getPicture() {
        return picture;
    }

    /**
     * It returns a reference to the last task thrown to download a bitmap for
     * the ImageView object referred by the view holder.
     */
    public BitmapHandlerDownloadTask getBitmapDownloadTaskRef() {
        return mBitmapHandlerDownloadTaskReference.get();
    }

    /**
     * It sets a weak reference to the task thrown to download a bitmap for the
     * ImageView object referred by the view holder based in the @param contact
     * of the this.setData method.
     */
    public void setBitmapDownloadTaskRef(BitmapHandlerDownloadTask taskRef) {
        mBitmapHandlerDownloadTaskReference = new WeakReference<BitmapHandlerDownloadTask>(taskRef);
    }

}