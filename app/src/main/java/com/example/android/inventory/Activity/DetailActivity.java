package com.example.android.inventory.Activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;
import com.example.android.inventory.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@link DetailActivity} shows the details of a selected item that is or was in inventory.
 * This screen allows the user to input a new item that contains information such as name, supplier,
 * quantity of item in stock, a description, along with the ability to update that information or
 * delete the item entirely.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {



    //Declare all global variable to be used throughout the detail activity.

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

    private ImageView mItemImageView;

    private String mSupplier;

    private Bitmap mItemImageBitmap;

    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    /**
     * onCreate initializes all global variables to be used and sets on click listeners to the
     * buttons used in the DetailActivity class.  Additionally, it sets the on touch listener to
     * each field so the user can be prompted if they want to save the changes or abandon them.
     *
     */
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

        mItemImageView = (ImageView) findViewById(R.id.detail_image);

        Button deleteButton = (Button) findViewById(R.id.button_delete);
        Button saveButton = (Button) findViewById(R.id.button_save);
        Button saleButton = (Button) findViewById(R.id.button_sale);
        Button shipmentButton = (Button) findViewById(R.id.button_shipment);
        Button orderButton = (Button) findViewById(R.id.button_order);
        Button imageButton = (Button) findViewById(R.id.image_button);

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

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasExternalPermission = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (hasExternalPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, 1);
                }
            }
        });

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
                if (!mSupplier.startsWith("https://") && !mSupplier.startsWith("http://")) {
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

    /**
     * onRequestPermissionResults listens to the results of a display presented to the user requesting
     * permission to access images on their device.
     *
     * @param requestCode is the code for access requests
     * @param permissions are the permissions requested to be accessed
     * @param grantResults are the results of the requests for access
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImage();
                } else {
                    Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * getImage is a helper method created to retrieve images form the device.
     */
    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 1);
    }

    /**
     *
     * @return a {@link CursorLoader} of data to be presented to the user.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME,
                InventoryEntry.DESCRIPTION,
                InventoryEntry.SUPPLIER,
                InventoryEntry.PRICE,
                InventoryEntry.QUANTITY,
                InventoryEntry.IMAGE};

        return new CursorLoader(
                this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    /**
     *
     * @param loader is a {@link Loader <{@link Cursor}>} object
     * @param cursor is a {@link Cursor} object who's contents are to be displayed to the user.
     */
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
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            mSupplier = cursor.getString(supplierColumnIndex);
            Integer itemPrice = cursor.getInt(priceColumnIndex);
            Integer itemQuantity = cursor.getInt(quantityColumnIndex);

            byte[] imageArray = cursor.getBlob(imageColumnIndex);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mSupplierEditText.setText(mSupplier);
            mPriceEditText.setText(getString(R.string.number_message, itemPrice));
            mQuantityEditText.setText(getString(R.string.number_message, itemQuantity));

            if (imageArray != null) {
                Bitmap image = byteToImage(imageArray);
                mItemImageView.setImageBitmap(image);
            }
        }
    }

    /**
     * onLoaderReset resets the edit text values back to empty.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    /**
     * showDeleteConfirmationDialog is a helper method used to prompt the user on if they are
     * certain the item should be deleted.
     */
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

    /**
     * deleteItem is a helper methdod that deletes the item from the database if the user confirms
     * the item should be deleted in showDeleteConfirmationDialog
     */
    private void deleteItem() {
        int rowDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        if (rowDeleted > 0) {
            Toast toast = Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
    }

    /**
     * insertItem is a helper method used to parse information from the edit text fields and insert
     * that information into the database.
     */
    private void insertItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        byte[] imageByteArray = imageToByte(mItemImageBitmap);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, nameString);
        values.put(InventoryEntry.QUANTITY, quantity);
        values.put(InventoryEntry.PRICE, price);
        values.put(InventoryEntry.DESCRIPTION, descriptionString);
        values.put(InventoryEntry.SUPPLIER, supplierString);
        values.put(InventoryEntry.IMAGE, imageByteArray);

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.item_saved, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * imageToByte is a helper method used to convert a {@link Bitmap} to an array of bytes that is then
     * stored in a database.
     *
     * @param bitmap is the user selected image that is to be converted into a byte array that is then
     *               saved into the database
     * @return a byte[] to be stored in the database
     */
    private byte[] imageToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /**
     * byteToImage is a helper method that converts an array of bytes that was formerly stored in a
     * database into a {@link Bitmap}
     *
     * @param image is an array of bytes to be converted into a {@link Bitmap}
     * @return a {@link Bitmap} to be displayed
     */
    private static Bitmap byteToImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * onActivityResult is the data associated with selecting an image
     *
     * @param requestCode is the code used to perform the request
     * @param resultCode is the result code from the selection
     * @param data is the data associated with the users selection.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri mSelectedImage = data.getData();
            mItemImageBitmap = null;

            try {
                mItemImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mSelectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mItemImageBitmap != null) {
                mItemImageView.setImageBitmap(mItemImageBitmap);
            }
        }
    }

    /**
     * updateItem is a helper method used to update the item in the database
     */
    private void updateItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        byte[] imageByteArray = imageToByte(mItemImageBitmap);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, nameString);
        values.put(InventoryEntry.QUANTITY, quantity);
        values.put(InventoryEntry.PRICE, price);
        values.put(InventoryEntry.DESCRIPTION, descriptionString);
        values.put(InventoryEntry.SUPPLIER, supplierString);
        values.put(InventoryEntry.IMAGE, imageByteArray);

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

    /**
     * quantityMinus is a helper method used to reduce the current stock of a selected item.
     */
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

    /**
     * quantityPlus is a helper method used to increase the current stock of a selected item.
     */
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

    /**
     * onOptionsItemSelected selects menu options
     *
     * @param item is the user selected menu item
     */
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

    /**
     * onBackPressed presents the user with a dialog if the user changed any setting for the item.
     */
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

    /**
     * showUnsavedChangesDialog is a helper method used to display a dialog to the user
     *
     * @param discardButtonClickListener is the result of the user click on the discard dialog
     */
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
}
