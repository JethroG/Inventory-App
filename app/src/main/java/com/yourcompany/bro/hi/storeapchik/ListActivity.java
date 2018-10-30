package com.yourcompany.bro.hi.storeapchik;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.CONTENT_URI;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry._ID;


public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    private DataCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = findViewById(R.id.list);

        adapter = new DataCursorAdapter(this, null);
        productListView.setAdapter(adapter);

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pAdapterView, View pView, int position, long id) {
                Intent intent = new Intent(ListActivity.this, EditActivity.class);
                Uri currentItemUri = ContentUris.withAppendedId(CONTENT_URI, id);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete_all_items) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete all item's?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton("Noooo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(CONTENT_URI, null, null);

    }



    public void onListItemClick(long id) {
        Intent intent = new Intent(ListActivity.this, EditActivity.class);

        Uri currentItemUri = ContentUris.withAppendedId(CONTENT_URI, id);
        intent.setData(currentItemUri);

        startActivity(intent);
    }

    public void onSaleButtonClick(long id, int quantity) {
        Uri currentItemUri = ContentUris.withAppendedId(CONTENT_URI, id);

        int newQuantity = quantity > 0 ? quantity - 1 : 0;

        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_QUANTITY, newQuantity);

        int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
        if (quantity == newQuantity) {

            Toast.makeText(this, "Zero my friend", Toast.LENGTH_SHORT).show();
        } else if (quantity != newQuantity) {

            Toast.makeText(this, "Update data",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                _ID,
                COLUMN_PRODUCT_NAME,
                COLUMN_PRODUCT_PRICE,
                COLUMN_PRODUCT_QUANTITY,
                COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this, CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
    }
}