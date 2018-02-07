package com.androidsalad.popcorntvapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsalad.popcorntvapp.Activity.Welcome.CelebPostActivity;
import com.androidsalad.popcorntvapp.Activity.Welcome.PostDetailActivity;
import com.androidsalad.popcorntvapp.Model.Post;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.MyPostViewHolder> {

    //context:
    private Context context;

    //post list:
    private List<Post> postList;

    public PostListAdapter(Context context) {
        this.context = context;
        this.postList = new ArrayList<>();
    }

    public class MyPostViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView celebProfileImageView;
        private TextView celebNameTextView;
        private ImageView postImageView;
        private TextView postDescTextView;
        private TextView postImageCountTextView;
        private TextView postViewsTextView;

        public MyPostViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);
        }

        private void findViews(View itemView) {

            //views initialize:
            celebProfileImageView = (CircleImageView) itemView.findViewById(R.id.singlePostCelebProfileImageView);
            celebNameTextView = (TextView) itemView.findViewById(R.id.singlePostCelebNameTextView);
            postImageView = (ImageView) itemView.findViewById(R.id.singlePostImageView);
            postDescTextView = (TextView) itemView.findViewById(R.id.singlePostDescTextView);
            postImageCountTextView = (TextView) itemView.findViewById(R.id.singlePostImageCountTextView);
            postViewsTextView = (TextView) itemView.findViewById(R.id.singlePostViewsTextView);
        }

        public void setData(Post post) {

            //set values with post:
            Glide.with(context).load(post.getCelebThumbUrl()).into(celebProfileImageView);
            celebNameTextView.setText(post.getCelebName());
            Glide.with(context).load(post.getPostThumbUrl()).thumbnail(0.1f).into(postImageView);
            postDescTextView.setText(post.getPostDesc());
            postImageCountTextView.setText("Images: " + post.getPostImages());
            postViewsTextView.setText("Views: " + post.getPostViews());

            //set onclick listeners:
            celebNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CelebPostActivity.class);
                    intent.putExtra(Constants.CELEB_ID, postList.get(getAdapterPosition()).getCelebId());
                    context.startActivity(intent);
                }
            });

            postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra(Constants.POST_ID, postList.get(getAdapterPosition()).getPostId());
                    context.startActivity(intent);
                }
            });


        }

    }

    @Override
    public MyPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_single_post_item, parent, false);

        return new MyPostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyPostViewHolder holder, int position) {

        holder.setData(postList.get(position));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void addAll(List<Post> newPosts) {
        int initialSize = postList.size();
        postList.addAll(newPosts);
        notifyItemRangeInserted(initialSize, newPosts.size());
    }

}
