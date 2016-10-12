package com.example.android.inventory.Activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;
import com.example.android.inventory.R;

/**
 * Created by samue_000 on 10/10/2016.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    private Uri mCurrentItemUri;

    private static boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mSupplierEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity_layout);

        mCurrentItemUri = getIntent().getData();

        mNameEditText = (EditText) findViewById(R.id.item_name);
        mDescriptionEditText = (EditText) findViewById(R.id.item_description);
        mSupplierEditText = (EditText) findViewById(R.id.item_supplier);
        mPriceEditText = (EditText) findViewById(R.id.item_price);
        mQuantityEditText = (EditText) findViewById(R.id.item_quantity);

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME,
                InventoryEntry.DESCRIPTION,
                InventoryEntry.SUPPLIER,
                InventoryEntry.PRICE,
                InventoryEntry.QUANTITY};

        return new CursorLoader(
                this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.DESCRIPTION);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.QUANTITY);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            Integer itemPrice = cursor.getInt(priceColumnIndex);
            Integer itemQuantity = cursor.getInt(quantityColumnIndex);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(Integer.toString(itemPrice));
            mQuantityEditText.setText(Integer.toString(itemQuantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }
}
