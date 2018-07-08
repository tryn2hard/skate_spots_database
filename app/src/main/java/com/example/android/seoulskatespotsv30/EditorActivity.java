package com.example.android.seoulskatespotsv30;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.seoulskatespotsv30.data.SpotContract.SpotEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Allows user to create a new spot or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the spot's name
     */
    private EditText mSpotNameEditText;

    /**
     * EditText field to enter the spot's bust factor
     */
    private EditText mSpotBustEditText;

    /**
     * ImageView to display the spot's image
     */
    private ImageView mSpotImageView;

    /**
     * EditText field to enter the spot's type
     */
    private Spinner mSpotTypeSpinner;

    // Specific URI_LOADER ID for this activity
    private static final int URI_LOADER = 1;

    // Spot ID values to compare with the cursor update return value
    private static final int SPOT_ID_STARTING_VALUE = -2;

    private Bitmap photo = null;

    private static final int CAMERA_PIC_REQUEST = 0;

    /**
     * Type of spot. The possible values are:
     * 0 for misc, 1 for ledge, 2 for bank, 3 for rail, 4 for park.
     */
    private int mSpotType = SpotEntry.TYPE_MISC;

    private Uri SpotUri;

    private boolean mSpotHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSpotHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent, if there is one
        // Form the intent grab the uri
        // Check the uri to see if it's null
        // Change the title based on the uri
        Intent intent = getIntent();
        SpotUri = intent.getData();

        if (SpotUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_spot));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.edit_spot));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor

            getLoaderManager().initLoader(URI_LOADER, null, this);
        }

        // Logic to add take an image and store it
        findViewById(R.id.spotImageEditor).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });



        // Find all relevant views that we will need to read user input from
        mSpotNameEditText = (EditText) findViewById(R.id.edit_spot_name);
        mSpotBustEditText = (EditText) findViewById(R.id.edit_spot_bust);
        mSpotTypeSpinner = (Spinner) findViewById(R.id.spinner_spotType);
        mSpotImageView = (ImageView) findViewById(R.id.spotImageEditor);

        // Set-up all the edit fields with a listener
        mSpotNameEditText.setOnTouchListener(mTouchListener);
        mSpotBustEditText.setOnTouchListener(mTouchListener);
        mSpotTypeSpinner.setOnTouchListener(mTouchListener);
        mSpotImageView.setOnTouchListener(mTouchListener);


        setupSpinner();

    }

    /**
     * This is called upon as soon as the editor activity is started.
     * What I think this code does is as follows:
     * 1. does a check for matching codes to make sure we dont' get null data
     * 2. takes the camera data and stores it as a bitmap
     * 3. Lastly the bitmap photo is set to the imageView
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            mSpotImageView.setImageBitmap(photo);
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSpotTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSpotTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_ledge))) {
                        mSpotType = SpotEntry.TYPE_LEDGE; // Ledge
                    } else if (selection.equals(getString(R.string.type_rail))) {
                        mSpotType = SpotEntry.TYPE_RAIL; // Rail
                    } else if (selection.equals(getString(R.string.type_bank))) {
                        mSpotType = SpotEntry.TYPE_BANK; // Bank
                    } else if (selection.equals(getString(R.string.type_park))) {
                        mSpotType = SpotEntry.TYPE_PARK;
                    } else {
                        mSpotType = SpotEntry.TYPE_MISC;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSpotType = SpotEntry.TYPE_MISC; // MISC
            }
        });
    }

    private void saveSpot() {

        // Getting the all that info some guy or gal puts in
        String nameString = mSpotNameEditText.getText().toString().trim();
        String bustString = mSpotBustEditText.getText().toString().trim();

        // mSpotType carries the associated integers for types of spots so no real change needed
        int typeInt = mSpotType;

        // This if/else makes sure that both the name and bust have been entered
        // without which the activity will just finish
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(bustString)
                && mSpotType == SpotEntry.TYPE_MISC) {
            return;
        } else {
            int spotId = SPOT_ID_STARTING_VALUE;

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SpotEntry.COLUMN_SPOT_NAME, nameString);
            values.put(SpotEntry.COLUMN_SPOT_BUST, bustString);
            values.put(SpotEntry.COLUMN_SPOT_TYPE, typeInt);
            values.put(SpotEntry.COLUMN_SPOT_IMAGE, bitToBlob(photo) );

            // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
            if (SpotUri == null) {
                // This is a NEW spot, so insert a new spot into the provider,
                // returning the content URI for the new spot.
                Uri newUri = getContentResolver().insert(SpotEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.new_spot_error),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.new_spot_saved),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING spot, so update the pet with content URI: mCurrentSpotUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentSpotUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(SpotUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.edit_spot_error),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.edit_spot),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (SpotUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save spot to the database
                saveSpot();
                // Exit Activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:

                // Create a click listener to handle the user confirming that
                // spot should be deleted.
                DialogInterface.OnClickListener deleteButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "delete" button, remove spot.
                                deleteSpot();

                            }
                        };

                // Show a dialog that notifies the user they have chosen to delete a spot
                showDeleteConfirmationDialog(deleteButtonClickListener);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the spot hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mSpotHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mSpotHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case URI_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,
                        SpotUri,                 // The content URI of the words table
                        null,                   // The columns to return for each row
                        null,                   // Selection criteria
                        null,                   // Selection criteria
                        null
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        cursor.moveToFirst();
        String nameString = cursor.getString(cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_NAME));
        String bustString = cursor.getString(cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_BUST));
        int typeInt = cursor.getInt(cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_TYPE));
        byte[] spotImage = cursor.getBlob(cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_IMAGE));


        mSpotNameEditText.setText(nameString);
        mSpotBustEditText.setText(bustString);
        mSpotTypeSpinner.setSelection(typeInt);

        ByteArrayInputStream imageStream = new ByteArrayInputStream(spotImage);
        Bitmap theImage= BitmapFactory.decodeStream(imageStream);
        mSpotImageView.setImageBitmap(theImage);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSpotNameEditText.setText("");
        mSpotBustEditText.setText("");
        mSpotTypeSpinner.setSelection(SpotEntry.TYPE_MISC);
        mSpotImageView.setImageBitmap(null);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the spot.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(
            DialogInterface.OnClickListener deleteButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteSpot();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the spot.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the spot in the database.
     */
    private void deleteSpot() {

        int spotDeleted = 0;
        if (SpotUri != null) {

            spotDeleted = getContentResolver().delete(SpotUri, null, null);

        }

        if (spotDeleted == 0) {

            Toast.makeText(this, getString(R.string.delete_spot_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.deleted_spot),
                    Toast.LENGTH_SHORT).show();
        }
        // This bit of code lets us exit the EditorActivity back to the Catalog
        NavUtils.navigateUpFromSameTask(EditorActivity.this);
    }

    /**
     * A helper method to convert bitmaps into BLOB for proper storage into the database
     * @param photo
     * @return
     */
    public byte[] bitToBlob(Bitmap photo){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] photoBlob;
        // If there is a photo do the conversion and return the BLOB
        // else return the generic input photo
        if(photo != null){

            Bitmap bitmap = photo;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            photoBlob = baos.toByteArray();
            return photoBlob;

        }
        else{
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.add_image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            photoBlob = baos.toByteArray();
            return photoBlob;

        }

    }
}

