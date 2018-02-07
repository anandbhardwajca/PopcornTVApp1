package com.androidsalad.popcorntvapp.Activity.Welcome;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.androidsalad.popcorntvapp.Adapter.CelebListAdapter;
import com.androidsalad.popcorntvapp.Adapter.PhotoListAdapter;
import com.androidsalad.popcorntvapp.Model.Celeb;
import com.androidsalad.popcorntvapp.Model.Photo;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.androidsalad.popcorntvapp.Util.GridSpacingItemDecoration;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SearchCelebActivity extends AppCompatActivity {

    private static final String TAG = "SearchCelebActivity";

    //views:
    private RecyclerView mRecyclerView;
    private CelebListAdapter mCelebAdapter;
    private List<Celeb> mCelebList;

    //searchView:
    private SearchView mCelebSearchView;

    //firebase database:
    private DatabaseReference mCelebDatabase;

    //progress dialog:
    private ProgressDialog mDialog;

    //items per page:
    private int mPostPerPage = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_celeb);

        //initializeVariables:
        intializeVariables();

        //download celeb list from firebase:
        downloadCelebListFromFirebase();

        //initiate Search View:
        initiateSearchView();

    }

    private void initiateSearchView() {


    }

    private void intializeVariables() {

        //initialize photoList:
        mCelebList = new ArrayList<>();

        //database:
        mCelebDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CELEB_DATABASE);

        //recycler view and adapter:
        mRecyclerView = (RecyclerView) findViewById(R.id.searchCelebRecyclerView);
        mCelebAdapter = new CelebListAdapter(this);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mCelebAdapter);

        //progress dialog:
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

    }

    //method for downloading celeb list from Firebase:
    private void downloadCelebListFromFirebase() {

        mCelebDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //get Photo from Database:
                Celeb mCeleb = dataSnapshot.getValue(Celeb.class);

                Log.d(TAG, "onChildAdded: " + mCeleb.getCelebName());

                //extract image url from Photo:
                mCelebList.add(mCeleb);

                //dismiss dialog if running:
                if (mDialog.isShowing()) mDialog.dismiss();

                //update the image adapter:
                mCelebAdapter.updateCelebList(mCelebList);
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);


        //Associate searchable configuration with the SearchView
        SearchManager mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //searchview:
        mCelebSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        mCelebSearchView.setSearchableInfo(mSearchManager.getSearchableInfo(getComponentName()));
        mCelebSearchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        mCelebSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                mCelebAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mCelebAdapter.getFilter().filter(query);
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!mCelebSearchView.isIconified()) {
            mCelebSearchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }


    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
