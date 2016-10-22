package com.example.android.inventory.Adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.Data.InventoryContract.InventoryEntry;
import com.example.android.inventory.R;

/**
 * Created by samue_000 on 10/9/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(final Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.NAME));
        Integer price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.PRICE));
        Integer quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.QUANTITY));
        Integer rowId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));

        nameTextView.setText(name);
        priceTextView.setText(context.getString(R.string.price_message, price));
        quantityTextView.setText(context.getString(R.string.number_message, quantity));
        final int updatedQuantity = quantity - 1;

        final ContentValues values = new ContentValues();
        values.put(InventoryEntry.QUANTITY, updatedQuantity);

        final Uri currentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);
        final String[] selectionArgs = {rowId.toString()};

        ImageView moneyImageView = (ImageView) view.findViewById(R.id.money_image_view);
        moneyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updatedQuantity >= 0) {
                    int updatedRows = context.getContentResolver().update(
                            currentUri,
                            values,
                            InventoryEntry._ID,
                            selectionArgs
                    );
                    if (updatedRows > 0) {
                        Toast.makeText(context, "Sale Completed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Cannot sell what isn't there", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
