package com.androidsalad.popcorntvapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.androidsalad.popcorntvapp.Activity.Welcome.PostImagesActivity;
import com.androidsalad.popcorntvapp.Model.Photo;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.MyPhotoViewHolder> {

    //context:
    private Context context;

    //post Id;
    private String mPostIdString;

    //post list:
    private List<Photo> photoList;

    public PhotoListAdapter(Context context, String mPostId) {
        this.context = context;
        this.photoList = new ArrayList<>();
        this.mPostIdString = mPostId;
    }

    public class MyPhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView photoImageView;
        private ProgressBar photoProgressBar;

        public MyPhotoViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);

            photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PostImagesActivity.class);
                    intent.putExtra(Constants.POST_ID, mPostIdString);
                    intent.putExtra("position", getAdapterPosition());
                    context.startActivity(intent);

                }
            });
        }

        private void findViews(View itemView) {
            //views initialize:
            photoImageView = (ImageView) itemView.findViewById(R.id.singlePhotoItemImageView);
            photoProgressBar = (ProgressBar) itemView.findViewById(R.id.singlePhotoItemProgressBar);
        }

        public void setData(Photo photo) {

            //set values with post:
            Glide.with(context).load(photo.getPhotoFullUrl()).thumbnail(0.1f)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            photoProgressBar.setVisibility(View.GONE);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            photoProgressBar.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .into(photoImageView);
        }


    }

    @Override
    public MyPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_single_photo_item, parent, false);

        return new MyPhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyPhotoViewHolder holder, int position) {

        holder.setData(photoList.get(position));
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void updatePhotoList(List<Photo> photoList) {
        this.photoList = photoList;
        notifyDataSetChanged();
    }

}