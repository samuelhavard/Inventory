package com.example.android.inventory.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * {@link InventoryContract} is the contract used for accessing the database.
 */

public class InventoryContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";
    private InventoryContract() {
    }

    public static class InventoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String NAME = "name";
        public static final String QUANTITY = "quantity";
        public static final String PRICE = "price";
        public static final String DESCRIPTION = "description";
        public static final String SUPPLIER = "supplier";
        public static final String IMAGE = "image";

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE
                        + "/"
                        + CONTENT_AUTHORITY
                        + "/"
                        + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE
                        + "/"
                        + CONTENT_AUTHORITY
                        + "/"
                        + PATH_INVENTORY;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
    }
}
