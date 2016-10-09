package com.example.android.inventory.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;
import com.example.android.inventory.R;

/**
 * Created by samue_000 on 10/9/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.NAME));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.QUANTITY));

        nameTextView.setText(name);
        priceTextView.setText(price);
        quantityTextView.setText(quantity);
    }
}
