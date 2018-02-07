package com.androidsalad.popcorntvapp.Activity.Welcome;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;

import com.androidsalad.popcorntvapp.Adapter.PhotoListAdapter;
import com.androidsalad.popcorntvapp.Model.Photo;
import com.androidsalad.popcorntvapp.Model.Post;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.androidsalad.popcorntvapp.Util.GridSpacingItemDecoration;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    //post Id from Intent:
    private String mPostIdString;

    //views:
    private RecyclerView mRecyclerView;

    //database reference
    private DatabaseReference mPostDatabase, mPostPhotoDatabase, mBaseReference;

    //adapter:
    private List<Photo> mPhotoList;
    private PhotoListAdapter mPhotoAdapter;

    //dialog
    private ProgressDialog mDialog;

    //post views:
    int mPostViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //initializeVariables:
        intializeVariables();

        //get and update post Views:
        updatePostViews();

        //download list of photos from firebase:
        downloadPhotoListFromFirebase();
    }

    private void intializeVariables() {

        //get postId from Intent:
        mPostIdString = getIntent().getStringExtra(Constants.POST_ID);

        //initialize photoList:
        mPhotoList = new ArrayList<>();

        //database:
        mBaseReference = FirebaseDatabase.getInstance().getReference();
        mPostPhotoDatabase = mBaseReference.child(Constants.FIREBASE_POST_PHOTOS_DATABASE);
        mPostDatabase = mBaseReference.child(Constants.FIREBASE_POST_DATABASE);

        //recycler view and adapter:
        mRecyclerView = (RecyclerView) findViewById(R.id.postDetailRecyclerView);
        mPhotoAdapter = new PhotoListAdapter(this, mPostIdString);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mPhotoAdapter);

        //progress dialog:
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

    }

    private void updatePostViews() {

        //get Post Views:
        mPostDatabase.child(mPostIdString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //get post by post Id;
                Post mPost = dataSnapshot.getValue(Post.class);

                //get post views:
                mPostViews = mPost.getPostViews();

                //increment by 1
                mPostViews = mPostViews + 1;

                //child updates to save post and photo:
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/celeb_posts/" + mPost.getCelebId() + "/" + mPost.getPostId() + "/" + "postViews", mPostViews);
                childUpdates.put("/posts/" + mPost.getPostId() + "/" + "postViews", mPostViews);

                mBaseReference.updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //method for downloading photos from Firebase:
    private void downloadPhotoListFromFirebase() {

        mPostPhotoDatabase.child(mPostIdString).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //get Photo from Database:
                Photo photo = dataSnapshot.getValue(Photo.class);

                //extract image url from Photo:
                mPhotoList.add(photo);

                //dismiss dialog if running:
                if (mDialog.isShowing()) mDialog.dismiss();

                //update the image adapter:
                mPhotoAdapter.updatePhotoList(mPhotoList);
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


    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
