package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmicc.module_message.ui.data.MessageBgModel;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;

public class MessageBgListAdapter extends RecyclerView.Adapter<MessageBgListAdapter.ViewHolder> {

    private List<MessageBgModel> appList;
    private Context context;

    public MessageBgListAdapter(Context context) {
        this.context = context;
        appList = new ArrayList<>();
    }

    public void updateList(List<MessageBgModel> appList) {
        this.appList = appList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewIcon;
        TextView textViewName;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            imageViewIcon = (ImageView) itemView.findViewById(R.id.imageViewIcon);
        }

        public void onBind(final int position) {
            final MessageBgModel app = appList.get(position);
            textViewName.setText(app.name);
            Glide.with(context.getApplicationContext())
                    .load(app.drawable)
                    .into(imageViewIcon);
        }
    }

}
