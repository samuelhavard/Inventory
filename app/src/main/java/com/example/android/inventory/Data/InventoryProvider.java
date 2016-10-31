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
 * Content providers are one of the primary building blocks of Android applications, providing
 * content to applications
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

    /**
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     *
     * @param uri The {@link Uri} to query. This will be the full URI sent by the client; if the client is
     *            requesting a specific record, the URI will end in a record number that the
     *            implementation should parse and add to a WHERE or HAVING clause, specifying
     *            that _id value.
     * @param projection The list of columns to put into the cursor. If null all columns are
     *                   included.
     * @param selection A selection criteria to apply when filtering rows. If null then all rows
     *                  are included.
     * @param selectionArgs May include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection.
     *                      The values will be bound as Strings.
     * @param sortOrder How the rows in the cursor should be sorted. If null then the provider is
     *                  free to define the sort order.
     * @return a {@link Cursor} or null.
     */
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
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

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

    /**
     *
     * @param uri he {@link Uri} to query.
     * @return a MIME type string, or null if there is no type.
     */
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

    /**
     *
     * @param uri The content:// {@link Uri} of the insertion request. This must not be null.
     * @param contentValues A set of column_name/value pairs to add to the database. This must not be null.
     * @return The URI for the newly inserted item.
     */
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

    /**
     *
     * @param uri The content:// URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database. This must not be null.
     * @return The URI for the newly inserted item.
     */
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

    /**
     *
     * @param uri The full {@link Uri} to query, including a row ID (if a specific record is requested).
     * @param selection An optional restriction to apply to rows when deleting.
     * @param selectionArgs The selection arguments
     * @return he number of rows affected.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsDeleted;
    }

    /**
     *
     * @param uri The {@link Uri} to query. This can potentially have a record ID if this is an
     *            update request for a specific record.
     * @param contentValues A set of column_name/value pairs to update in the database. This must not be null.
     * @param selection An optional filter to match rows to update.
     * @param selectionArgs Selection arguments
     * @return the number of rows affected.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int matcher = sUriMatcher.match(uri);
        switch (matcher) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * A helper method used to help update the database.
     *
     * @param uri The {@link Uri} to query. This can potentially have a record ID if this is an
     *            update request for a specific record.
     * @param contentValues A set of column_name/value pairs to update in the database. This must not be null.
     * @param selection An optional filter to match rows to update.
     * @param selectionArgs Selection arguments
     * @return the number of rows affected.
     */
    private int updateInventory(Uri uri, ContentValues contentValues, String selection,
                                String[] selectionArgs) {
//        if (contentValues.containsKey(InventoryEntry.NAME)) {
//            String name = contentValues.getAsString(InventoryEntry.NAME);
//            if (name.isEmpty()) {
//                throw new IllegalArgumentException("Item requires a name");
//            }
//        }

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
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsUpdated;
    }
}
