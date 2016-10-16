package com.example.android.inventory.Activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    private Button mDelete;
    private Button mSave;
    private Button mSale;
    private Button mShipment;

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

        mDelete = (Button) findViewById(R.id.button_delete);
        mSave = (Button) findViewById(R.id.button_save);
        mSale = (Button) findViewById(R.id.button_sale);
        mShipment = (Button) findViewById(R.id.button_shipment);

        if (mCurrentItemUri == null) {
            setTitle("Add an Item");
            invalidateOptionsMenu();
            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    insertItem();
                    finish();
                }
            });
        } else {
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateItem();
                    finish();
                }
            });
        }

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        mSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityMinus();
            }
        });

        mShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityPlus();
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_details, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_save:
//                insertItem();
//                finish();
//                break;
//            case R.id.action_delete:
//                showDeleteConfirmationDialog();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        if (mCurrentItemUri == null) {
//            MenuItem menuItem = menu.findItem(R.id.action_delete);
//            menuItem.setVisible(false);
//        }
//        return true;
//    }

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

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    private void deleteItem() {
        int rowDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        if (rowDeleted > 0) {
            Toast toast = Toast.makeText(this, "Item Deleted", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
    }

    private void insertItem() {

        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, nameString);
        values.put(InventoryEntry.QUANTITY, quantity);
        values.put(InventoryEntry.PRICE, price);
        values.put(InventoryEntry.DESCRIPTION, descriptionString);
        values.put(InventoryEntry.SUPPLIER, supplierString);

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, "Error saving item", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Item Saved", Toast.LENGTH_LONG).show();
        }
    }

    private void updateItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, nameString);
        values.put(InventoryEntry.QUANTITY, quantity);
        values.put(InventoryEntry.PRICE, price);
        values.put(InventoryEntry.DESCRIPTION, descriptionString);
        values.put(InventoryEntry.SUPPLIER, supplierString);

        Long rowId = ContentUris.parseId(mCurrentItemUri);
        String[] selectionArgs = {rowId.toString()};

        int updatedRows = getContentResolver().update(
                mCurrentItemUri,
                values,
                InventoryEntry._ID,
                selectionArgs);

        if (updatedRows < 0) {
            Toast.makeText(this, "Update Not completed", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Update Completed", Toast.LENGTH_LONG).show();
        }
    }

    private void quantityMinus() {

        if (mQuantityEditText.getText().toString().isEmpty()) {
            mQuantityEditText.setText(0 + "");
        } else {
            String stringQuantity = mQuantityEditText.getText().toString().trim();
            int quantity = Integer.parseInt(stringQuantity);
            if (quantity > 0) {
                quantity--;
                mQuantityEditText.setText(quantity + "");
            } else {
                Toast.makeText(this, "Quantity cannot be below 0", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void quantityPlus() {

        if (mQuantityEditText.getText().toString().isEmpty()) {
            mQuantityEditText.setText(0 + "");
        } else {
            String stringQuantity = mQuantityEditText.getText().toString().trim();
            int quantity = Integer.parseInt(stringQuantity);
            quantity++;
            mQuantityEditText.setText(quantity + "");
        }
    }
}
