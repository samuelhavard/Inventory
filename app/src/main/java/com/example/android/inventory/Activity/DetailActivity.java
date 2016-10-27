package com.example.android.inventory.Activity;

import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;
import com.example.android.inventory.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import static android.R.attr.bitmap;
import static android.R.attr.readPermission;

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

    private ImageView mItemImageView;

    private String mSupplier;

    private Uri mSelectedImage;

    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;


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
                //quantityMinus();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasExternalPermission = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (hasExternalPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, 1);
                }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImage();
                } else {
                    Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT ).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 1);
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
        values.put(InventoryEntry.IMAGE, mSelectedImage.toString());

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.item_saved, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mItemImageView = (ImageView) findViewById(R.id.detail_image);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                mItemImageView.setImageBitmap(bitmap);
            }
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
}
