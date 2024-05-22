package com.codexxatransco.user.adepter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codexxatransco.user.R;
import com.codexxatransco.user.retrofit.APIClient;

import java.util.List;

public class BannerAdp extends RecyclerView.Adapter<BannerAdp.MyViewHolder> {
    private Context mContext;
    private List<String> itemList;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;


        public MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.imageView);
        }
    }

    public BannerAdp(Context mContext, List<String> categoryList) {
        this.mContext = mContext;
        this.itemList = categoryList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        String item = itemList.get(position);
        Glide.with(mContext).load(APIClient.baseUrl + "/" + item).into(holder.thumbnail);

    }

    @Override
    public int getItemCount() {
        return itemList.size();

    }
}