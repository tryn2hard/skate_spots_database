package com.example.android.seoulskatespotsv30;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.seoulskatespotsv30.data.SpotContract;

import java.io.ByteArrayOutputStream;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    // Global Cursor Adapter variable
    public static SpotCursorAdapter mAdapter;

    // Loader ID
    private static final int URI_LOADER = 0;

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    private String[] projection = {
            SpotContract.SpotEntry._ID,
            SpotContract.SpotEntry.COLUMN_SPOT_NAME,
            SpotContract.SpotEntry.COLUMN_SPOT_TYPE,
            SpotContract.SpotEntry.COLUMN_SPOT_IMAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the adapter
        mAdapter = new SpotCursorAdapter(this, null);

        // Find the listview
        ListView spotListView = (ListView) findViewById(R.id.list_view_spot);

        // Find the view to display when the database is empty
        View emptyView = (View) findViewById(R.id.empty_view);
        // Tell the listview to display the emptyview when the database is empty
        spotListView.setEmptyView(emptyView);

        // Set the adapter to the listview
        spotListView.setAdapter(mAdapter);

        // Create an onClickListener
        spotListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), EditorActivity.class);
                Uri currentSpotUri = ContentUris.withAppendedId(SpotContract.SpotEntry.CONTENT_URI, id);
                intent.setData(currentSpotUri);
                startActivity(intent);
            }
        });

        // Initialize the CursorLoader. The URI_LOADER value is eventually passed
        getLoaderManager().initLoader(URI_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertSpot() {
        // Create a ContentValues object where column names are the keys,
        // and the Spot attributes are the values.
        ContentValues values = new ContentValues();
        values.put(SpotContract.SpotEntry.COLUMN_SPOT_NAME, "Ttuk");
        values.put(SpotContract.SpotEntry.COLUMN_SPOT_TYPE, SpotContract.SpotEntry.TYPE_PARK);
        values.put(SpotContract.SpotEntry.COLUMN_SPOT_BUST, "No");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.park_ttukseom);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photo = baos.toByteArray();
        values.put(SpotContract.SpotEntry.COLUMN_SPOT_IMAGE, photo);

        // Insert a new row for Ttuk into the provider using the ContentResolver.
        // Use the {@link SPOTEntry#CONTENT_URI} to indicate that we want to insert
        // into the spots database table.
        // Receive the new content URI that will allow us to access Ttuk's data in the future.
        Uri newUri = getContentResolver().insert(SpotContract.SpotEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertSpot();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
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

                // Show a dialog that notifies the user they have chosen to delete a pet
                showDeleteConfirmationDialog(deleteButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case URI_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,
                        SpotContract.SpotEntry.CONTENT_URI,     // The content URI of the words table
                        projection,                             // The columns to return for each row
                        null,                                   // Selection criteria
                        null,                                   // Selection criteria
                        null
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor mCursor) {
        if (mCursor != null) {
            mAdapter.swapCursor(mCursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clear out the adapters reference to the cursor
        mAdapter.swapCursor(null);
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
                // and continue editing the pet.
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
     * Perform the deletion of the pet in the database.
     */
    private void deleteSpot() {

        int spotDeleted = getContentResolver().delete(SpotContract.SpotEntry.CONTENT_URI, null, null);


        if (spotDeleted == 0) {

            Toast.makeText(this, getString(R.string.delete_all_spots_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.deleted_all_spots),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
