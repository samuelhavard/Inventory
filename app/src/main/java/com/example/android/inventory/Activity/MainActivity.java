package com.example.android.inventory.Activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

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
        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mInventoryCursorAdapter);

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }
}
