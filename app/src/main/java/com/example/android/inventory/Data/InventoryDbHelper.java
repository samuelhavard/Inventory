package com.example.android.inventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryDbHelper} extends {@link SQLiteOpenHelper}
 * Create a helper object to create, open, and/or manage a database.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.NAME + " TEXT NOT NULL, "
                + InventoryEntry.PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.DESCRIPTION + " TEXT NOT NULL, "
                + InventoryEntry.SUPPLIER + " TEXT NOT NULL, "
                + InventoryEntry.IMAGE + " BLOB);";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
