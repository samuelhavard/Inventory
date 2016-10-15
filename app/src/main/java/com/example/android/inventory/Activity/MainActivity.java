package com.example.android.inventory.Activity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
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


    private void insertDummyData() {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, "Thing");
        values.put(InventoryEntry.QUANTITY, 1);
        values.put(InventoryEntry.PRICE, 100);
        values.put(InventoryEntry.DESCRIPTION, "A thing that does stuff");
        values.put(InventoryEntry.SUPPLIER, "Dude on the corner");

        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_new_item:
                newItem();
                return true;
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void newItem () {
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }
}
