package com.yourcompany.bro.hi.storeapchik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_PRICE;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry.CONTENT_URI;
import static com.yourcompany.bro.hi.storeapchik.DataContract.ProductEntry._ID;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    private static final int MAX_PRODUCT_QUANTITY = 100;
    private static final int MIN_PRODUCT_QUANTITY = 0;
    String quantityString;
    Uri currentItemUri;
    Uri imageUri;
    private boolean productHasChanged = false;
    private EditText productName;
    private EditText productPrice;
    private EditText productQuantity;
    private ImageView productImage;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        currentItemUri = intent.getData();

        if (currentItemUri == null) {
            setTitle("Add product");
        } else {
            setTitle("Edit product");
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.edit_product_name);
        productPrice = findViewById(R.id.edit_price);
        productQuantity = findViewById(R.id.product_quantity);
        Button decreaseButton = findViewById(R.id.decrement);
        Button increaseButton = findViewById(R.id.increment);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button saveButton = findViewById(R.id.save_button_and_store);

        productName.setOnTouchListener(touchListener);
        productPrice.setOnTouchListener(touchListener);
        productQuantity.setOnTouchListener(touchListener);
        decreaseButton.setOnTouchListener(touchListener);
        increaseButton.setOnTouchListener(touchListener);
        uploadButton.setOnTouchListener(touchListener);
        saveButton.setOnTouchListener(touchListener);


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                productHasChanged = true;
            }
        });

      increaseButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(productQuantity.getText().toString().trim());
                    quantity = quantity + 1;
                    productQuantity.setText(Integer.toString(quantity));

            }
        });

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(productQuantity.getText().toString().trim());
                    quantity = quantity - 1;
                    productQuantity.setText(Integer.toString(quantity));

            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean productSaved = saveProduct();
                if (productSaved) finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if (currentItemUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private boolean saveProduct() {
        String name = productName.getText().toString().trim();
        String price = productPrice.getText().toString().trim();
        quantityString = productQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please, enter name", Toast.LENGTH_SHORT).show();
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);

        int quantity = !TextUtils.isEmpty(quantityString) ? Integer.parseInt(quantityString) : 0;
        values.put(COLUMN_PRODUCT_QUANTITY, quantity);

        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this,"Please, enter price", Toast.LENGTH_SHORT).show();
            return false;
        }

        values.put(COLUMN_PRODUCT_PRICE, price);

        if (imageUri == null) {
            Toast.makeText(this,"Please, add photo", Toast.LENGTH_SHORT).show();
            return false;
        }

        String image = imageUri.toString();
        values.put(COLUMN_PRODUCT_IMAGE, image);

        if (currentItemUri == null) {
            setTitle("Add Product");
            supportInvalidateOptionsMenu();
            Uri newUri = getContentResolver().insert(CONTENT_URI, values);


            if (newUri == null) {

                Toast.makeText(this, "Fatal error",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {

                Toast.makeText(this, "Product add",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {

            int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
            if (rowsAffected == 0) {

                Toast.makeText(this, "Fatal error",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {

                Toast.makeText(this,"Product save",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }

    private void showToastMessage(int messageResourceId) {
        Toast.makeText(EditActivity.this, getString(messageResourceId),
                Toast.LENGTH_SHORT).show();
    }

    private void showToastIf(boolean condition) {
        int messageResourceId = condition ? R.string.editor_delete_item_failed : R.string.editor_delete_item_successful;
        showToastMessage(messageResourceId);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want delete?");
        builder.setPositiveButton("Yes", discardButtonClickListener);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want delete?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                deleteProduct();
            }
        });
        builder.setNegativeButton("No", null);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (null != currentItemUri) {
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);
            showToastIf(0 == rowsDeleted
            );
        }

        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                _ID,
                COLUMN_PRODUCT_NAME,
                COLUMN_PRODUCT_PRICE,
                COLUMN_PRODUCT_QUANTITY,
                COLUMN_PRODUCT_IMAGE};
        return new CursorLoader(this, currentItemUri,
                projection, null, null, null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (null == cursor || 1 > cursor.getCount()) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String picture = cursor.getString(pictureColumnIndex);

            productName.setText(name);
            productPrice.setText(price);
            productQuantity.setText(Integer.toString(quantity));
            productImage.setImageURI(Uri.parse(picture));
            imageUri = Uri.parse(picture);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productName.setText("");
        productPrice.setText("");
        productQuantity.setText("");

    }


    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                productImage.setImageURI(imageUri);
                productImage.invalidate();
            }
        }
    }
}