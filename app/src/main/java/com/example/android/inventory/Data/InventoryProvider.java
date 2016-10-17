package com.example.android.inventory.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;

/**
 *
 */

public class InventoryProvider extends ContentProvider {

    InventoryDbHelper mDbHelper;

    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(
                        InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(
                        InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {

        if (values.containsKey(InventoryEntry.NAME)) {
            String name = values.getAsString(InventoryEntry.NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        if (values.containsKey(InventoryEntry.QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires a valid quantity");
            }
        }

        if (values.containsKey(InventoryEntry.PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires a valid price");
            }
        }

        if (values.containsKey(InventoryEntry.DESCRIPTION)) {
            String description = values.getAsString(InventoryEntry.DESCRIPTION);
            if (description == null) {
                throw new IllegalArgumentException("Item requires a valid description");
            }
        }

        if (values.containsKey(InventoryEntry.SUPPLIER)) {
            String supplier = values.getAsString(InventoryEntry.SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Item requires a valid supplier");
            }
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {
            return null;
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final  int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted !=0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int matcher = sUriMatcher.match(uri);
        switch (matcher) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory (Uri uri, ContentValues contentValues, String selection,
                                 String[] selectionArgs) {
        if (contentValues.containsKey(InventoryEntry.NAME)) {
            String name = contentValues.getAsString(InventoryEntry.NAME);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        if (contentValues.containsKey(InventoryEntry.QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(InventoryEntry.QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires a quantity");
            }
        }

        if (contentValues.containsKey(InventoryEntry.PRICE)) {
            Integer price = contentValues.getAsInteger(InventoryEntry.PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires a price");
            }
        }

        if (contentValues.containsKey(InventoryEntry.DESCRIPTION)) {
            String description = contentValues.getAsString(InventoryEntry.DESCRIPTION);
            if (description == null) {
                throw new IllegalArgumentException("Item requires a description");
            }
        }

        if (contentValues.containsKey(InventoryEntry.SUPPLIER)) {
            String supplier = contentValues.getAsString(InventoryEntry.SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Item needs a supplier.");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection,
                selectionArgs);

        if (rowsUpdated != 0) {
            if(getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsUpdated;
    }
}
