package com.blazingtrail.btscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<String> mNames=new ArrayList<>();
    private ArrayList<String> mTags=new ArrayList<>();
    private ArrayList<String> mCats=new ArrayList<>();
    private ArrayList<String> mDates=new ArrayList<>();
    private ArrayList<Bitmap> mImages=new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<String> mNames, ArrayList<String> mTags, ArrayList<String> mCats, ArrayList<String> mDates, ArrayList<Bitmap> mImages, Context mContext) {
        this.mNames = mNames;
        this.mTags = mTags;
        this.mCats = mCats;
        this.mDates = mDates;
        this.mImages = mImages;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rvimage.setImageBitmap(mImages.get(position));
        holder.rvname.setText(mNames.get(position));
        holder.rvtags.setText(mTags.get(position));
        holder.rvcats.setText(mCats.get(position));
        holder.rvdate.setText(mDates.get(position));
        holder.rvlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView rvimage;
        TextView rvname;
        TextView rvtags;
        TextView rvcats;
        TextView rvdate;
        RelativeLayout rvlayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rvimage=itemView.findViewById(R.id.rvImage);
            rvname=itemView.findViewById(R.id.rvName);
            rvtags=itemView.findViewById(R.id.rvTags);
            rvcats=itemView.findViewById(R.id.rvCats);
            rvdate=itemView.findViewById(R.id.rvDate);
            rvlayout=itemView.findViewById(R.id.rvLayout);
        }
    }
}
