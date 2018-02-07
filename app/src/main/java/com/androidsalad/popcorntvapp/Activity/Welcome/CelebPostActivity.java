package com.androidsalad.popcorntvapp.Activity.Welcome;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidsalad.popcorntvapp.Adapter.CelebPostListAdapter;
import com.androidsalad.popcorntvapp.Adapter.PostListAdapter;
import com.androidsalad.popcorntvapp.Model.Celeb;
import com.androidsalad.popcorntvapp.Model.Post;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.androidsalad.popcorntvapp.Util.GridSpacingItemDecoration;
import com.androidsalad.popcorntvapp.Util.InternetUtil;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CelebPostActivity extends AppCompatActivity {

    private static final String TAG = "CelebPostActivity";

    //views:
    private ImageView mCelebImageView;
    private TextView mCelebNameTextView;
    private RecyclerView mRecyclerView;

    //celeb Id from Intent:
    private String mCelebIdString;

    //post list and adapter for recycler view:
    private List<Post> mPostList;
    private CelebPostListAdapter mPostAdapter;

    //firebase database:
    private DatabaseReference mCelebDatabase, mCelebPostDatabase, mPostPhotosDatabase;

    //progress dialog:
    private ProgressDialog mDialog;

    //items per page:
    private int mPostPerPage = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_celeb_post);

        //initialize Variables:
        initializeVariables();

        //set celeb image and name:
        setCelebNameAndImage();


        //check Internet Connection:
        if (InternetUtil.isNetworkAvailable(this)) {

            //download post list from firebase database:
            loadDataFromFirebase(null);

        } else {

            showAlertDialog();
        }

    }

    private void setCelebNameAndImage() {

        mCelebDatabase.child(mCelebIdString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //get celeb:
                Celeb celeb = dataSnapshot.getValue(Celeb.class);

                Log.d(TAG, "onCelebAdded: " + celeb.getCelebName());


                //set celeb Name & Image:
                mCelebNameTextView.setText(celeb.getCelebName());
                Glide.with(CelebPostActivity.this).load(celeb.getCelebThumbUrl()).into(mCelebImageView);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initializeVariables() {

        //views:
        mCelebNameTextView = (TextView) findViewById(R.id.celebPostActivityCelebNameTextView);
        mCelebImageView = (ImageView) findViewById(R.id.celebPostActivityCelebImageView);

        //get postId from Intent:
        mCelebIdString = getIntent().getStringExtra(Constants.CELEB_ID);

        //recycler view methods:
        mRecyclerView = (RecyclerView) findViewById(R.id.celebPostActivityPostRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //initialize post list:
        mPostList = new ArrayList<>();

        //adapter for recycler view:
        mPostAdapter = new CelebPostListAdapter(this);
        mRecyclerView.setAdapter(mPostAdapter);

        //firebase database:
        mCelebDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CELEB_DATABASE);
        mCelebPostDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CELEB_POSTS_DATABASE);
        mPostPhotosDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POST_PHOTOS_DATABASE);

        //progress dialog: show:
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private void loadDataFromFirebase(String nodeId) {

        //create firebase query:
        Query query;

        //check if node passed:
        if (nodeId == null) {
            query = mCelebPostDatabase.child(mCelebIdString)
                    .orderByKey()
                    .limitToLast(mPostPerPage);
        } else {

            query = mCelebPostDatabase.child(mCelebIdString)
                    .orderByKey()
                    .endAt(nodeId)
                    .limitToLast(mPostPerPage);
        }

        //run query:
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //initiate new post List:
                List<Post> newPosts = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    Post post = postSnapshot.getValue(Post.class);
                    //check if post is live:
                    if (post != null) {
                        //add to new post List:
                        newPosts.add(post);
                    }


                }
                if (mDialog.isShowing()) mDialog.dismiss();

                Collections.reverse(newPosts);
                mPostAdapter.addAll(newPosts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setMessage("Internet Connection Required")
                .setCancelable(false)
                .setPositiveButton("Retry",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int id) {

                                // Restart the activity
                                Intent intent = new Intent(CelebPostActivity.this,
                                        CelebPostActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
