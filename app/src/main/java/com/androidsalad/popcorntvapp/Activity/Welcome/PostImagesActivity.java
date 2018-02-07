package com.androidsalad.popcorntvapp.Activity.Welcome;

import android.app.ProgressDialog;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.androidsalad.popcorntvapp.Adapter.ImageAdapter;
import com.androidsalad.popcorntvapp.Model.Photo;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PostImagesActivity extends AppCompatActivity {

    //views
    private ViewPager mViewPager;

    //adapter for displaying images:
    private ImageAdapter mImageAdapter;

    //database reference
    private DatabaseReference mPostDatabase, mPostPhotoDatabase, mBaseReference;

    //post Id from Intent:
    private String mPostIdString;
    private int mPositionInt;

    //list
    List<String> mPhotoList;

    //dialog
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_images);

        //initializeVariables:
        intializeVariables();

        //download list of photos from firebase:
        downloadPhotoListFromFirebase();

    }

    private void downloadPhotoListFromFirebase() {

        mPostPhotoDatabase.child(mPostIdString).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //get Photo from Database:
                Photo mPhoto = dataSnapshot.getValue(Photo.class);

                //extract image url from Photo:
                mPhotoList.add(mPhoto.getPhotoFullUrl());

                //dismiss dialog if running:
                if (mDialog.isShowing()) mDialog.dismiss();

                //update the image adapter:
                mImageAdapter.updatePhotoList(mPhotoList);

                mViewPager.setCurrentItem(mPositionInt, true);
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

    private void intializeVariables() {

        //get postId from Intent:
        mPostIdString = getIntent().getStringExtra(Constants.POST_ID);

        mPositionInt = getIntent().getIntExtra(Constants.INT_POSITION, 0);

        //image adapter:
        mImageAdapter = new ImageAdapter(this);


        //initialize photoList:
        mPhotoList = new ArrayList<>();

        //views:
        mViewPager = (ViewPager) findViewById(R.id.postViewPager);
        mViewPager.setAdapter(mImageAdapter);

        //database:
        mPostPhotoDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POST_PHOTOS_DATABASE);
        mPostDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POST_DATABASE);
        mBaseReference = FirebaseDatabase.getInstance().getReference();

        //progress dialog:
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }


}
