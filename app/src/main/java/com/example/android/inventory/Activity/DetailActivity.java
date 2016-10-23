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
import android.support.v4.app.NavUtils;
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
 *
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

    private String mSupplier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity_layout);

        mCurrentItemUri = getIntent().getData();

        mNameEditText = (EditText) findViewById(R.id.item_name);
        mDescriptionEditText = (EditText) findViewById(R.id.item_description);
        mSupplierEditText = (EditText) findViewById(R.id.item_supplier);
        mPriceEditText = (EditText) findViewById(R.id.item_price);
        mQuantityEditText = (EditText) findViewById(R.id.item_quantity);

        Button deleteButton = (Button) findViewById(R.id.button_delete);
        Button saveButton = (Button) findViewById(R.id.button_save);
        Button saleButton = (Button) findViewById(R.id.button_sale);
        Button shipmentButton = (Button) findViewById(R.id.button_shipment);
        Button orderButton = (Button) findViewById(R.id.button_order);

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.add_item));
            invalidateOptionsMenu();
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    insertItem();
                    finish();
                }
            });
        } else {
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateItem();
                    finish();
                }
            });
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityMinus();
            }
        });

        shipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityPlus();
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri orderUri;
                if(!mSupplier.startsWith("https://") && !mSupplier.startsWith("http://")){
                    String url = "http://" + mSupplier;
                    orderUri = Uri.parse(url);
                } else {
                    orderUri = Uri.parse(mSupplier);
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(orderUri);
                startActivity(intent);
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        saleButton.setOnTouchListener(mTouchListener);
        shipmentButton.setOnTouchListener(mTouchListener);
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
            mSupplier = cursor.getString(supplierColumnIndex);
            Integer itemPrice = cursor.getInt(priceColumnIndex);
            Integer itemQuantity = cursor.getInt(quantityColumnIndex);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mSupplierEditText.setText(mSupplier);
            mPriceEditText.setText(getString(R.string.number_message, itemPrice));
            mQuantityEditText.setText(getString(R.string.number_message, itemQuantity));
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
        builder.setMessage(R.string.delete_question);
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

    private void deleteItem() {
        int rowDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        if (rowDeleted > 0) {
            Toast toast = Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_LONG);
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
            Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.item_saved, Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, R.string.update_not_completed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.update_completed, Toast.LENGTH_LONG).show();
        }
    }

    private void quantityMinus() {
        if (mQuantityEditText.getText().toString().isEmpty()) {
            mQuantityEditText.setText(getString(R.string.number_message, 0));
        } else {
            String stringQuantity = mQuantityEditText.getText().toString().trim();
            int quantity = Integer.parseInt(stringQuantity);
            if (quantity > 0) {
                quantity--;
                mQuantityEditText.setText(getString(R.string.number_message, quantity));
            } else {
                Toast.makeText(this, R.string.quantity_below_zero, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void quantityPlus() {
        if (mQuantityEditText.getText().toString().isEmpty()) {
            mQuantityEditText.setText(getString(R.string.number_message, 0));
        } else {
            String stringQuantity = mQuantityEditText.getText().toString().trim();
            int quantity = Integer.parseInt(stringQuantity);
            quantity++;
            mQuantityEditText.setText(getString(R.string.number_message, quantity));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_and_quit);
        builder.setPositiveButton(R.string.discard_option, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing_option, new DialogInterface.OnClickListener() {
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

//    private Uri getUri(String supplier) {
//        Uri supplierUri = Uri.parse(supplier);
//    }
}
