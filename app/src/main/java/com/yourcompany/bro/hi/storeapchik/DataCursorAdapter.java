package com.yourcompany.bro.hi.storeapchik;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;


public class DataCursorAdapter extends CursorAdapter {

    private ListActivity listActivity;

    public DataCursorAdapter(ListActivity context, Cursor c) {
        super(context, c, 0);
        this.listActivity = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.name);
        int nameColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME);
        String productName = cursor.getString(nameColumnIndex);
        nameTextView.setText(productName);

        TextView priceTextView = view.findViewById(R.id.price);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE);
        int productPrice = cursor.getInt(priceColumnIndex);
        priceTextView.setText(String.valueOf(productPrice));

        TextView quantityTextView = view.findViewById(R.id.quantity);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_QUANTITY);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        quantityTextView.setText(String.valueOf(productQuantity));

        ImageView productImageView = view.findViewById(R.id.product_image);
        String image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE));
        productImageView.setImageURI(Uri.parse(image));


        final Long id = cursor.getLong(cursor.getColumnIndex(DataContract.ProductEntry._ID));


        Button saleButton = view.findViewById(R.id.sale_button);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity.onListItemClick(id);
            }
        });
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listActivity.onSaleButtonClick(id, productQuantity);
                notifyDataSetChanged();
            }
        });

    }
}
