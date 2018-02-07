package com.androidsalad.popcorntvapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidsalad.popcorntvapp.Activity.Welcome.CelebPostActivity;
import com.androidsalad.popcorntvapp.Model.Celeb;
import com.androidsalad.popcorntvapp.Model.Photo;
import com.androidsalad.popcorntvapp.R;
import com.androidsalad.popcorntvapp.Util.Constants;
import com.androidsalad.popcorntvapp.Util.Filterable;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CelebListAdapter extends RecyclerView.Adapter<CelebListAdapter.MyCelebViewHolder> implements Filterable {

    //context:
    private Context context;

    //list for filtering:
    private List<Celeb> celebListFiltered;

    //list:
    private List<Celeb> celebList;

    public CelebListAdapter(Context context) {
        this.context = context;
        this.celebList = new ArrayList<>();
        this.celebListFiltered = new ArrayList<>();
    }

    public void updateCelebList(List<Celeb> celebList) {
        this.celebList = celebList;
        this.celebListFiltered = celebList;
        notifyDataSetChanged();
    }

    @Override
    public MyCelebViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View convertView = LayoutInflater.from(context).inflate(R.layout.layout_single_celeb_item, parent, false);
        return new MyCelebViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(MyCelebViewHolder holder, int position) {

        holder.setData(celebListFiltered.get(position));

    }

    @Override
    public int getItemCount() {
        return celebListFiltered.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                if (charString.isEmpty()) {

                    celebListFiltered = celebList;
                } else {

                    List<Celeb> filteredList = new ArrayList<>();
                    for (Celeb celeb : celebList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (celeb.getCelebName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(celeb);
                        }
                    }

                    celebListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = celebListFiltered;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                celebListFiltered = (List<Celeb>) filterResults.values;
                notifyDataSetChanged();


            }
        };
    }

    public class MyCelebViewHolder extends RecyclerView.ViewHolder {

        private TextView mCelebNameTextView;
        private ImageView mCelebImageView;
        private TextView mCelebPostCountTextView;

        public MyCelebViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CelebPostActivity.class);
                    intent.putExtra(Constants.CELEB_ID, celebList.get(getAdapterPosition()).getCelebId());
                    context.startActivity(intent);
                }
            });


        }

        private void findViews(View itemView) {

            mCelebNameTextView = (TextView) itemView.findViewById(R.id.singleCelebNameTextView);
            mCelebImageView = (ImageView) itemView.findViewById(R.id.singleCelebImageView);
            mCelebPostCountTextView = (TextView) itemView.findViewById(R.id.singleCelebPostCountTextView);
        }

        public void setData(Celeb celeb) {

            mCelebNameTextView.setText(celeb.getCelebName());
            Glide.with(context).load(celeb.getCelebThumbUrl()).thumbnail(0.1f).into(mCelebImageView);
            mCelebPostCountTextView.setText("Post Count: " + celeb.getPostCount());
        }
    }

}
