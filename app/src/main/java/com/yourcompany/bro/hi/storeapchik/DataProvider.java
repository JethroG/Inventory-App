package com.yourcompany.bro.hi.storeapchik;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.yourcompany.bro.hi.storeapchik.DataContract.CONTENT_AUTHORITY;
import static com.yourcompany.bro.hi.storeapchik.DataContract.PATH_PRODUCTS;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.TABLE_NAME;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry._ID;

@SuppressLint("Registered")
public class DataProvider extends ContentProvider {


    private static final int ITEMS = 100;
    private static final int ITEM_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, ITEMS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#", ITEM_ID);
    }

    private DataDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DataDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return DataContract.ProductEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return DataContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {

        String name = values.getAsString(COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a product name");
        }

        Integer quantity = values.getAsInteger(COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        Integer price = values.getAsInteger(COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(TABLE_NAME, null, values);

        if (id == -1) {
            Log.e("ProductProvider", "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateProduct(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a product name");
            }
        }
        Integer quantity = values.getAsInteger(COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        if (values.containsKey(COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }
        if (values.containsKey(COLUMN_PRODUCT_IMAGE)) {
            String productImage = values.getAsString(COLUMN_PRODUCT_IMAGE);
            if (productImage == null) {
                throw new IllegalArgumentException("Product requires an image");
            }
        }
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        int rowsUpdated = database.update(TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}


