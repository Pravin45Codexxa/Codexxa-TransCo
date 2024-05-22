package com.codexxatransco.user.adepter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.codexxatransco.user.R;
import com.codexxatransco.user.model.LogisticHistory;
import com.codexxatransco.user.utility.SessionManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogisticAdapter extends RecyclerView.Adapter<LogisticAdapter.MyViewHolder> {




    private List<LogisticHistory> historyinfo;
    private RecyclerTouchListener listener;
    SessionManager sessionManager;

    public interface RecyclerTouchListener {
        public void onClickPackageItem(LogisticHistory item, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_icon)
        public ImageView imgIcon;
        @BindView(R.id.txt_date)
        public TextView txtDate;
        @BindView(R.id.txt_type)
        public TextView txtType;
        @BindView(R.id.txt_totle)
        public TextView txtTotle;
        @BindView(R.id.txt_pickaddress)
        public TextView txtPickaddress;
        @BindView(R.id.txt_dropaddress)
        public TextView txtDropaddress;
        @BindView(R.id.txt_status)
        public TextView txtStatus;
        @BindView(R.id.lvl_click)
        public LinearLayout lvlClick;


        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public LogisticAdapter(Context mContext, List<LogisticHistory> historyinfo, final RecyclerTouchListener listener) {

        this.historyinfo = historyinfo;
        this.listener = listener;
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_logistic, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        LogisticHistory item = historyinfo.get(position);
        holder.txtDate.setText("" + item.getLogisticDate());
        holder.txtType.setText("" + item.getPackageNumber());
        holder.txtTotle.setText(sessionManager.getStringData(SessionManager.currency) + item.getTotal());
        holder.txtPickaddress.setText(""+item.getPickAddress());
        holder.txtDropaddress.setText(""+item.getDropAddress());

        holder.txtStatus.setText("" + item.getStatus());

        holder.lvlClick.setOnClickListener(v -> {
            listener.onClickPackageItem(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return historyinfo.size();
    }
}