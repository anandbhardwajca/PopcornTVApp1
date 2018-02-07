package com.androidsalad.popcorntvapp.Activity.NewCeleb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidsalad.popcorntvapp.Activity.Welcome.WelcomeActivity;
import com.androidsalad.popcorntvapp.Model.Celeb;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.androidsalad.popcorntvapp.Util.Constants.INT_ACTION_PICK;

public class NewCelebActivity extends AppCompatActivity {

    //database storage
    private StorageReference mCelebImageStorage;
    private DatabaseReference mCelebDatabase;

    //variables
    private Button mSaveButton;
    private EditText mCelebNameEditText;
    private ImageButton mCelebImageButton;

    // bitmap
    private Bitmap mFullSizeBitmap, mThumbnailBitmap;

    //progress dialog
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_celeb);

        //initialize variables
        initializeVariables();

        //select image from gallery
        mCelebImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        //after selecting image save to Firebase
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                saveToFirebase();
            }
        });
    }


    private void initializeVariables() {

        //views
        mSaveButton = (Button) findViewById(R.id.addCelebSaveButton);
        mCelebNameEditText = (EditText) findViewById(R.id.addCelebNameEditText);
        mCelebImageButton = (ImageButton) findViewById(R.id.addCelebImageButton);

        //firebase ref
        mCelebImageStorage = FirebaseStorage.getInstance().getReference().child(Constants.FIREBASE_CELEB_DATABASE);
        mCelebDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CELEB_DATABASE);

        //progress dialog
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
    }

    private void selectImageFromGallery() {

        //start intent for selecting image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INT_ACTION_PICK);

    }

    //on activity result: after selecting image from gallery;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INT_ACTION_PICK && resultCode == RESULT_OK && data != null) {


            //get image uri from on Activity Result:
            Uri imageUri = data.getData();

            //create full size and thumbnail bitmaps
            try {
                mFullSizeBitmap = decodeSampledBitmapFromUri(imageUri, 1280, 1280);
                mThumbnailBitmap = decodeSampledBitmapFromUri(imageUri, 128, 128);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //display image
            Glide.with(this).load(imageUri).into(mCelebImageButton);
        }
    }


    //methods for getting bitmaps from uri:
    private Bitmap decodeSampledBitmapFromUri(Uri fileUri, int reqWidth, int reqHeight) throws IOException {
        InputStream stream = new BufferedInputStream(
                getApplicationContext().getContentResolver().openInputStream(fileUri));
        stream.mark(stream.available());
        BitmapFactory.Options options = new BitmapFactory.Options();
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        stream.reset();

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(stream, null, options);
    }

    //method for calculating in sample size for getting bitmaps from uri:
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private void saveToFirebase() {

        //disable save button to ensure no repeat
        mSaveButton.setEnabled(false);

        //get database unique key for new celeb
        final String celebId = mCelebDatabase.push().getKey();

        //compress bitmap for full upload
        ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
        mFullSizeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);
        byte[] bytes = fullSizeStream.toByteArray();

        //upload image to firebase storage: full image first:
        mCelebImageStorage.child(celebId + Constants.trim(mCelebNameEditText.getText().toString().trim())).child("full").putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //get download url from upload
                final String fullSizeUrl = taskSnapshot.getDownloadUrl().toString();

                //compress bitmap for thumbnail upload
                ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
                mThumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 50, thumbnailStream);

                //upload image to thumbnail storage:
                mCelebImageStorage.child(celebId + Constants.trim(mCelebNameEditText.getText().toString().trim())).child("thumb").putBytes(thumbnailStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //get thumbnail url from upload
                        String thumbnailUrl = taskSnapshot.getDownloadUrl().toString();

                        //create new celeb for firebase database
                        Celeb celeb = new Celeb(celebId, mCelebNameEditText.getText().toString(), fullSizeUrl, thumbnailUrl, 0);

                        //upload the new created celeb to firebase database:
                        mCelebDatabase.child(celebId).setValue(celeb).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //dismiss the dialog
                                mDialog.dismiss();

                                //toast to make sure celeb saved:
                                Toast.makeText(NewCelebActivity.this, "Celeb saved!!", Toast.LENGTH_SHORT).show();

                                //start welcome activity and finish:
                                startActivity(new Intent(NewCelebActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }


}
