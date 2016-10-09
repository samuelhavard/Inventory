package com.example.android.inventory.Data;

import android.provider.BaseColumns;

/**
 * Created by samue_000 on 10/9/2016.
 */

public class InventoryContract {
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
    }
}
