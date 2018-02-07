package com.androidsalad.popcorntvapp.Activity.NewPost;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidsalad.popcorntvapp.Activity.Welcome.WelcomeActivity;
import com.androidsalad.popcorntvapp.Adapter.UploadListAdapter;
import com.androidsalad.popcorntvapp.Model.Celeb;
import com.androidsalad.popcorntvapp.Model.Photo;
import com.androidsalad.popcorntvapp.Model.Post;
import com.androidsalad.popcorntvapp.Model.UploadItem;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;

public class NewPostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "NewPostActivity";

    //create & save views:
    private LinearLayout mCreateLayout, mSaveLayout;
    private Spinner mCelebSpinner;
    private ImageView mCelebImageView, mPostMasterImageView;
    private EditText mPostDescEditText, mPostViewsEditText;
    private ImageButton mPostMasterImageButton, mPostAddImagesButton;
    private Button mCreatePostButton, mSavePostButton;
    private TextView mPostDescTextView, mPostImageCountTextView;

    //firebase:
    private StorageReference mPhotoStorage;
    private DatabaseReference mPostDatabase, mBaseDatabase, mCelebDatabase, mPhotoDatabase, mPostPhotoDatabase, mCelebPostDatabase;

    //select celeb spinner items:
    List<String> mCelebNameList;
    ArrayAdapter<String> mCelebAdapter;
    private String mCelebNameString, mCelebThumbUrlString, mCelebIdString, mPostIdString, mPostDescString, mPostMasterImageUrlString;

    //bitmaps
    private Bitmap mFullSizeBitmap, mThumbnailBitmap;

    //dialog
    private ProgressDialog mDialog;

    //recycler View for displaying upload item List:
    private RecyclerView mRecyclerView;
    private List<UploadItem> mUploadList;
    private UploadListAdapter mUploadAdapter;

    //post count:
    int mPostImageCount = 0, mCelebPostCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        //initialize Variables:
        initializeVariables();

        //initialize list of celebs in spinner:
        addCelebNamesToSpinner();

        //set button click listener on post master image button:
        mPostMasterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMasterImageFromGallery();
            }
        });

        //create post with master image:
        mCreatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                createPostInFirebase();
            }
        });

        //set button click listener on post master image button:
        mPostAddImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImagesFromGallery();
            }
        });

        //save post with final image count:
        mSavePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                saveToFirebase();
            }
        });
    }

    private void initializeVariables() {

        //create & save views:
        mCreateLayout = (LinearLayout) findViewById(R.id.newPostCreateLayout);
        mSaveLayout = (LinearLayout) findViewById(R.id.newPostSaveLayout);

        mCelebSpinner = (Spinner) findViewById(R.id.newPostCelebNameSpinner);
        mCelebImageView = (ImageView) findViewById(R.id.newPostCelebImageView);
        mPostMasterImageView = (ImageView) findViewById(R.id.newPostMasterImageView);

        mPostDescEditText = (EditText) findViewById(R.id.newPostDescEditText);
        mPostViewsEditText = (EditText) findViewById(R.id.newPostViewsEditText);

        mPostMasterImageButton = (ImageButton) findViewById(R.id.newPostMasterImageButton);
        mPostAddImagesButton = (ImageButton) findViewById(R.id.newPostAddImagesButton);

        mCreatePostButton = (Button) findViewById(R.id.newPostCreateButton);
        mSavePostButton = (Button) findViewById(R.id.newPostSaveButton);

        mPostDescTextView = (TextView) findViewById(R.id.newPostDescriptionTextView);
        mPostImageCountTextView = (TextView) findViewById(R.id.newPostImageCountTextView);

        //firebase:
        mPhotoStorage = FirebaseStorage.getInstance().getReference().child("photos");
        mBaseDatabase = FirebaseDatabase.getInstance().getReference();
        mPostDatabase = mBaseDatabase.child(Constants.FIREBASE_POST_DATABASE);
        mPhotoDatabase = mBaseDatabase.child(Constants.FIREBASE_PHOTO_DATABASE);
        mCelebDatabase = mBaseDatabase.child(Constants.FIREBASE_CELEB_DATABASE);
        mPostPhotoDatabase = mBaseDatabase.child(Constants.FIREBASE_POST_PHOTOS_DATABASE);
        mCelebPostDatabase = mBaseDatabase.child(Constants.FIREBASE_CELEB_POSTS_DATABASE);

        //spinner on item click listener:
        mCelebSpinner.setOnItemSelectedListener(this);

        //create spinner name list of celeb:
        mCelebNameList = new ArrayList<>();

        //create adapter for spinner and set to spinner:
        mCelebAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCelebNameList);
        mCelebAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCelebSpinner.setAdapter(mCelebAdapter);

        //progress dialog
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);

        //recycler view and upload adapter for displaying list of upload Items:
        mRecyclerView = (RecyclerView) findViewById(R.id.newPostImageRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mUploadAdapter = new UploadListAdapter(this);
        mUploadList = new ArrayList<>();
        mRecyclerView.setAdapter(mUploadAdapter);
    }

    //method: add celeb names to celeb spinner:
    private void addCelebNamesToSpinner() {

        mCelebDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //get celeb list from database:
                Celeb mCeleb = dataSnapshot.getValue(Celeb.class);

                if (mCeleb != null) {
                    //add to celebNames List:
                    mCelebNameList.add(mCeleb.getCelebName());

                    //notify the adapter to update:
                    mCelebAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //method: select master image from gallery:
    private void selectMasterImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.INT_ACTION_PICK);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //master image selection result:
        if (requestCode == Constants.INT_ACTION_PICK && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            //create full size and thumbnail bitmaps
            try {
                mFullSizeBitmap = decodeSampledBitmapFromUri(imageUri, 1280, 1280);
                mThumbnailBitmap = decodeSampledBitmapFromUri(imageUri, 128, 128);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCreatePostButton.setEnabled(true);

            //display image
            Glide.with(this).load(imageUri).into(mPostMasterImageButton);
        }

        //multiple images pic and upload to firebase:
        if (requestCode == ConstantsCustomGallery.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            ArrayList<Image> images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);

            for (int index = 0; index < images.size(); index++) {
                Uri uri = Uri.fromFile(new File(images.get(index).path));

                //saveImageToFirebase:
                saveImagesToFirebase(uri);
            }

            //un-hide recycler view:
            mRecyclerView.setVisibility(View.VISIBLE);

            //hide add images button:
            mPostAddImagesButton.setVisibility(View.GONE);

            //enable save post button:
            mSavePostButton.setEnabled(true);
        }
    }

    //methods to extract bitmap from uris:
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


    //method to calculate in sample size for extracting bitmaps from uri:
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


    private void createPostInFirebase() {

        //disable create post button to avoid repetition in saving
        mCreatePostButton.setEnabled(false);

        //generate unique post id for saving post:
        mPostIdString = mPostDatabase.push().getKey();

        //create unique photo Id for master image:
        final String mPhotoIdString = mPhotoDatabase.push().getKey();

        //compress bitmap for full upload
        ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
        mFullSizeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);

        //upload full size first to firebase storage:
        mPhotoStorage.child(mPostIdString).child(mPhotoIdString).child("full").putBytes(fullSizeStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //get full size download url from firebase:
                final String mFullSizeUrl = taskSnapshot.getDownloadUrl().toString();

                //compress bitmap for full upload
                ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
                mThumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 50, thumbnailStream);

                mPhotoStorage.child(mPostIdString).child(mPhotoIdString).child("thumb").putBytes(thumbnailStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //get thumbnail download url from firebase:
                        String mThumbnailUrl = taskSnapshot.getDownloadUrl().toString();

                        //create new photo:
                        Photo photo = new Photo(mPhotoIdString, mFullSizeUrl);

                        //create new Post:
                        Post mPost = new Post(mPostIdString, mCelebIdString, mCelebNameString, mCelebThumbUrlString, mPostDescEditText.getText().toString(), mThumbnailUrl, Constants.getPostViews(mPostViewsEditText.getText().toString()), 1);

                        //child updates to save post and photo:
                        Map<String, Object> mChildUpdates = new HashMap<>();
                        Map<String, Object> mPostValues = mPost.toMap();
                        Map<String, Object> mPhotoValues = photo.toMap();

                        mChildUpdates.put("/posts/" + mPostIdString, mPostValues);
                        mChildUpdates.put("/photos/" + mPhotoIdString, mPhotoValues);

                        //save photo again in post_photos to retrieve post wise photos:
                        mChildUpdates.put("/post_photos/" + mPostIdString + "/" + mPhotoIdString, mPhotoValues);

                        //save post in celeb_posts to retrieve celeb wise posts:
                        mChildUpdates.put("/celeb_posts/" + mCelebIdString + "/" + mPostIdString, mPostValues);

                        //update all the children of the firebase database:
                        mBaseDatabase.updateChildren(mChildUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //dismiss dialog if running
                                if (mDialog.isShowing()) mDialog.dismiss();

                                //update post count in celeb:
                                updateCelebPostCount();

                                //update UI to show save post linear layout:
                                updateUI();

                            }
                        }); // end of update children on complete listener
                    }
                }); // end of thumbnail success listener

            }
        }); // end of full size success listener


    }

    private void updateCelebPostCount() {

        mCelebPostDatabase.child(mCelebIdString).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCelebPostCount = (int) dataSnapshot.getChildrenCount();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateUI() {

        //hide create layout and reveal save post layout for adding images:
        mCreateLayout.setVisibility(View.GONE);
        mSaveLayout.setVisibility(View.VISIBLE);

        //get post details from post database to ensure details are updated in firebase:
        mPostDatabase.child(mPostIdString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //get post from database:
                Post mPost = dataSnapshot.getValue(Post.class);

                //get post description and master image url:
                mPostDescString = mPost.getPostDesc();
                mPostMasterImageUrlString = mPost.getPostThumbUrl();

                //update UI:
                mPostDescTextView.setText(mPostDescString);
                Glide.with(getApplicationContext()).load(mPostMasterImageUrlString).into(mPostMasterImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selectImagesFromGallery() {

        //start intent for selecting image from gallery by awesome image picker:
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, Constants.MAX_IMAGE_PICK); // set limit for image selection
        startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
    }


    private void saveImagesToFirebase(final Uri mImageUri) {

        //create full size bitmaps:
        try {
            mFullSizeBitmap = decodeSampledBitmapFromUri(mImageUri, 1280, 1280);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get unique id for new photo:
        final String mPhotoIdString = mPhotoDatabase.push().getKey();

        //compress bitmap for full upload
        ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
        mFullSizeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);

        mPhotoStorage.child(mPostIdString).child(mPhotoIdString).putBytes(fullSizeStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //get full size download url from firebase:
                final String mDownloadUrl = taskSnapshot.getDownloadUrl().toString();

                //create new photo:
                Photo mPhoto = new Photo(mPhotoIdString, mDownloadUrl);

                //child updates to save post and photo:
                Map<String, Object> mChildUpdates = new HashMap<>();
                Map<String, Object> mPhotoValues = mPhoto.toMap();

                mChildUpdates.put("/photos/" + mPhotoIdString, mPhotoValues);

                //save photo again in post_photos to retrieve post wise photos:
                mChildUpdates.put("/post_photos/" + mPostIdString + "/" + mPhotoIdString, mPhotoValues);

                //update all the children of the firebase database:
                mBaseDatabase.updateChildren(mChildUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //dismiss dialog if running
                        if (mDialog.isShowing()) mDialog.dismiss();

                        //create upload item:


                        UploadItem item = new UploadItem(mDownloadUrl, mImageUri.getLastPathSegment(), true);
                        mUploadList.add(item);
                        mUploadAdapter.updateList(mUploadList);

                        //update post image count:
                        updatePostImageCount();

                    }
                }); // end of update children on complete listener
            }
        }); // end of full size success listener

    }

    private void updatePostImageCount() {

        mPostPhotoDatabase.child(mPostIdString).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPostImageCount = (int) dataSnapshot.getChildrenCount();

                mPostImageCountTextView.setText(getString(R.string.image_count) + mPostImageCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //finally save to firebase:
    private void saveToFirebase() {

        //disable save button to avoid repetition in saving
        mSavePostButton.setEnabled(false);

        Map<String, Object> mChildUpdates = new HashMap<>();
        mChildUpdates.put("/celeb_posts/" + mCelebIdString + "/" + mPostIdString + "/" + "postImages", mPostImageCount);
        mChildUpdates.put("/posts/" + mPostIdString + "/" + "postImages", mPostImageCount);
        mChildUpdates.put("/celebs/" + mCelebIdString + "/" + "postCount", mCelebPostCount);
        mBaseDatabase.updateChildren(mChildUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //start welcome activity and finish:
                startActivity(new Intent(NewPostActivity.this, WelcomeActivity.class));
                finish();


            }
        });


    }

    //methods for celeb spinner on item selected listener:
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //get selected celeb name for saving in post:
        mCelebNameString = parent.getItemAtPosition(position).toString();

        //get celeb profile pic from database:
        mCelebDatabase.orderByChild(Constants.CELEB_NAME).equalTo(mCelebNameString).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //get selected celeb:
                Celeb mCeleb = dataSnapshot.getValue(Celeb.class);

                //get celeb Id & Thumb Url:
                mCelebIdString = mCeleb.getCelebId();
                mCelebThumbUrlString = mCeleb.getCelebThumbUrl();

                //display celeb image to confirm selection of celeb:
                Glide.with(getApplicationContext()).load(mCelebThumbUrlString).into(mCelebImageView);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
