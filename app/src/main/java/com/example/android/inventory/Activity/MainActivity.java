package com.example.android.inventory.Activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.Adapter.InventoryCursorAdapter;
import com.example.android.inventory.Data.InventoryContract.InventoryEntry;
import com.example.android.inventory.R;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    InventoryCursorAdapter mInventoryCursorAdapter;
    private static final int URL_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView inventoryListView = (ListView) findViewById(R.id.activity_main);
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mInventoryCursorAdapter);

        getLoaderManager().initLoader(URL_LOADER, null, this);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                Uri selectedUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(selectedUri);
                startActivity(intent);
            }
        });
    }

    /**
     * onCreateLoader returns a {@link CursorLoader} for displaying a list of items to the user
     * @return a {@link CursorLoader} for use in a list
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME,
                InventoryEntry.PRICE,
                InventoryEntry.QUANTITY
        };

        return new CursorLoader(
                this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryCursorAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }

    /**
     * helper mehtod used to insert dummy data
     */
    private void insertDummyData() {

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, "Thing");
        values.put(InventoryEntry.QUANTITY, 1);
        values.put(InventoryEntry.PRICE, 100);
        values.put(InventoryEntry.DESCRIPTION, "A thing that does stuff!");
        values.put(InventoryEntry.SUPPLIER, "www.amazon.com");

        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    /**
     *
     * @param menu {@link Menu} to inflate into. The items and submenus will be added to this Menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_new_item:
                newItem();
                return true;
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.action_delete_all_items:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void newItem() {
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    /**
     * showDeleteConfirmationDialog is a helper method that displays a dialog to the user to confirm
     * the user wants to delete all items.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_items);
        builder.setPositiveButton(R.string.delete_option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel_option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * deleteItem deletes all rows of items from the database.
     */
    private void deleteItem() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.all_items_deleted, Toast.LENGTH_LONG).show();
        }
    }
}
